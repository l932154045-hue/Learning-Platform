package com.learning.learning.dto.resp;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MyCourseVO {
    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private String courseCover;
    private String teacherName;
    private BigDecimal price;
    private Integer totalProgress;
    private Integer videoCount;
    private Integer finishedVideoCount;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastLearnedAt;
}
