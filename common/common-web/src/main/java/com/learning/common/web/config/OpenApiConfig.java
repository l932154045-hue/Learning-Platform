package com.learning.common.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:unknown-service}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(titleFromAppName(applicationName))
                        .description(descFromAppName(applicationName))
                        .version("1.0.0")
                        .contact(new Contact().name("Learning Platform")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("输入 JWT Token（不含 Bearer 前缀）")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        String pathPrefix = pathFromAppName(applicationName);
        return GroupedOpenApi.builder()
                .group(applicationName)
                .pathsToMatch(pathPrefix + "/**")
                .build();
    }

    private static String titleFromAppName(String name) {
        String base = name.replace("-service", "");
        char first = Character.toUpperCase(base.charAt(0));
        return first + base.substring(1) + " Service API";
    }

    private static String descFromAppName(String name) {
        return switch (name) {
            case "user-service" -> "用户服务：注册、登录、个人信息管理";
            case "course-service" -> "课程服务：课程列表、详情、分类、搜索";
            case "cart-service" -> "购物车服务：加购、删除、清空";
            case "order-service" -> "订单服务：下单、订单查询、取消、支付回调";
            case "payment-service" -> "支付服务：支付处理、退款";
            case "learning-service" -> "学习服务：视频学习、进度上报、课程评价";
            case "admin-service" -> "管理后台：课程管理、用户管理、订单管理、数据看板";
            default -> name + " API";
        };
    }

    private static String pathFromAppName(String name) {
        String prefix = name.replace("-service", "");
        return "/api/" + prefix;
    }
}
