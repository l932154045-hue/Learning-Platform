package com.learning.order.mq.producer;

import com.learning.order.config.RabbitMQConfig;
import com.learning.order.mq.message.OrderCreatedMessage;
import com.learning.order.mq.message.OrderPaidMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOrderCreated(OrderCreatedMessage message) {
        CorrelationData correlationData = new CorrelationData(message.getMessageId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_TOPIC,
                RabbitMQConfig.RK_ORDER_CREATED,
                message,
                correlationData
        );
        log.info("发送订单创建消息: orderId={}, orderNo={}", message.getOrderId(), message.getOrderNo());
    }

    public void sendOrderPaid(OrderPaidMessage message) {
        CorrelationData correlationData = new CorrelationData(message.getMessageId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_TOPIC,
                RabbitMQConfig.RK_ORDER_PAID,
                message,
                correlationData
        );
        log.info("发送订单支付消息: orderId={}, orderNo={}", message.getOrderId(), message.getOrderNo());
    }
}
