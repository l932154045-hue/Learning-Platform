package com.learning.order.service.impl;

import com.learning.common.core.exception.BizException;
import com.learning.order.client.CourseClient;
import com.learning.order.entity.Order;
import com.learning.order.mapper.OrderItemMapper;
import com.learning.order.mapper.OrderMapper;
import com.learning.order.mq.producer.OrderEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl 单元测试")
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderEventProducer orderEventProducer;
    @Mock
    private CourseClient courseClient;
    @Mock
    private TransactionTemplate transactionTemplate;
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("查询详情 — 订单不存在抛异常")
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> orderService.getDetail(999L, 1L));
    }

    @Test
    @DisplayName("查询详情 — 非本人订单抛异常")
    void shouldThrowExceptionWhenNotOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(2L);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BizException.class, () -> orderService.getDetail(1L, 1L));
    }

    @Test
    @DisplayName("取消订单 — 订单不存在抛异常")
    void shouldThrowExceptionWhenCancelNotFound() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> orderService.cancel(999L, 1L));
    }

    @Test
    @DisplayName("取消订单 — 非本人订单抛异常")
    void shouldThrowExceptionWhenCancelNotOwn() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(2L);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BizException.class, () -> orderService.cancel(1L, 1L));
    }

    @Test
    @DisplayName("获取订单归属用户 ID")
    void shouldReturnOwnerUserId() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(100L);
        when(orderMapper.selectById(1L)).thenReturn(order);

        Long ownerId = orderService.getOwnerUserId(1L);

        assertEquals(100L, ownerId);
    }

    @Test
    @DisplayName("获取订单归属 — 订单不存在抛异常")
    void shouldThrowExceptionWhenGetOwnerNotFound() {
        when(orderMapper.selectById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> orderService.getOwnerUserId(999L));
    }
}
