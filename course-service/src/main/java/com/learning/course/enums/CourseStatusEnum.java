package com.learning.course.enums;

import lombok.Getter;

@Getter
public enum CourseStatusEnum {
    OFFLINE(0, "下架"),
    ONLINE(1, "上架");

    private final Integer code;
    private final String desc;

    CourseStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
