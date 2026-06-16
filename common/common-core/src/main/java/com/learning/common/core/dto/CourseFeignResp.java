package com.learning.common.core.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseFeignResp {
    private Long id;
    private String title;
    private String coverUrl;
    private String teacherName;
    private BigDecimal price;
}
