package com.campushub.category;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping
    public List<AdminCategoryResponse> categories() {
        return adminCategoryService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCategoryResponse create(@Valid @RequestBody AdminCategoryRequest request) {
        return adminCategoryService.create(request);
    }

    @PutMapping("/{id}")
    public AdminCategoryResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AdminCategoryRequest request
    ) {
        return adminCategoryService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    public AdminCategoryResponse changeActive(
            @PathVariable Long id,
            @RequestBody CategoryActiveRequest request
    ) {
        return adminCategoryService.changeActive(id, request.active());
    }
}
