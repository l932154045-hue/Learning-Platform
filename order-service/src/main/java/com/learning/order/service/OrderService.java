package com.learning.order.service;

import com.learning.order.dto.req.CreateOrderReq;
import com.learning.order.dto.resp.OrderDetailVO;
import com.learning.order.dto.resp.OrderSummaryVO;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    Long createOrder(Long userId, CreateOrderReq req);
    OrderDetailVO getDetail(Long id, Long userId);
    List<OrderDetailVO> list(Long userId);
    void cancel(Long id, Long userId);
    void updateStatus(Long id, Integer status);
    Long getOwnerUserId(Long id);
    Long getCourseId(Long orderId);
    BigDecimal getTotalAmount(Long orderId);
    OrderSummaryVO getOrderSummary(Long orderId);
}
