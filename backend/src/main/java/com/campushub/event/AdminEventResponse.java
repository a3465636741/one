package com.campushub.event;

import java.time.LocalDateTime;
import java.util.List;

public record AdminEventResponse(
        Long id,
        String title,
        String summary,
        String content,
        Long categoryId,
        String category,
        String categorySlug,
        Long organizerId,
        String organizer,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        String coverUrl,
        String sourceUrl,
        EventStatus status,
        boolean demo,
        List<EventEntryResponse> entries
) {
    public static AdminEventResponse from(Event event) {
        return new AdminEventResponse(
                event.getId(),
                event.getTitle(),
                event.getSummary(),
                event.getContent(),
                event.getCategory().getId(),
                event.getCategory().getName(),
                event.getCategory().getSlug(),
                event.getOrganizer().getId(),
                event.getOrganizer().getName(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getCoverUrl(),
                event.getSourceUrl(),
                event.getStatus(),
                event.isDemo(),
                event.getEntries().stream().map(EventEntryResponse::from).toList()
        );
    }
}
