package com.learning.user.dto.resp;

import lombok.Data;

@Data
public class UserInfoResp {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer role;
}
