package com.learning.cart.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddReq {
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
}
