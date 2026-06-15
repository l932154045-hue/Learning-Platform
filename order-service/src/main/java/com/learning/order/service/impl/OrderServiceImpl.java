package com.learning.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.ResultCode;
import com.learning.order.dto.req.CreateOrderReq;
import com.learning.order.dto.resp.OrderDetailVO;
import com.learning.order.entity.Order;
import com.learning.order.entity.OrderItem;
import com.learning.order.enums.OrderStatusEnum;
import com.learning.order.mapper.OrderItemMapper;
import com.learning.order.mapper.OrderMapper;
import com.learning.order.mq.message.OrderCreatedMessage;
import com.learning.order.mq.producer.OrderEventProducer;
import com.learning.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderEventProducer orderEventProducer;

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("99.00");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long userId, CreateOrderReq req) {
        // Generate order number
        String orderNo = generateOrderNo();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(DEFAULT_PRICE);
        order.setStatus(OrderStatusEnum.PENDING.getCode());
        orderMapper.insert(order);

        // Create order item
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setCourseId(req.getCourseId());
        item.setCourseTitle("课程-" + req.getCourseId());
        item.setPrice(DEFAULT_PRICE);
        orderItemMapper.insert(item);

        // Send MQ message after transaction commit
        Order finalOrder = order;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                OrderCreatedMessage message = new OrderCreatedMessage();
                message.setOrderId(finalOrder.getId());
                message.setOrderNo(finalOrder.getOrderNo());
                message.setUserId(finalOrder.getUserId());
                message.setCourseId(req.getCourseId());
                message.setAmount(finalOrder.getTotalAmount());
                orderEventProducer.sendOrderCreated(message);
            }
        });

        return order.getId();
    }

    @Override
    public OrderDetailVO getDetail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusDesc(getStatusDesc(order.getStatus()));

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        vo.setOrderItems(items);

        return vo;
    }

    @Override
    public List<OrderDetailVO> list(Long userId) {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreatedAt));

        return orders.stream().map(order -> {
            OrderDetailVO vo = new OrderDetailVO();
            BeanUtils.copyProperties(order, vo);
            vo.setStatusDesc(getStatusDesc(order.getStatus()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void cancel(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getStatus().equals(OrderStatusEnum.PENDING.getCode())) {
            throw new BizException(ResultCode.ORDER_PAID);
        }
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        orderMapper.updateById(order);
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return "ORD" + datePart + randomPart;
    }

    private String getStatusDesc(Integer status) {
        for (OrderStatusEnum e : OrderStatusEnum.values()) {
            if (e.getCode().equals(status)) {
                return e.getDesc();
            }
        }
        return "未知";
    }
}
