package com.learning.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.common.core.dto.CourseFeignResp;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import com.learning.order.client.CourseClient;
import com.learning.order.dto.req.CreateOrderReq;
import com.learning.order.dto.resp.OrderDetailVO;
import com.learning.order.dto.resp.OrderListVO;
import com.learning.order.dto.resp.OrderSummaryVO;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderEventProducer orderEventProducer;
    private final CourseClient courseClient;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Long createOrder(Long userId, CreateOrderReq req) {
        // 检查是否已购买
        int alreadyPaid = orderItemMapper.countPaidByUserAndCourse(userId, req.getCourseId());
        if (alreadyPaid > 0) {
            throw new BizException(ResultCode.COURSE_ALREADY_PURCHASED);
        }

        // Fetch course info from course-service (outside transaction to avoid holding DB connection)
        CourseFeignResp course = fetchCourseInfo(req.getCourseId());

        // DB operations inside transaction
        return transactionTemplate.execute(status -> {
            // Generate order number
            String orderNo = generateOrderNo();

            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setTotalAmount(course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO);
            order.setStatus(OrderStatusEnum.PENDING.getCode());
            orderMapper.insert(order);

            // Create order item
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setCourseId(req.getCourseId());
            item.setCourseTitle(course.getTitle());
            item.setPrice(course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO);
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
        });
    }

    private CourseFeignResp fetchCourseInfo(Long courseId) {
        try {
            R<CourseFeignResp> result = courseClient.getCourseDetail(courseId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取课程信息失败: courseId={}", courseId, e);
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
    }

    @Override
    public OrderDetailVO getDetail(Long id, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!userId.equals(order.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN);
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
    public void cancel(Long id, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!userId.equals(order.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        if (!order.getStatus().equals(OrderStatusEnum.PENDING.getCode())) {
            throw new BizException(ResultCode.ORDER_PAID);
        }
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        orderMapper.updateById(order);
    }

    @Override
    public Long getOwnerUserId(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        return order.getUserId();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        order.setStatus(status);
        if (status.equals(OrderStatusEnum.PAID.getCode())) {
            order.setPaidAt(LocalDateTime.now());
        }
        orderMapper.updateById(order);
        log.info("订单状态更新: orderId={}, status={}", id, status);
    }

    @Override
    public Long getCourseId(Long orderId) {
        OrderItem item = orderItemMapper.selectOne(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        if (item == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        return item.getCourseId();
    }

    @Override
    public BigDecimal getTotalAmount(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        return order.getTotalAmount();
    }

    @Override
    public OrderSummaryVO getOrderSummary(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        OrderItem item = orderItemMapper.selectOne(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));

        OrderSummaryVO vo = new OrderSummaryVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCourseId(item != null ? item.getCourseId() : null);
        return vo;
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

    @Override
    public PageResp<OrderListVO> listAllOrders(PageReq req, String keyword, Integer status) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Order::getOrderNo, keyword);
        }
        if (status != null) {
            qw.eq(Order::getStatus, status);
        }
        Page<Order> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<Order> iPage = orderMapper.selectPage(page, qw);

        // Batch-fetch order items to avoid N+1
        List<Long> orderIds = iPage.getRecords().stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> allItems = orderIds.isEmpty() ? List.of()
                : orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
        Map<Long, OrderItem> itemMap = allItems.stream()
                .collect(Collectors.toMap(OrderItem::getOrderId, i -> i, (a, b) -> a));

        List<OrderListVO> list = iPage.getRecords().stream().map(order -> {
            OrderListVO vo = new OrderListVO();
            BeanUtils.copyProperties(order, vo);
            vo.setStatusDesc(getStatusDesc(order.getStatus()));

            OrderItem item = itemMap.get(order.getId());
            if (item != null) {
                vo.setCourseId(item.getCourseId());
                vo.setCourseTitle(item.getCourseTitle());
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResp.of(list, iPage.getTotal(), req.getPageNum(), req.getPageSize());
    }

    @Override
    public Long getOrderCount() {
        return orderMapper.selectCount(null);
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return orderMapper.selectTotalRevenue();
    }
}
