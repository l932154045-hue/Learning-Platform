package com.learning.payment.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import com.learning.payment.client.OrderClient;
import com.learning.payment.dto.resp.OrderSummaryVO;
import com.learning.payment.dto.resp.PayResultVO;
import com.learning.payment.entity.PaymentRecord;
import com.learning.payment.enums.PayStatusEnum;
import com.learning.payment.mapper.PaymentRecordMapper;
import com.learning.common.mq.message.OrderPaidMessage;
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

    private final PaymentRecordMapper paymentRecordMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderClient orderClient;
    private final PaymentEventProducer paymentEventProducer;

    @SentinelResource(
            value = "pay",
            blockHandler = "payBlock",
            fallback = "payFallback"
    )
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayResultVO pay(Long userId, Long orderId) {
        // 1. Fetch order summary (owner + amount + courseId + orderNo in one call)
        OrderSummaryVO orderSummary;
        try {
            R<OrderSummaryVO> result = orderClient.getOrderSummary(orderId);
            if (result == null || result.getData() == null) {
                throw new BizException(ResultCode.ORDER_NOT_FOUND);
            }
            orderSummary = result.getData();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取订单摘要失败: orderId={}", orderId, e);
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }

        // 2. Verify order ownership
        if (!orderSummary.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }

        // 3. Validate courseId
        if (orderSummary.getCourseId() == null) {
            log.error("订单缺少课程ID: orderId={}", orderId);
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }

        // 4. Idempotent check: check if already paid
        PaymentRecord existRecord = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
                        .eq(PaymentRecord::getStatus, PayStatusEnum.SUCCESS.getCode()));
        if (existRecord != null) {
            log.info("订单已支付, 幂等返回: orderId={}", orderId);
            return buildPayResultVO(existRecord);
        }

        // 5. Redis distributed lock to prevent concurrent payment
        String lockKey = PAY_LOCK_KEY + orderId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", PAY_LOCK_TTL,
                TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BizException(ResultCode.DUPLICATE_PAY);
        }

        try {
            // 6. Insert payment record with PENDING status
            BigDecimal orderAmount = orderSummary.getTotalAmount() != null
                    ? orderSummary.getTotalAmount() : BigDecimal.ZERO;
            String paymentNo = generatePaymentNo();
            PaymentRecord record = new PaymentRecord();
            record.setPaymentNo(paymentNo);
            record.setOrderId(orderId);
            record.setOrderNo(orderSummary.getOrderNo());
            record.setUserId(userId);
            record.setAmount(orderAmount);
            record.setPayMethod("WECHAT");
            record.setStatus(PayStatusEnum.PENDING.getCode());
            paymentRecordMapper.insert(record);

            // 7. Simulate payment success
            record.setStatus(PayStatusEnum.SUCCESS.getCode());
            record.setPaidAt(LocalDateTime.now());
            paymentRecordMapper.updateById(record);

            log.info("模拟支付成功: paymentNo={}, orderId={}, amount={}", paymentNo, orderId, orderAmount);

            // 8. Feign call order-service to update order status
            try {
                orderClient.updateStatus(orderId, 1);
                log.info("订单状态更新成功: orderId={}", orderId);
            } catch (Exception e) {
                log.error("Feign调用订单服务更新状态失败: orderId={}", orderId, e);
                throw new BizException(ResultCode.REMOTE_CALL_ERROR);
            }

            // 9. Send OrderPaidMessage to MQ
            OrderPaidMessage message = new OrderPaidMessage();
            message.setOrderId(orderId);
            message.setOrderNo(orderSummary.getOrderNo());
            message.setUserId(userId);
            message.setCourseId(orderSummary.getCourseId());
            message.setAmount(orderAmount);
            paymentEventProducer.sendOrderPaid(message);

            return buildPayResultVO(record);

        } finally {
            // Release distributed lock
            redisTemplate.delete(lockKey);
        }
    }

    /** Sentinel 限流处理 */
    public PayResultVO payBlock(Long userId, Long orderId, BlockException ex) {
        log.warn("[Sentinel] pay 限流 userId={}", userId);
        throw new BizException(ResultCode.REMOTE_CALL_ERROR);
    }

    /** Sentinel fallback */
    public PayResultVO payFallback(Long userId, Long orderId, Throwable t) {
        log.error("[Sentinel] pay fallback userId={}", userId, t);
        throw new BizException(ResultCode.SYSTEM_ERROR);
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
