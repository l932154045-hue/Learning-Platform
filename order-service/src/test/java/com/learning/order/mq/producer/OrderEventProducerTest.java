package com.learning.order.mq.producer;

import com.learning.common.mq.message.OrderPaidMessage;
import com.learning.order.config.RabbitMQConfig;
import com.learning.order.mq.message.OrderCreatedMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.amqp.rabbit.connection.CorrelationData;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventProducer 单元测试")
class OrderEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private OrderEventProducer producer;

    @Test
    @DisplayName("发送订单创建消息")
    void shouldSendOrderCreatedMessage() {
        OrderCreatedMessage message = new OrderCreatedMessage();
        message.setOrderId(1L);
        message.setOrderNo("ORD001");
        message.setUserId(100L);
        message.setCourseId(200L);

        producer.sendOrderCreated(message);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ORDER_TOPIC),
                eq(RabbitMQConfig.RK_ORDER_CREATED),
                eq(message),
                any(CorrelationData.class));
    }

    @Test
    @DisplayName("发送订单支付成功消息")
    void shouldSendOrderPaidMessage() {
        OrderPaidMessage message = new OrderPaidMessage();
        message.setOrderId(1L);
        message.setOrderNo("ORD001");
        message.setUserId(100L);
        message.setCourseId(200L);

        producer.sendOrderPaid(message);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ORDER_TOPIC),
                eq(RabbitMQConfig.RK_ORDER_PAID),
                eq(message),
                any(CorrelationData.class));
    }
}
