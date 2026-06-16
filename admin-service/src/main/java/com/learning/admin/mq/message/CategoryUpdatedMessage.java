package com.learning.admin.mq.message;

import com.learning.common.mq.message.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryUpdatedMessage extends BaseMessage {
    private Long categoryId;
    private Integer operation; // 1=创建/更新 2=删除

    public CategoryUpdatedMessage() {
        setEventType("category.updated");
    }
}
