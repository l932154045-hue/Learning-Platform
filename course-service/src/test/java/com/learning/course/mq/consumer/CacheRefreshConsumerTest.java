package com.learning.course.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.common.mq.message.CategoryUpdatedMessage;
import com.learning.common.mq.message.CourseUpdatedMessage;
import com.learning.course.cache.CourseCacheService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheRefreshConsumer 单元测试")
class CacheRefreshConsumerTest {

    @Mock
    private CourseCacheService courseCacheService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private Channel channel;
    @InjectMocks
    private CacheRefreshConsumer consumer;

    @Test
    @DisplayName("course.updated 更新操作 — 精确驱逐 + 热门")
    void shouldEvictSingleCourseAndHotOnUpdate() throws Exception {
        CourseUpdatedMessage msg = new CourseUpdatedMessage(42L, 1);
        byte[] body = objectMapper.writeValueAsBytes(msg);
        Message amqpMsg = new Message(body, new MessageProperties());
        consumer.handleCourseUpdated(amqpMsg, channel, 1L);
        verify(courseCacheService).evict(42L);
        verify(courseCacheService).evictHotTop10();
        verify(courseCacheService, never()).evictCategoryTree();
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("course.updated 删除操作 — 额外清除分类树")
    void shouldEvictCategoryTreeOnDelete() throws Exception {
        CourseUpdatedMessage msg = new CourseUpdatedMessage(42L, 3);
        byte[] body = objectMapper.writeValueAsBytes(msg);
        Message amqpMsg = new Message(body, new MessageProperties());
        consumer.handleCourseUpdated(amqpMsg, channel, 1L);
        verify(courseCacheService).evict(42L);
        verify(courseCacheService).evictHotTop10();
        verify(courseCacheService).evictCategoryTree();
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("category.updated — 仅清除分类树")
    void shouldEvictCategoryTreeOnly() throws Exception {
        CategoryUpdatedMessage msg = new CategoryUpdatedMessage(5L, 1);
        byte[] body = objectMapper.writeValueAsBytes(msg);
        Message amqpMsg = new Message(body, new MessageProperties());
        consumer.handleCourseUpdated(amqpMsg, channel, 1L);
        verify(courseCacheService).evictCategoryTree();
        verify(courseCacheService, never()).evict(anyLong());
        verify(courseCacheService, never()).evictHotTop10();
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("未知事件类型 — 全量刷新兜底")
    void shouldFullRefreshOnUnknownEventType() throws Exception {
        String unknownBody = "{\"eventType\":\"unknown.event\",\"data\":\"test\"}";
        Message amqpMsg = new Message(unknownBody.getBytes(), new MessageProperties());
        consumer.handleCourseUpdated(amqpMsg, channel, 1L);
        verify(courseCacheService).refreshAllCaches();
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("无效 JSON — 全量刷新兜底 + ACK")
    void shouldFullRefreshAndAckOnBadJson() throws Exception {
        String badBody = "not valid json at all {{{";
        Message amqpMsg = new Message(badBody.getBytes(), new MessageProperties());
        consumer.handleCourseUpdated(amqpMsg, channel, 1L);
        verify(courseCacheService).refreshAllCaches();
        verify(channel).basicAck(1L, false);
    }
}
