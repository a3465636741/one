package com.campushub.organizer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizerPasswordChangeRequest(
        @NotBlank @Size(max = 200) String currentPassword,
        @NotBlank @Size(min = 10, max = 72) String newPassword
) {
}
