package com.learning.order.mq.consumer;

import com.learning.order.entity.Order;
import com.learning.order.enums.OrderStatusEnum;
import com.learning.order.mapper.OrderMapper;
import com.learning.order.mq.message.OrderCreatedMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final OrderMapper orderMapper;

    @RabbitListener(queues = "order.timeout.cancel")
    public void cancelTimeoutOrder(OrderCreatedMessage msg,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("处理超时订单取消: orderId={}, orderNo={}", msg.getOrderId(), msg.getOrderNo());
            Order order = orderMapper.selectById(msg.getOrderId());
            if (order != null && order.getStatus().equals(OrderStatusEnum.PENDING.getCode())) {
                order.setStatus(OrderStatusEnum.CANCELLED.getCode());
                orderMapper.updateById(order);
                log.info("超时订单已取消: orderId={}", msg.getOrderId());
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("处理超时订单取消失败: orderId={}", msg.getOrderId(), e);
            channel.basicNack(tag, false, true);
        }
    }
}
