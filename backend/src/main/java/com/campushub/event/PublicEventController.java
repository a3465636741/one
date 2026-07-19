package com.campushub.event;

import com.campushub.category.CategoryResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicEventController {

    private final EventCatalogService eventCatalogService;

    public PublicEventController(EventCatalogService eventCatalogService) {
        this.eventCatalogService = eventCatalogService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return eventCatalogService.findCategories();
    }

    @GetMapping("/events")
    public List<EventSummaryResponse> events(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return eventCatalogService.search(keyword, category, from, to);
    }

    @GetMapping("/events/{id}")
    public EventDetailResponse event(@PathVariable Long id) {
        return eventCatalogService.findById(id);
    }
}
