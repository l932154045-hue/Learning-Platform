package com.learning.payment.mq.producer;

import com.learning.common.mq.message.OrderPaidMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    public static final String ORDER_TOPIC = "order.topic";
    public static final String RK_ORDER_PAID = "order.paid";

    private final RabbitTemplate rabbitTemplate;

    public void sendOrderPaid(OrderPaidMessage message) {
        CorrelationData correlationData = new CorrelationData(message.getMessageId());
        rabbitTemplate.convertAndSend(
                ORDER_TOPIC,
                RK_ORDER_PAID,
                message,
                correlationData
        );
        log.info("发送订单支付消息: orderId={}, orderNo={}", message.getOrderId(), message.getOrderNo());
    }
}
