package com.learning.learning.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("enrollment")
public class Enrollment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long courseId;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime enrolledAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastLearnedAt;
}
