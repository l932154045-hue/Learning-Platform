package com.learning.order.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderReq {
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
}
