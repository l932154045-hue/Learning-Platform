package com.learning.learning.dto.resp;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}
