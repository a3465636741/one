package com.campushub.category;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        int sortOrder,
        boolean active
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getSortOrder(),
                category.isActive()
        );
    }
}
