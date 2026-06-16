package com.learning.course.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String COURSE_TOPIC = "course.topic";
    public static final String QUEUE_COURSE_UPDATED_CACHE = "course.updated.cache";
    public static final String RK_COURSE_UPDATED = "course.updated";

    @Bean
    public TopicExchange courseTopicExchange() {
        return new TopicExchange(COURSE_TOPIC);
    }

    @Bean
    public Queue courseUpdatedCacheQueue() {
        return QueueBuilder.durable(QUEUE_COURSE_UPDATED_CACHE).build();
    }

    @Bean
    public Binding courseUpdatedCacheBinding() {
        return BindingBuilder.bind(courseUpdatedCacheQueue())
                .to(courseTopicExchange())
                .with(RK_COURSE_UPDATED);
    }
}
