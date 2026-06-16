package com.learning.payment.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSummaryVO {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long courseId;
    private BigDecimal totalAmount;
    private Integer status;
}
