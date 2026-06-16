package com.learning.admin.mq.producer;

import com.learning.admin.mq.message.CategoryUpdatedMessage;
import com.learning.admin.mq.message.CourseUpdatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseEventProducer {
    private final RabbitTemplate rabbitTemplate;
    private static final String COURSE_TOPIC = "course.topic";
    private static final String RK_COURSE_UPDATED = "course.updated";

    public void sendCourseUpdated(CourseUpdatedMessage msg) {
        CorrelationData data = new CorrelationData(msg.getMessageId());
        rabbitTemplate.convertAndSend(COURSE_TOPIC, RK_COURSE_UPDATED, msg, data);
        log.info("发送课程变更消息: courseId={}, operation={}", msg.getCourseId(), msg.getOperation());
    }

    public void sendCategoryUpdated(CategoryUpdatedMessage msg) {
        CorrelationData data = new CorrelationData(msg.getMessageId());
        rabbitTemplate.convertAndSend(COURSE_TOPIC, RK_COURSE_UPDATED, msg, data);
        log.info("发送分类变更消息: categoryId={}, operation={}", msg.getCategoryId(), msg.getOperation());
    }
}
