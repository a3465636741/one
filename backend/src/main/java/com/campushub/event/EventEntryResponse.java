package com.campushub.event;

public record EventEntryResponse(
        Long id,
        String type,
        String label,
        String url,
        String qrImageUrl
) {
    public static EventEntryResponse from(EventEntry entry) {
        return new EventEntryResponse(
                entry.getId(),
                entry.getType().name(),
                entry.getLabel(),
                entry.getUrl(),
                entry.getQrImageUrl()
        );
    }
}
