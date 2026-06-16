package com.learning.course.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseSaveReq {
    @NotBlank private String title;
    private String description;
    private String coverUrl;
    @NotNull private Long categoryId;
    private String teacherName;
    @NotNull private BigDecimal price;
}
