package com.campushub.event;

import com.campushub.category.CategoryResponse;
import com.campushub.organizer.OrganizerResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminEventController {

    private final AdminEventService adminEventService;

    public AdminEventController(AdminEventService adminEventService) {
        this.adminEventService = adminEventService;
    }

    @GetMapping("/events")
    public List<AdminEventResponse> events(@RequestParam(required = false) EventStatus status) {
        return adminEventService.findAll(status);
    }

    @GetMapping("/events/{id}")
    public AdminEventResponse event(@PathVariable Long id) {
        return adminEventService.findById(id);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminEventResponse create(@Valid @RequestBody AdminEventRequest request) {
        return adminEventService.create(request);
    }

    @PutMapping("/events/{id}")
    public AdminEventResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AdminEventRequest request
    ) {
        return adminEventService.update(id, request);
    }

    @PatchMapping("/events/{id}/status")
    public AdminEventResponse changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminEventStatusRequest request
    ) {
        return adminEventService.changeStatus(id, request.status(), request.reviewNote());
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return adminEventService.findCategories();
    }

    @GetMapping("/organizers")
    public List<OrganizerResponse> organizers() {
        return adminEventService.findOrganizers();
    }
}
