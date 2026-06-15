package com.learning.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("chapter_video")
public class ChapterVideo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String chapterTitle;
    private String videoTitle;
    private String videoUrl;
    private Integer duration;
    private Integer sortOrder;
    private Integer status;
}
