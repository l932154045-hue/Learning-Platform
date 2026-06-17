package com.learning.common.mq.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单支付成功消息 — 由 payment-service 发送，learning-service/order-service 消费
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderPaidMessage extends BaseMessage {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long courseId;

    /** 支付金额 — 仅 payment-service 会填充，消费端 JSON 反序列化自动忽略多余字段 */
    private BigDecimal amount;

    public OrderPaidMessage(Long orderId, String orderNo, Long userId, Long courseId, BigDecimal amount) {
        super();
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.userId = userId;
        this.courseId = courseId;
        this.amount = amount;
        setEventType("order.paid");
    }
}
