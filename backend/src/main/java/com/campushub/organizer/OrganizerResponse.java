package com.campushub.organizer;

public record OrganizerResponse(
        Long id,
        String name,
        String description,
        String contact
) {
    public static OrganizerResponse from(Organizer organizer) {
        return new OrganizerResponse(
                organizer.getId(),
                organizer.getName(),
                organizer.getDescription(),
                organizer.getContact()
        );
    }
}
