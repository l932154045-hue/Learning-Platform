package com.learning.user.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserInfoReq {
    @Size(max = 32, message = "昵称最长32位")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 255, message = "头像URL最长255位")
    private String avatarUrl;
}
