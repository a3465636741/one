package com.campushub.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminEventStatusRequest(
        @NotNull EventStatus status,
        @Size(max = 500) String reviewNote
) {
}
