package com.learning.cart.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long cartId;
    private Long courseId;
    private String courseTitle;
    private String coverUrl;
    private String teacherName;
    private BigDecimal price;
}
