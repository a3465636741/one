package com.campushub.organizer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizerLoginRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Size(max = 200) String password
) {
}
