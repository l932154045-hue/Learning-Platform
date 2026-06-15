package com.learning.learning.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewReq {
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分至少为1")
    @Max(value = 5, message = "评分最多为5")
    private Integer rating;
    @NotBlank(message = "评价内容不能为空")
    private String content;
}
