package com.learning.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.ResultCode;
import com.learning.payment.client.OrderClient;
import com.learning.payment.dto.resp.PayResultVO;
import com.learning.payment.entity.PaymentRecord;
import com.learning.payment.enums.PayStatusEnum;
import com.learning.payment.mapper.PaymentRecordMapper;
import com.learning.payment.mq.message.OrderPaidMessage;
import com.learning.payment.mq.producer.PaymentEventProducer;
import com.learning.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String PAY_LOCK_KEY = "pay:lock:";
    private static final long PAY_LOCK_TTL = 60;
    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("99.00");

    private final PaymentRecordMapper paymentRecordMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderClient orderClient;
    private final PaymentEventProducer paymentEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayResultVO pay(Long userId, Long orderId) {
        // 1. Idempotent check: check if already paid
        PaymentRecord existRecord = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
                        .eq(PaymentRecord::getStatus, PayStatusEnum.SUCCESS.getCode()));
        if (existRecord != null) {
            log.info("订单已支付, 幂等返回: orderId={}", orderId);
            return buildPayResultVO(existRecord);
        }

        // 2. Redis distributed lock to prevent concurrent payment
        String lockKey = PAY_LOCK_KEY + orderId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", PAY_LOCK_TTL,
                TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BizException(ResultCode.DUPLICATE_PAY);
        }

        try {
            // 3. Insert payment record with PENDING status
            String paymentNo = generatePaymentNo();
            PaymentRecord record = new PaymentRecord();
            record.setPaymentNo(paymentNo);
            record.setOrderId(orderId);
            record.setOrderNo("ORD" + orderId);
            record.setUserId(userId);
            record.setAmount(DEFAULT_AMOUNT);
            record.setPayMethod("WECHAT");
            record.setStatus(PayStatusEnum.PENDING.getCode());
            paymentRecordMapper.insert(record);

            // 4. Simulate payment success
            record.setStatus(PayStatusEnum.SUCCESS.getCode());
            record.setPaidAt(LocalDateTime.now());
            paymentRecordMapper.updateById(record);

            log.info("模拟支付成功: paymentNo={}, orderId={}", paymentNo, orderId);

            // 5. Feign call order-service to update order status
            try {
                orderClient.updateStatus(orderId, 1);
                log.info("订单状态更新成功: orderId={}", orderId);
            } catch (Exception e) {
                log.error("Feign调用订单服务更新状态失败: orderId={}", orderId, e);
                throw new BizException(ResultCode.REMOTE_CALL_ERROR);
            }

            // 6. Send OrderPaidMessage to MQ
            OrderPaidMessage message = new OrderPaidMessage();
            message.setOrderId(orderId);
            message.setOrderNo(record.getOrderNo());
            message.setUserId(userId);
            message.setAmount(DEFAULT_AMOUNT);
            paymentEventProducer.sendOrderPaid(message);

            return buildPayResultVO(record);

        } finally {
            // Release distributed lock
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public PayResultVO queryResult(Long orderId, Long userId) {
        PaymentRecord record = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
                        .orderByDesc(PaymentRecord::getCreatedAt)
                        .last("LIMIT 1"));
        if (record == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!record.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return buildPayResultVO(record);
    }

    private PayResultVO buildPayResultVO(PaymentRecord record) {
        String statusDesc = getStatusDesc(record.getStatus());
        return PayResultVO.builder()
                .paymentNo(record.getPaymentNo())
                .orderNo(record.getOrderNo())
                .amount(record.getAmount())
                .status(record.getStatus())
                .statusDesc(statusDesc)
                .paidAt(record.getPaidAt())
                .build();
    }

    private String getStatusDesc(Integer status) {
        for (PayStatusEnum e : PayStatusEnum.values()) {
            if (e.getCode().equals(status)) {
                return e.getDesc();
            }
        }
        return "未知";
    }

    private String generatePaymentNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return "PAY" + datePart + randomPart;
    }
}
