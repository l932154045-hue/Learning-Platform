package com.learning.course.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VideoSaveReq {
    @NotNull private Long courseId;
    @NotBlank private String chapterTitle;
    @NotBlank private String videoTitle;
    @NotBlank private String videoUrl;
    @NotNull private Integer duration;
    @NotNull private Integer sortOrder;
}
