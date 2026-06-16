package com.learning.user.dto.resp;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserListResp {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
