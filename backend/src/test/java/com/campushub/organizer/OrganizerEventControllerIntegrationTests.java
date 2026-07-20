package com.campushub.organizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
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
@Sql(statements = "UPDATE organizer_users SET password_change_required = FALSE WHERE username = 'techclub'")
class OrganizerEventControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listsOnlyCurrentOrganizersEvents() throws Exception {
        mockMvc.perform(get("/api/organizer/events").session(login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].organizerId").value(1));
    }

    @Test
    void createsDraftAndAutomaticallyBindsCurrentOrganizer() throws Exception {
        mockMvc.perform(post("/api/organizer/events").with(csrf()).session(login())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest(true, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.organizerId").value(1));
    }

    @Test
    void cannotEditAnotherOrganizersEvent() throws Exception {
        mockMvc.perform(put("/api/organizer/events/2").with(csrf()).session(login())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest(true, true)))
                .andExpect(status().isNotFound());
    }

    @Test
    void submitsCompleteDraftForReview() throws Exception {
        long id = createDraft(validRequest(true, true));

        mockMvc.perform(patch("/api/organizer/events/{id}/submit", id)
                        .with(csrf()).session(login()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_REVIEW"));
    }

    @Test
    void refusesSubmissionWithoutSourceOrOfficialEntry() throws Exception {
        long missingSourceId = createDraft(validRequest(false, true));
        mockMvc.perform(patch("/api/organizer/events/{id}/submit", missingSourceId)
                        .with(csrf()).session(login()))
                .andExpect(status().isBadRequest());

        long missingEntryId = createDraft(validRequest(true, false));
        mockMvc.perform(patch("/api/organizer/events/{id}/submit", missingEntryId)
                        .with(csrf()).session(login()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannotEditPublishedEventButCanCancelIt() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(put("/api/organizer/events/1").with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest(true, true)))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/organizer/events/1/cancel").with(csrf()).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private long createDraft(String request) throws Exception {
        String response = mockMvc.perform(post("/api/organizer/events").with(csrf()).session(login())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private MockHttpSession login() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/organizer/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"techclub","password":"campus-organizer-2026"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }

    private String validRequest(boolean withSource, boolean withEntry) {
        String sourceUrl = withSource ? "\"https://example.com/source/organizer-demo\"" : "null";
        String entries = withEntry
                ? """
                  [{
                    "type":"LINK",
                    "label":"查看官方入口",
                    "url":"https://example.com/organizer-demo",
                    "qrImageUrl":null,
                    "sortOrder":10
                  }]
                  """
                : "[]";
        return """
                {
                  "title":"活动方新增演示活动",
                  "summary":"用于验证活动方自助提交和审核流程的模拟活动。",
                  "content":"这是活动方接口集成测试创建的虚构内容。",
                  "categoryId":1,
                  "startTime":"2026-11-01T19:00:00",
                  "endTime":"2026-11-01T20:30:00",
                  "location":"示例校园创新楼 101",
                  "coverUrl":"https://picsum.photos/seed/organizer-event/960/640",
                  "sourceUrl":%s,
                  "demo":true,
                  "entries":%s
                }
                """.formatted(sourceUrl, entries);
    }
}
