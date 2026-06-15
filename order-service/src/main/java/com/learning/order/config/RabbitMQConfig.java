package com.learning.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_TOPIC = "order.topic";
    public static final String ORDER_DLX = "order.dlx";
    public static final String QUEUE_ORDER_CREATED_COURSE = "order.created.course";
    public static final String QUEUE_ORDER_CREATED_NOTIFY = "order.created.notify";
    public static final String QUEUE_ORDER_PAYMENT_DELAY = "order.payment.delay";
    public static final String QUEUE_ORDER_TIMEOUT_CANCEL = "order.timeout.cancel";
    public static final String QUEUE_ORDER_PAID_ENROLLMENT = "order.paid.enrollment";
    public static final String RK_ORDER_CREATED = "order.created";
    public static final String RK_ORDER_PAID = "order.paid";
    public static final String RK_ORDER_TIMEOUT = "order.timeout";

    @Bean
    public TopicExchange orderTopicExchange() {
        return new TopicExchange(ORDER_TOPIC);
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX);
    }

    @Bean
    public Queue courseQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_CREATED_COURSE).build();
    }

    @Bean
    public Queue notifyQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_CREATED_NOTIFY).build();
    }

    @Bean
    public Queue paymentDelayQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_PAYMENT_DELAY)
                .ttl(30 * 60 * 1000)
                .deadLetterExchange(ORDER_DLX)
                .deadLetterRoutingKey(RK_ORDER_TIMEOUT)
                .build();
    }

    @Bean
    public Queue timeoutCancelQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_TIMEOUT_CANCEL).build();
    }

    @Bean
    public Queue paidEnrollmentQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_PAID_ENROLLMENT).build();
    }

    @Bean
    public Binding courseQueueBinding() {
        return BindingBuilder.bind(courseQueue()).to(orderTopicExchange()).with(RK_ORDER_CREATED);
    }

    @Bean
    public Binding notifyQueueBinding() {
        return BindingBuilder.bind(notifyQueue()).to(orderTopicExchange()).with(RK_ORDER_CREATED);
    }

    @Bean
    public Binding delayQueueBinding() {
        return BindingBuilder.bind(paymentDelayQueue()).to(orderTopicExchange()).with(RK_ORDER_CREATED);
    }

    @Bean
    public Binding timeoutCancelBinding() {
        return BindingBuilder.bind(timeoutCancelQueue()).to(orderDlxExchange()).with(RK_ORDER_TIMEOUT);
    }

    @Bean
    public Binding paidEnrollmentBinding() {
        return BindingBuilder.bind(paidEnrollmentQueue()).to(orderTopicExchange()).with(RK_ORDER_PAID);
    }
}
