package com.learning.admin.service.impl;

import com.learning.admin.client.OrderServiceClient;
import com.learning.admin.service.AdminAuthService;
import com.learning.admin.service.OrderAdminService;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAdminServiceImpl implements OrderAdminService {
    private final AdminAuthService authService;
    private final OrderServiceClient orderServiceClient;

    @Override
    public PageResp<Map<String, Object>> listOrders(PageReq req, Integer role) {
        authService.checkAdmin(role);
        R<PageResp<Map<String, Object>>> result = orderServiceClient.listOrders(req);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        return result.getData();
    }

    @Override
    public void updateOrderStatus(Long orderId, Integer status, Integer role) {
        authService.checkAdmin(role);
        R<Void> result = orderServiceClient.updateStatus(orderId, status);
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        log.info("管理员更新订单状态: orderId={}, status={}", orderId, status);
    }

    @Override
    public Long getOrderCount(Integer role) {
        authService.checkAdmin(role);
        R<Long> result = orderServiceClient.getOrderCount();
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        return result.getData();
    }

    @Override
    public BigDecimal getTotalRevenue(Integer role) {
        authService.checkAdmin(role);
        R<BigDecimal> result = orderServiceClient.getTotalRevenue();
        if (result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        return result.getData();
    }
}
