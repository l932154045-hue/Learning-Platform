package com.learning.order.mq.message;

import com.learning.common.mq.message.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPaidMessage extends BaseMessage {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long courseId;

    public OrderPaidMessage() {
        setEventType("order.paid");
    }
}
