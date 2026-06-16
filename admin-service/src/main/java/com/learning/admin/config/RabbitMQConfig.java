package com.learning.admin.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String COURSE_TOPIC = "course.topic";

    @Bean
    public TopicExchange courseTopicExchange() {
        return new TopicExchange(COURSE_TOPIC);
    }
}
