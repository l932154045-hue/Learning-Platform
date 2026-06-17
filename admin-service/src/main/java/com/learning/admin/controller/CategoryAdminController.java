package com.learning.admin.controller;

import com.learning.admin.service.CategoryAdminService;
import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    @PostMapping
    public R<Void> create(@RequestParam String name,
                           @RequestParam(defaultValue = "0") Long parentId,
                           @RequestParam(defaultValue = "0") Integer sortOrder,
                           @CurrentUser UserContext userContext) {
        categoryAdminService.createCategory(name, parentId, sortOrder, userContext.getRole());
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable("id") Long id,
                           @RequestParam String name,
                           @RequestParam Integer sortOrder,
                           @CurrentUser UserContext userContext) {
        categoryAdminService.updateCategory(id, name, sortOrder, userContext.getRole());
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id,
                           @CurrentUser UserContext userContext) {
        categoryAdminService.deleteCategory(id, userContext.getRole());
        return R.ok();
    }
}
