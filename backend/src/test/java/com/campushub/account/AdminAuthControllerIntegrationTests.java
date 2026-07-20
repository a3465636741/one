package com.campushub.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminAuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectsAdminApiButKeepsPublicApiAvailable() throws Exception {
        mockMvc.perform(get("/api/admin/events"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/public/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));
    }

    @Test
    void exposesCsrfTokenWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void rejectsIncorrectCredentials() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"username":"admin","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("账号或密码不正确"));
    }

    @Test
    void requiresInitialPasswordChangeBeforeManagingEvents() throws Exception {
        MockHttpSession session = loginWithInitialPassword();

        mockMvc.perform(get("/api/admin/account").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.passwordChangeRequired").value(true));

        mockMvc.perform(get("/api/admin/events").session(session))
                .andExpect(status().is(428));
    }

    @Test
    void changesPasswordAndUnlocksAdminOperations() throws Exception {
        MockHttpSession session = loginWithInitialPassword();

        mockMvc.perform(patch("/api/admin/account/password").with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword":"campus-admin-2026",
                                  "newPassword":"new-campus-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordChangeRequired").value(false));

        mockMvc.perform(get("/api/admin/events").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));
    }

    @Test
    void logsOutCurrentSession() throws Exception {
        MockHttpSession session = loginWithInitialPassword();

        mockMvc.perform(post("/api/admin/auth/logout").with(csrf()).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/account").session(session))
                .andExpect(status().isUnauthorized());
    }

    private MockHttpSession loginWithInitialPassword() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/admin/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"campus-admin-2026"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("内容管理员"))
                .andReturn()
                .getRequest()
                .getSession(false);
    }
}
