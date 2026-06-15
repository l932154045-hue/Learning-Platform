package com.learning.admin.service;

import jakarta.validation.constraints.NotBlank;

public interface CategoryAdminService {
    void createCategory(@NotBlank String name, Long parentId, Integer sortOrder, Integer role);
    void updateCategory(Long id, @NotBlank String name, Integer sortOrder, Integer role);
    void deleteCategory(Long id, Integer role);
}
