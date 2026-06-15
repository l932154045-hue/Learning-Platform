package com.learning.course.dto.resp;

import lombok.Data;

@Data
public class ChapterVideoVO {
    private Long id;
    private String chapterTitle;
    private String videoTitle;
    private String videoUrl;
    private Integer duration;
    private Integer sortOrder;
}
