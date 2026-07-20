package com.campushub.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", roles = "ADMIN")
@Sql(statements = "UPDATE admin_users SET password_change_required = FALSE WHERE username = 'admin'")
class AdminEventControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listsAllEventsAndSupportsStatusFilter() throws Exception {
        mockMvc.perform(get("/api/admin/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));

        mockMvc.perform(get("/api/admin/events").param("status", "PUBLISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));
    }

    @Test
    void createsDraftEventWithOfficialEntry() throws Exception {
        mockMvc.perform(post("/api/admin/events").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest(true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("管理端新增演示活动"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.entries[0].url")
                        .value("https://example.com/admin-demo"));
    }

    @Test
    void rejectsInvalidEventTime() throws Exception {
        String request = validRequest(true)
                .replace("2026-11-01T20:30:00", "2026-11-01T18:30:00");

        mockMvc.perform(post("/api/admin/events").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnknownCategory() throws Exception {
        String request = validRequest(true).replace("\"categoryId\": 1", "\"categoryId\": 9999");

        mockMvc.perform(post("/api/admin/events").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publishesCreatedEventAndMakesItPublic() throws Exception {
        String response = mockMvc.perform(post("/api/admin/events").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest(true)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode created = objectMapper.readTree(response);
        long id = created.get("id").asLong();

        mockMvc.perform(patch("/api/admin/events/{id}/status", id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/public/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("管理端新增演示活动"));
    }

    @Test
    void refusesToPublishEventWithoutOfficialEntry() throws Exception {
        String response = mockMvc.perform(post("/api/admin/events").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest(false)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/admin/events/{id}/status", id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatesExistingEventWithoutChangingItsStatus() throws Exception {
        String request = validRequest(true)
                .replace("管理端新增演示活动", "管理端编辑后的活动");

        mockMvc.perform(put("/api/admin/events/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("管理端编辑后的活动"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void archivesPublishedEventAndRemovesItFromPublicApi() throws Exception {
        mockMvc.perform(patch("/api/admin/events/1/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ARCHIVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(get("/api/public/events/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsPendingEventWithReviewNote() throws Exception {
        mockMvc.perform(patch("/api/admin/events/1/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"REJECTED","reviewNote":"请补充活动来源和准确地点"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.reviewNote").value("请补充活动来源和准确地点"));
    }

    @Test
    void requiresReviewNoteWhenRejectingEvent() throws Exception {
        mockMvc.perform(patch("/api/admin/events/1/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsCategoryAndOrganizerReferences() throws Exception {
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9));

        mockMvc.perform(get("/api/admin/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));
    }

    private String validRequest(boolean withEntry) {
        String entries = withEntry
                ? """
                  [{
                    "type": "LINK",
                    "label": "查看官方入口",
                    "url": "https://example.com/admin-demo",
                    "qrImageUrl": "https://example.com/admin-demo-qr.png",
                    "sortOrder": 10
                  }]
                  """
                : "[]";

        return """
                {
                  "title": "管理端新增演示活动",
                  "summary": "用于验证管理员新增、编辑和发布流程的模拟活动。",
                  "content": "这是管理员接口集成测试创建的模拟内容。",
                  "categoryId": 1,
                  "organizerId": 1,
                  "startTime": "2026-11-01T19:00:00",
                  "endTime": "2026-11-01T20:30:00",
                  "location": "示例校园管理楼 101",
                  "coverUrl": "https://picsum.photos/seed/admin-event/960/640",
                  "sourceUrl": "https://example.com/source/admin-demo",
                  "demo": true,
                  "entries": %s
                }
                """.formatted(entries);
    }
}
