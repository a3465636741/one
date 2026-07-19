package com.campushub.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminEventEntryRequest(
        @NotNull EntryType type,
        @NotBlank @Size(max = 100) String label,
        @Size(max = 500) String url,
        @Size(max = 500) String qrImageUrl,
        int sortOrder
) {
}
