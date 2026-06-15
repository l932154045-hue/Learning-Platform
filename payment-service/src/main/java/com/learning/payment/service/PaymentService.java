package com.learning.payment.service;

import com.learning.payment.dto.resp.PayResultVO;

public interface PaymentService {
    PayResultVO pay(Long userId, Long orderId);
    PayResultVO queryResult(Long orderId, Long userId);
}
