package com.campushub.organizer;

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
class OrganizerAuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectsOrganizerApi() throws Exception {
        mockMvc.perform(get("/api/organizer/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsIncorrectCredentials() throws Exception {
        mockMvc.perform(post("/api/organizer/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"techclub","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("账号或密码不正确"));
    }

    @Test
    void requiresInitialPasswordChangeBeforeManagingEvents() throws Exception {
        MockHttpSession session = loginWithInitialPassword();

        mockMvc.perform(get("/api/organizer/account").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("techclub"))
                .andExpect(jsonPath("$.organizerId").value(1))
                .andExpect(jsonPath("$.passwordChangeRequired").value(true));

        mockMvc.perform(get("/api/organizer/events").session(session))
                .andExpect(status().is(428));
    }

    @Test
    void changesPasswordAndUnlocksOrganizerOperations() throws Exception {
        MockHttpSession session = loginWithInitialPassword();

        mockMvc.perform(patch("/api/organizer/account/password").with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword":"campus-organizer-2026",
                                  "newPassword":"new-organizer-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordChangeRequired").value(false));

        mockMvc.perform(get("/api/organizer/events").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void logsOutCurrentSession() throws Exception {
        MockHttpSession session = loginWithInitialPassword();

        mockMvc.perform(post("/api/organizer/auth/logout").with(csrf()).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/organizer/account").session(session))
                .andExpect(status().isUnauthorized());
    }

    private MockHttpSession loginWithInitialPassword() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/organizer/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"techclub","password":"campus-organizer-2026"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("启航科技社运营员"))
                .andReturn()
                .getRequest()
                .getSession(false);
    }
}
