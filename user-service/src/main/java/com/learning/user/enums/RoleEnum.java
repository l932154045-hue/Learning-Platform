package com.learning.user.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    STUDENT(0, "学员"),
    ADMIN(1, "管理员");

    private final Integer code;
    private final String desc;

    RoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
