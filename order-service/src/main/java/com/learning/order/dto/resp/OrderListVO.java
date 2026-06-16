package com.learning.order.dto.resp;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderListVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusDesc;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
