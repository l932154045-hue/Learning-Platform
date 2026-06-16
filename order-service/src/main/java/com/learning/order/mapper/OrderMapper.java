package com.learning.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learning.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM `order` WHERE status = 1")
    BigDecimal selectTotalRevenue();
}
