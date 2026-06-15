package com.learning.order.dto.resp;

import com.learning.order.entity.OrderItem;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusDesc;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private List<OrderItem> orderItems;
}
