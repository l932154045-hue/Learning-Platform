package com.learning.admin.service;

import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;

import java.math.BigDecimal;
import java.util.Map;

public interface OrderAdminService {
    PageResp<Map<String, Object>> listOrders(PageReq req, Integer role);
    void updateOrderStatus(Long orderId, Integer status, Integer role);
    Long getOrderCount(Integer role);
    BigDecimal getTotalRevenue(Integer role);
}
