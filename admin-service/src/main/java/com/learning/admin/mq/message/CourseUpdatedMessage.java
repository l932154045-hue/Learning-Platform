package com.learning.admin.mq.message;

import com.learning.common.mq.message.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseUpdatedMessage extends BaseMessage {
    private Long courseId;
    private Integer operation; // 1=更新 2=下架 3=删除

    public CourseUpdatedMessage() {
        setEventType("course.updated");
    }
}
