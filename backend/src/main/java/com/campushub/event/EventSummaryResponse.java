package com.campushub.event;

import java.time.LocalDateTime;

public record EventSummaryResponse(
        Long id,
        String title,
        String summary,
        String category,
        String categorySlug,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        String organizer,
        String coverUrl,
        boolean demo
) {
    public static EventSummaryResponse from(Event event) {
        return new EventSummaryResponse(
                event.getId(),
                event.getTitle(),
                event.getSummary(),
                event.getCategory().getName(),
                event.getCategory().getSlug(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getOrganizer().getName(),
                event.getCoverUrl(),
                event.isDemo()
        );
    }
}
