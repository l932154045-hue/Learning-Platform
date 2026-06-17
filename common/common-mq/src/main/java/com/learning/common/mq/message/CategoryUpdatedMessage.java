package com.learning.common.mq.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 分类更新消息 — admin-service 发送，course-service 消费刷新分类缓存
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CategoryUpdatedMessage extends BaseMessage {

    /** 分类 ID */
    private Long categoryId;

    /** 操作类型：1=创建/更新，2=删除 */
    private Integer operation;

    public CategoryUpdatedMessage(Long categoryId, Integer operation) {
        super();
        this.categoryId = categoryId;
        this.operation = operation;
        setEventType("category.updated");
    }
}
