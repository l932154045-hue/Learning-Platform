package com.learning.learning.mq.consumer;

import com.learning.common.mq.message.OrderPaidMessage;
import com.learning.learning.service.EnrollmentService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentConsumer 单元测试")
class EnrollmentConsumerTest {

    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private Channel channel;
    @InjectMocks
    private EnrollmentConsumer consumer;

    @Test
    @DisplayName("幂等消费 — SETNX 成功则报名")
    void shouldEnrollWhenNotProcessed() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("enroll:lock:1"), eq("1"), eq(Duration.ofHours(72)))).thenReturn(true);

        OrderPaidMessage msg = new OrderPaidMessage(1L, "ORD001", 100L, 200L, BigDecimal.TEN);
        consumer.handleOrderPaid(msg, channel, 1L);

        verify(enrollmentService).enroll(100L, 200L);
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("幂等消费 — SETNX 失败跳过")
    void shouldSkipWhenAlreadyProcessed() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        OrderPaidMessage msg = new OrderPaidMessage(1L, "ORD001", 100L, 200L, BigDecimal.TEN);
        consumer.handleOrderPaid(msg, channel, 1L);

        verify(enrollmentService, never()).enroll(anyLong(), anyLong());
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("缺少必要字段 — 丢弃消息并 ACK")
    void shouldDiscardWhenMissingFields() throws Exception {
        OrderPaidMessage msg = new OrderPaidMessage();
        msg.setOrderId(2L);
        // userId and courseId are null

        consumer.handleOrderPaid(msg, channel, 2L);

        verify(enrollmentService, never()).enroll(anyLong(), anyLong());
        verify(channel).basicAck(2L, false);
    }
}
