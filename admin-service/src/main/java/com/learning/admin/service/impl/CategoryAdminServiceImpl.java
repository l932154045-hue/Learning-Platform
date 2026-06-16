package com.learning.admin.service.impl;

import com.learning.admin.mq.message.CategoryUpdatedMessage;
import com.learning.admin.mq.producer.CourseEventProducer;
import com.learning.admin.service.AdminAuthService;
import com.learning.admin.service.CategoryAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryAdminServiceImpl implements CategoryAdminService {
    private final AdminAuthService authService;
    private final CourseEventProducer courseEventProducer;

    @Override
    public void createCategory(String name, Long parentId, Integer sortOrder, Integer role) {
        authService.checkAdmin(role);
        log.info("管理员创建分类: name={}, parentId={}", name, parentId);
        sendCategoryMsg(null, 1);
    }

    @Override
    public void updateCategory(Long id, String name, Integer sortOrder, Integer role) {
        authService.checkAdmin(role);
        log.info("管理员更新分类: id={}, name={}", id, name);
        sendCategoryMsg(id, 1);
    }

    @Override
    public void deleteCategory(Long id, Integer role) {
        authService.checkAdmin(role);
        log.info("管理员删除分类: id={}", id);
        sendCategoryMsg(id, 2);
    }

    private void sendCategoryMsg(Long categoryId, Integer operation) {
        CategoryUpdatedMessage msg = new CategoryUpdatedMessage();
        msg.setCategoryId(categoryId);
        msg.setOperation(operation);
        courseEventProducer.sendCategoryUpdated(msg);
    }
}
