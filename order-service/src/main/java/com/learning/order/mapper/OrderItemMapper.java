package com.learning.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learning.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    /** 检查用户是否已购买某课程（存在已支付订单） */
    @Select("SELECT COUNT(1) FROM order_item oi " +
            "JOIN `order` o ON o.id = oi.order_id " +
            "WHERE o.user_id = #{userId} AND oi.course_id = #{courseId} AND o.status = 1")
    int countPaidByUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
