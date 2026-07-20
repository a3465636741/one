package com.campushub.category;

import com.campushub.event.EventRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public AdminCategoryService(
            CategoryRepository categoryRepository,
            EventRepository eventRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryResponse> findAll() {
        return categoryRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(category -> AdminCategoryResponse.from(
                        category,
                        eventRepository.countByCategoryId(category.getId())
                ))
                .toList();
    }

    public AdminCategoryResponse create(AdminCategoryRequest request) {
        String name = request.name().trim();
        String slug = request.slug().trim();
        validateUnique(name, slug, null);
        Category category = categoryRepository.save(new Category(
                name,
                slug,
                request.sortOrder(),
                request.active()
        ));
        return AdminCategoryResponse.from(category, 0);
    }

    public AdminCategoryResponse update(Long id, AdminCategoryRequest request) {
        Category category = requireCategory(id);
        String name = request.name().trim();
        String slug = request.slug().trim();
        validateUnique(name, slug, id);
        category.update(name, slug, request.sortOrder(), request.active());
        return AdminCategoryResponse.from(
                category,
                eventRepository.countByCategoryId(category.getId())
        );
    }

    public AdminCategoryResponse changeActive(Long id, boolean active) {
        Category category = requireCategory(id);
        category.changeActive(active);
        return AdminCategoryResponse.from(
                category,
                eventRepository.countByCategoryId(category.getId())
        );
    }

    private Category requireCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "活动分类不存在"
                ));
    }

    private void validateUnique(String name, String slug, Long currentId) {
        boolean duplicateName = currentId == null
                ? categoryRepository.existsByNameIgnoreCase(name)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(name, currentId);
        if (duplicateName) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "分类名称已经存在");
        }
        boolean duplicateSlug = currentId == null
                ? categoryRepository.existsBySlugIgnoreCase(slug)
                : categoryRepository.existsBySlugIgnoreCaseAndIdNot(slug, currentId);
        if (duplicateSlug) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "分类标识已经存在");
        }
    }
}
