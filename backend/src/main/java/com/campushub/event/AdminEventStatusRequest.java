package com.campushub.event;

import jakarta.validation.constraints.NotNull;

public record AdminEventStatusRequest(@NotNull EventStatus status) {
}
