package com.learning.course.dto.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseListItemVO {
    private Long id;
    private String title;
    private String coverUrl;
    private String teacherName;
    private BigDecimal price;
    private Integer saleCount;
    private Long categoryId;
    private String categoryName;
    private Integer status;
}
