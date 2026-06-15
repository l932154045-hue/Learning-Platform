package com.learning.course.dto.resp;

import lombok.Data;

import java.util.List;

@Data
public class CourseCategoryVO {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private List<CourseCategoryVO> children;
}
