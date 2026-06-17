package com.learning.learning.mq.consumer;

import com.learning.common.core.exception.BizException;
import com.learning.common.mq.message.OrderPaidMessage;
import com.learning.learning.service.EnrollmentService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentConsumer {

    private final EnrollmentService enrollmentService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_KEY_PREFIX = "enroll:lock:";
    private static final int LOCK_TTL_HOURS = 72;

    @RabbitListener(queues = "order.paid.enrollment")
    public void handleOrderPaid(OrderPaidMessage msg,
                                 Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("收到选课消息: orderId={}, userId={}, courseId={}", msg.getOrderId(), msg.getUserId(), msg.getCourseId());

            // Validate required fields — malformed messages are discarded
            if (msg.getUserId() == null || msg.getCourseId() == null) {
                log.error("选课消息缺少必要字段，已丢弃: orderId={}, userId={}, courseId={}",
                        msg.getOrderId(), msg.getUserId(), msg.getCourseId());
                channel.basicAck(tag, false);
                return;
            }

            // Idempotent check with Redis SETNX
            String lockKey = LOCK_KEY_PREFIX + msg.getOrderId();
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofHours(LOCK_TTL_HOURS));
            if (Boolean.FALSE.equals(locked)) {
                log.info("重复消息，已忽略: orderId={}", msg.getOrderId());
                channel.basicAck(tag, false);
                return;
            }

            // Execute enrollment
            enrollmentService.enroll(msg.getUserId(), msg.getCourseId());

            // Manual ACK
            channel.basicAck(tag, false);
            log.info("选课消息处理完成: orderId={}", msg.getOrderId());
        } catch (BizException e) {
            // Business errors (e.g. already enrolled) are not retriable — discard
            log.warn("选课消息业务异常，已丢弃: orderId={}, userId={}, courseId={}, error={}",
                    msg.getOrderId(), msg.getUserId(), msg.getCourseId(), e.getMessage());
            String lockKey = LOCK_KEY_PREFIX + msg.getOrderId();
            redisTemplate.delete(lockKey);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            // Transient errors (DB down, network) — requeue for retry
            log.error("选课消息处理失败，重新入队: orderId={}, userId={}, courseId={}",
                    msg.getOrderId(), msg.getUserId(), msg.getCourseId(), e);
            String lockKey = LOCK_KEY_PREFIX + msg.getOrderId();
            redisTemplate.delete(lockKey);
            channel.basicNack(tag, false, true);
        }
    }
}
