package com.learning.course.mq.consumer;

import com.learning.course.cache.CourseCacheService;
import com.learning.course.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshConsumer {

    private final CourseCacheService courseCacheService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CATEGORY_TREE_KEY = "course:category:tree";
    private static final String HOT_TOP10_KEY = "course:hot:top10";

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURSE_UPDATED_CACHE)
    public void handleCourseUpdated(Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("收到缓存刷新消息，清除课程详情缓存...");
            courseCacheService.evictAllCourseDetail();
            redisTemplate.delete(HOT_TOP10_KEY);
            redisTemplate.delete(CATEGORY_TREE_KEY);
            log.info("缓存刷新完成: 课程详情 + 热榜 + 分类树");
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("缓存刷新失败", e);
            channel.basicNack(tag, false, true);
        }
    }
}
