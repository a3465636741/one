package com.campushub.event;

import java.time.LocalDateTime;
import java.util.List;

public record EventDetailResponse(
        Long id,
        String title,
        String summary,
        String content,
        String category,
        String categorySlug,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        String organizer,
        String coverUrl,
        String sourceUrl,
        boolean demo,
        List<EventEntryResponse> entries
) {
    public static EventDetailResponse from(Event event) {
        return new EventDetailResponse(
                event.getId(),
                event.getTitle(),
                event.getSummary(),
                event.getContent(),
                event.getCategory().getName(),
                event.getCategory().getSlug(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getOrganizer().getName(),
                event.getCoverUrl(),
                event.getSourceUrl(),
                event.isDemo(),
                event.getEntries().stream().map(EventEntryResponse::from).toList()
        );
    }
}
