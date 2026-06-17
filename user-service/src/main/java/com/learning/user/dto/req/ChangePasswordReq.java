package com.learning.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordReq {
    @NotBlank private String oldPassword;
    @NotBlank @Size(min = 6, message = "新密码不能少于6位") private String newPassword;
}
