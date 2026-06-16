package com.learning.admin.service.impl;

import com.learning.admin.client.CourseServiceClient;
import com.learning.admin.mq.message.CategoryUpdatedMessage;
import com.learning.admin.mq.producer.AdminEventProducer;
import com.learning.admin.service.AdminAuthService;
import com.learning.admin.service.CategoryAdminService;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryAdminServiceImpl implements CategoryAdminService {
    private final AdminAuthService authService;
    private final AdminEventProducer eventProducer;
    private final CourseServiceClient courseServiceClient;

    @Override
    public void createCategory(String name, Long parentId, Integer sortOrder, Integer role) {
        authService.checkAdmin(role);
        R<Long> result = courseServiceClient.createCategory(name, parentId, sortOrder);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        sendCategoryMsg(result.getData(), 1);
        log.info("管理员创建分类成功: name={}, parentId={}, id={}", name, parentId, result.getData());
    }

    @Override
    public void updateCategory(Long id, String name, Integer sortOrder, Integer role) {
        authService.checkAdmin(role);
        R<Void> result = courseServiceClient.updateCategory(id, name, sortOrder);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        sendCategoryMsg(id, 1);
        log.info("管理员更新分类成功: id={}, name={}", id, name);
    }

    @Override
    public void deleteCategory(Long id, Integer role) {
        authService.checkAdmin(role);
        R<Void> result = courseServiceClient.deleteCategory(id);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        sendCategoryMsg(id, 2);
        log.info("管理员删除分类成功: id={}", id);
    }

    private void sendCategoryMsg(Long categoryId, Integer operation) {
        CategoryUpdatedMessage msg = new CategoryUpdatedMessage();
        msg.setCategoryId(categoryId);
        msg.setOperation(operation);
        eventProducer.sendCategoryUpdated(msg);
    }
}
