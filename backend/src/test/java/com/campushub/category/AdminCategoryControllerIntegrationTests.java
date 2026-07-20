package com.campushub.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", roles = "ADMIN")
@Sql(statements = "UPDATE admin_users SET password_change_required = FALSE WHERE username = 'admin'")
class AdminCategoryControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsCategoriesWithUsageCount() throws Exception {
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9))
                .andExpect(jsonPath("$[0].name").value("学术讲座"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].eventCount").value(2));
    }

    @Test
    void createsAndUpdatesCategory() throws Exception {
        String response = mockMvc.perform(post("/api/admin/categories").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"创新创业","slug":"innovation","sortOrder":15,"active":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("创新创业"))
                .andExpect(jsonPath("$.eventCount").value(0))
                .andReturn().getResponse().getContentAsString();

        long id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/admin/categories/{id}", id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"创新实践","slug":"innovation-practice","sortOrder":16,"active":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("创新实践"))
                .andExpect(jsonPath("$.slug").value("innovation-practice"));
    }

    @Test
    void rejectsDuplicateNameAndInvalidSlug() throws Exception {
        mockMvc.perform(post("/api/admin/categories").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"学术讲座","slug":"another-lecture","sortOrder":15,"active":true}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/admin/categories").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"创新创业","slug":"中文 标识","sortOrder":15,"active":true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivatesCategoryWithoutDeletingHistoricalEvents() throws Exception {
        mockMvc.perform(patch("/api/admin/categories/1/active").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.eventCount").value(2));

        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8));

        mockMvc.perform(get("/api/public/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("学术讲座"));
    }

    @Test
    void refusesNewEventInInactiveCategory() throws Exception {
        mockMvc.perform(patch("/api/admin/categories/1/active").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/events").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"停用分类测试活动",
                                  "summary":"验证停用分类不能继续用于新活动。",
                                  "content":"这是接口测试使用的虚构活动。",
                                  "categoryId":1,
                                  "organizerId":1,
                                  "startTime":"2026-11-01T19:00:00",
                                  "endTime":"2026-11-01T20:30:00",
                                  "location":"示例校园管理楼 101",
                                  "coverUrl":null,
                                  "sourceUrl":null,
                                  "demo":true,
                                  "entries":[]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
