package com.learning.course.mq.consumer;

import com.learning.course.cache.CourseCacheService;
import com.learning.course.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshConsumer {

    private final CourseCacheService courseCacheService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURSE_UPDATED_CACHE)
    public void handleCourseUpdated(@Payload Object msg,
                                     Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            String eventType = "unknown";
            if (msg instanceof Map) {
                Object et = ((Map<?, ?>) msg).get("eventType");
                if (et != null) eventType = et.toString();
            }
            log.info("收到缓存刷新消息: eventType={}", eventType);

            // TODO: 后续可根据 eventType 做差异化清除
            // 目前安全策略：全量清除，保证数据一致性
            courseCacheService.refreshAllCaches();

            channel.basicAck(tag, false);
            log.info("缓存刷新完成");
        } catch (Exception e) {
            // 缓存操作幂等，失败直接丢弃不重试，管理后台可手动刷新
            log.error("缓存刷新失败，已丢弃消息", e);
            channel.basicAck(tag, false);
        }
    }
}
