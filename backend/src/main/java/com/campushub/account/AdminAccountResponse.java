package com.campushub.account;

import java.time.LocalDateTime;

public record AdminAccountResponse(
        Long id,
        String username,
        String displayName,
        String role,
        boolean passwordChangeRequired,
        LocalDateTime lastLoginAt
) {
    public static AdminAccountResponse from(AdminUser user) {
        return new AdminAccountResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.isPasswordChangeRequired(),
                user.getLastLoginAt()
        );
    }
}
