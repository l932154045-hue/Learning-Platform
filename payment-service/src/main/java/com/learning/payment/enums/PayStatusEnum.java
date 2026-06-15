package com.learning.payment.enums;

import lombok.Getter;

@Getter
public enum PayStatusEnum {
    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    FAILED(2, "支付失败");

    private final Integer code;
    private final String desc;

    PayStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
