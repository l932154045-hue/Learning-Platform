package com.learning.payment.service.impl;

import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import com.learning.payment.client.OrderClient;
import com.learning.payment.dto.resp.OrderSummaryVO;
import com.learning.payment.entity.PaymentRecord;
import com.learning.payment.mapper.PaymentRecordMapper;
import com.learning.payment.mq.producer.PaymentEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl 单元测试")
class PaymentServiceImplTest {

    @Mock
    private PaymentRecordMapper paymentRecordMapper;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private OrderClient orderClient;
    @Mock
    private PaymentEventProducer paymentEventProducer;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("支付 — 订单不属于当前用户抛异常")
    void shouldThrowExceptionWhenNotOwner() {
        OrderSummaryVO summary = new OrderSummaryVO();
        summary.setOrderId(1L);
        summary.setUserId(2L);
        summary.setCourseId(100L);
        summary.setTotalAmount(BigDecimal.TEN);
        when(orderClient.getOrderSummary(1L)).thenReturn(R.ok(summary));

        assertThrows(BizException.class, () -> paymentService.pay(1L, 1L));
    }

    @Test
    @DisplayName("支付 — 已有成功支付记录则幂等返回")
    void shouldThrowExceptionWhenAlreadyPaid() {
        OrderSummaryVO summary = new OrderSummaryVO();
        summary.setOrderId(1L);
        summary.setUserId(1L);
        summary.setCourseId(100L);
        summary.setTotalAmount(BigDecimal.TEN);
        when(orderClient.getOrderSummary(1L)).thenReturn(R.ok(summary));

        PaymentRecord existing = new PaymentRecord();
        existing.setId(10L);
        existing.setOrderId(1L);
        // PaymentRecord with SUCCESS status found
        when(paymentRecordMapper.selectOne(any())).thenReturn(existing);

        // Should not throw, returns existing result (no distributed lock needed)
        assertDoesNotThrow(() -> paymentService.pay(1L, 1L));
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("支付 — 获取分布式锁失败（并发重复支付）抛异常")
    void shouldThrowExceptionWhenLockFailed() {
        OrderSummaryVO summary = new OrderSummaryVO();
        summary.setOrderId(1L);
        summary.setUserId(1L);
        summary.setCourseId(100L);
        summary.setTotalAmount(BigDecimal.TEN);
        when(orderClient.getOrderSummary(1L)).thenReturn(R.ok(summary));

        // No existing successful payment record
        when(paymentRecordMapper.selectOne(any())).thenReturn(null);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        assertThrows(BizException.class, () -> paymentService.pay(1L, 1L));
    }
}
