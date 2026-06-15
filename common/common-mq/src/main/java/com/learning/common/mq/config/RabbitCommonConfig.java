package com.learning.common.mq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitCommonConfig {

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory,
                                          Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) return;
            if (ack) {
                log.info("消息发送成功: id={}", correlationData.getId());
            } else {
                log.error("消息发送失败: id={}, cause={}", correlationData.getId(), cause);
            }
        });

        template.setReturnsCallback(returned -> {
            log.error("消息路由失败: exchange={}, routingKey={}, replyCode={}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode());
        });

        return template;
    }
}
