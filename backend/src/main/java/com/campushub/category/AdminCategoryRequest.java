package com.campushub.category;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminCategoryRequest(
        @NotBlank @Size(max = 60) String name,
        @NotBlank
        @Size(max = 80)
        @Pattern(
                regexp = "[a-z0-9]+(?:-[a-z0-9]+)*",
                message = "分类标识只能包含小写字母、数字和连字符"
        )
        String slug,
        @Min(0) @Max(9999) int sortOrder,
        boolean active
) {
}
