package com.learning.common.core.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200, "成功"),

    // 4xx
    PARAM_ERROR(40001, "参数校验失败"),
    USERNAME_EXIST(40002, "用户名已存在"),
    PHONE_EXIST(40003, "手机号已注册"),
    LOGIN_FAIL(40004, "用户名或密码错误"),
    TOKEN_INVALID(40005, "Token无效或过期"),
    ACCOUNT_DISABLED(40006, "账号已被禁用"),
    COURSE_NOT_FOUND(40007, "课程不存在"),
    COURSE_ALREADY_PURCHASED(40008, "课程已购买"),
    STOCK_NOT_ENOUGH(40009, "库存不足"),
    ORDER_NOT_FOUND(40010, "订单不存在"),
    ORDER_PAID(40011, "订单已支付"),
    DUPLICATE_PAY(40012, "重复支付"),
    REVIEW_EXISTS(40013, "评价已存在"),
    CART_DUPLICATE(40014, "课程已在购物车中"),
    FORBIDDEN(40015, "无权访问"),
    VIDEO_NOT_FOUND(40016, "视频不存在"),
    CATEGORY_NOT_FOUND(40017, "分类不存在"),
    CATEGORY_HAS_CHILDREN(40018, "该分类下有子分类，无法删除"),
    LAST_ADMIN_CANNOT_DISABLE(40019, "无法禁用最后一个管理员账号"),
    PASSWORD_TOO_SHORT(40020, "新密码不能少于6位"),
    PASSWORD_WRONG(40021, "旧密码不正确"),
    PROGRESS_BLOCKED(40022, "进度上报被拦截，只能递增"),

    // 5xx
    SYSTEM_ERROR(50001, "系统内部错误"),
    DB_ERROR(50002, "数据库异常"),
    MQ_ERROR(50003, "消息队列异常"),
    CACHE_ERROR(50004, "缓存异常"),
    REMOTE_CALL_ERROR(50005, "远程调用失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
