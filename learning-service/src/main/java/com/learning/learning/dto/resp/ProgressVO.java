package com.learning.learning.dto.resp;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProgressVO {
    private Long videoId;
    private Long courseId;
    private Integer progressSeconds;
    private Integer duration;
    private Boolean finished;
    private LocalDateTime updatedAt;
}
