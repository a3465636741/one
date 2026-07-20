package com.campushub.organizer;

import java.time.LocalDateTime;

public record OrganizerAccountResponse(
        Long id,
        String username,
        String displayName,
        Long organizerId,
        String organizerName,
        boolean passwordChangeRequired,
        LocalDateTime lastLoginAt
) {
    public static OrganizerAccountResponse from(OrganizerUser user) {
        return new OrganizerAccountResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getOrganizer().getId(),
                user.getOrganizer().getName(),
                user.isPasswordChangeRequired(),
                user.getLastLoginAt()
        );
    }
}
