package com.campushub.category;

public record AdminCategoryResponse(
        Long id,
        String name,
        String slug,
        int sortOrder,
        boolean active,
        long eventCount
) {
    public static AdminCategoryResponse from(Category category, long eventCount) {
        return new AdminCategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getSortOrder(),
                category.isActive(),
                eventCount
        );
    }
}
