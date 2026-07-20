package com.campushub.organizer;

import com.campushub.event.AdminEventEntryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record OrganizerEventRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String summary,
        @NotBlank String content,
        @NotNull Long categoryId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotBlank @Size(max = 200) String location,
        @Size(max = 500) String coverUrl,
        @Size(max = 500) String sourceUrl,
        boolean demo,
        @NotNull List<@Valid AdminEventEntryRequest> entries
) {
}
