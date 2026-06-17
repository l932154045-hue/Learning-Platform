package com.learning.course.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.common.mq.message.CategoryUpdatedMessage;
import com.learning.common.mq.message.CourseUpdatedMessage;
import com.learning.course.cache.CourseCacheService;
import com.learning.course.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 缓存刷新消费者 — 监听 course.updated / category.updated 消息，精确驱逐对应缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshConsumer {

    private final CourseCacheService courseCacheService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURSE_UPDATED_CACHE)
    public void handleCourseUpdated(Message message, Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            byte[] body = message.getBody();
            String eventType = extractEventType(body);

            if ("course.updated".equals(eventType)) {
                handleCourseUpdated(body);
            } else if ("category.updated".equals(eventType)) {
                handleCategoryUpdated(body);
            } else {
                log.warn("未知事件类型: {}, 执行全量刷新兜底", eventType);
                courseCacheService.refreshAllCaches();
            }
        } catch (Exception e) {
            log.error("缓存刷新失败，执行全量刷新兜底", e);
            courseCacheService.refreshAllCaches();
        } finally {
            channel.basicAck(tag, false);
        }
    }

    private void handleCourseUpdated(byte[] body) throws IOException {
        CourseUpdatedMessage msg = objectMapper.readValue(body, CourseUpdatedMessage.class);
        Long courseId = msg.getCourseId();
        Integer operation = msg.getOperation();
        log.info("课程缓存刷新: courseId={}, operation={}", courseId, operation);

        // 精确驱逐对应课程详情
        courseCacheService.evict(courseId);

        // 热门排名可能变化
        courseCacheService.evictHotTop10();

        // 删除课程时还需清除分类树（课程-分类关联变更）
        if (operation != null && operation == 3) {
            courseCacheService.evictCategoryTree();
        }
    }

    private void handleCategoryUpdated(byte[] body) throws IOException {
        CategoryUpdatedMessage msg = objectMapper.readValue(body, CategoryUpdatedMessage.class);
        log.info("分类缓存刷新: categoryId={}, operation={}", msg.getCategoryId(), msg.getOperation());
        courseCacheService.evictCategoryTree();
    }

    private String extractEventType(byte[] body) {
        // 从 JSON 中快速提取 eventType 字段，避免完整反序列化
        String json = new String(body, StandardCharsets.UTF_8);
        int idx = json.indexOf("\"eventType\"");
        if (idx < 0) return null;
        int colonIdx = json.indexOf(":", idx);
        int startQuote = json.indexOf("\"", colonIdx);
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (startQuote < 0 || endQuote < 0) return null;
        return json.substring(startQuote + 1, endQuote);
    }
}
