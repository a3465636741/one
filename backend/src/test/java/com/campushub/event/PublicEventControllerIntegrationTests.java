package com.campushub.event;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PublicEventControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsAllPublishedDemoEvents() throws Exception {
        mockMvc.perform(get("/api/public/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)))
                .andExpect(jsonPath("$[0].title").value("“从代码到产品”校园技术分享会"))
                .andExpect(jsonPath("$[0].demo").value(true));
    }

    @Test
    void filtersEventsByKeyword() throws Exception {
        mockMvc.perform(get("/api/public/events").param("keyword", "摄影"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("秋日校园摄影打卡挑战"));
    }

    @Test
    void filtersEventsByCategorySlug() throws Exception {
        mockMvc.perform(get("/api/public/events").param("category", "lectures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category").value("学术讲座"))
                .andExpect(jsonPath("$[1].category").value("学术讲座"));
    }

    @Test
    void returnsEventDetailAndOfficialEntry() throws Exception {
        mockMvc.perform(get("/api/public/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.entries", hasSize(1)))
                .andExpect(jsonPath("$.entries[0].label").value("前往报名入口"))
                .andExpect(jsonPath("$.entries[0].url").value("https://example.com/demo-event/1"))
                .andExpect(jsonPath("$.entries[0].qrImageUrl").isNotEmpty());
    }

    @Test
    void returnsNotFoundForUnknownEvent() throws Exception {
        mockMvc.perform(get("/api/public/events/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/public/events").param("from", "2026-99-99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEndDateBeforeStartDate() throws Exception {
        mockMvc.perform(get("/api/public/events")
                        .param("from", "2026-10-01")
                        .param("to", "2026-09-01"))
                .andExpect(status().isBadRequest());
    }
}
