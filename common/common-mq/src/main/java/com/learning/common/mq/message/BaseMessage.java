package com.learning.common.mq.message;

import lombok.Data;
import java.util.UUID;

@Data
public abstract class BaseMessage {
    private String messageId = UUID.randomUUID().toString().replace("-", "");
    private Long timestamp = System.currentTimeMillis();
    private String eventType;
    private Integer retryCount = 0;
}
