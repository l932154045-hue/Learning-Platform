package com.learning.course.dto.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseDetailVO {
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String categoryName;
    private String teacherName;
    private BigDecimal price;
    private Integer saleCount;
    private Integer status;
    private List<ChapterVideoVO> chapters;
}
