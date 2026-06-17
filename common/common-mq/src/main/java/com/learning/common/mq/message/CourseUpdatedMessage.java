package com.learning.common.mq.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 课程更新消息 — admin-service 发送，course-service 消费刷新缓存
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CourseUpdatedMessage extends BaseMessage {

    /** 课程 ID */
    private Long courseId;

    /** 操作类型：1=更新，2=下架，3=删除 */
    private Integer operation;

    public CourseUpdatedMessage(Long courseId, Integer operation) {
        super();
        this.courseId = courseId;
        this.operation = operation;
        setEventType("course.updated");
    }
}
