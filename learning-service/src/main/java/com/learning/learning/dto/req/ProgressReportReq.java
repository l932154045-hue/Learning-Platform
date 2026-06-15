package com.learning.learning.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProgressReportReq {
    @NotNull(message = "视频ID不能为空")
    private Long videoId;
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    @NotNull(message = "进度不能为空")
    private Integer progressSeconds;
    @NotNull(message = "视频时长不能为空")
    private Integer duration;
}
