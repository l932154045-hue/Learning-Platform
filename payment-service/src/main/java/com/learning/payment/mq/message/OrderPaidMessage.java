package com.learning.payment.mq.message;

import com.learning.common.mq.message.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPaidMessage extends BaseMessage {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long courseId;
    private BigDecimal amount;

    public OrderPaidMessage() {
        setEventType("order.paid");
    }
}
