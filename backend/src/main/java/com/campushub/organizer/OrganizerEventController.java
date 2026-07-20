package com.campushub.organizer;

import com.campushub.category.CategoryResponse;
import com.campushub.event.AdminEventResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizer")
public class OrganizerEventController {

    private final OrganizerEventService organizerEventService;

    public OrganizerEventController(OrganizerEventService organizerEventService) {
        this.organizerEventService = organizerEventService;
    }

    @GetMapping("/events")
    public List<AdminEventResponse> events(Authentication authentication) {
        return organizerEventService.findAll(principal(authentication).organizerId());
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminEventResponse create(
            Authentication authentication,
            @Valid @RequestBody OrganizerEventRequest request
    ) {
        return organizerEventService.create(principal(authentication).organizerId(), request);
    }

    @PutMapping("/events/{id}")
    public AdminEventResponse update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody OrganizerEventRequest request
    ) {
        return organizerEventService.update(
                principal(authentication).organizerId(),
                id,
                request
        );
    }

    @PatchMapping("/events/{id}/submit")
    public AdminEventResponse submit(Authentication authentication, @PathVariable Long id) {
        return organizerEventService.submit(principal(authentication).organizerId(), id);
    }

    @PatchMapping("/events/{id}/cancel")
    public AdminEventResponse cancel(Authentication authentication, @PathVariable Long id) {
        return organizerEventService.cancel(principal(authentication).organizerId(), id);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return organizerEventService.findCategories();
    }

    private OrganizerPrincipal principal(Authentication authentication) {
        return (OrganizerPrincipal) authentication.getPrincipal();
    }
}
