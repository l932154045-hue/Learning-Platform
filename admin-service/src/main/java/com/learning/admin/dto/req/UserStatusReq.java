package com.learning.admin.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusReq {
    @NotNull private Integer status;
}
