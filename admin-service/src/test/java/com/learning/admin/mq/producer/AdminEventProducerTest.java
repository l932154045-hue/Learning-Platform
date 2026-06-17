package com.learning.admin.mq.producer;

import com.learning.common.mq.message.CategoryUpdatedMessage;
import com.learning.common.mq.message.CourseUpdatedMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminEventProducer 单元测试")
class AdminEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private AdminEventProducer producer;

    @Test
    @DisplayName("发送课程更新消息")
    void shouldSendCourseUpdated() {
        CourseUpdatedMessage msg = new CourseUpdatedMessage(1L, 1);
        producer.sendCourseUpdated(msg);
        verify(rabbitTemplate).convertAndSend(
                eq("course.topic"), eq("course.updated"), eq(msg), any(CorrelationData.class));
    }

    @Test
    @DisplayName("发送分类更新消息")
    void shouldSendCategoryUpdated() {
        CategoryUpdatedMessage msg = new CategoryUpdatedMessage(1L, 1);
        producer.sendCategoryUpdated(msg);
        verify(rabbitTemplate).convertAndSend(
                eq("course.topic"), eq("course.updated"), eq(msg), any(CorrelationData.class));
    }
}
