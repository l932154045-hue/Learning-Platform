package com.learning.payment.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayResultVO {
    private String paymentNo;
    private String orderNo;
    private BigDecimal amount;
    private Integer status;
    private String statusDesc;
    private LocalDateTime paidAt;
}
