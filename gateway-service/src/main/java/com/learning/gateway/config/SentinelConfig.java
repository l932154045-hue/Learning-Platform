package com.learning.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SentinelConfig {

    private final SentinelBlockRequestHandler blockRequestHandler;

    public SentinelConfig(SentinelBlockRequestHandler blockRequestHandler) {
        this.blockRequestHandler = blockRequestHandler;
    }

    @PostConstruct
    public void initGatewayRules() {
        // 注册自定义 BlockException 处理器（返回结构化 JSON 而非默认纯文本）
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);

        Set<GatewayFlowRule> rules = new HashSet<>();
        // 课程服务：列表/详情高并发（首页访问）
        rules.add(new GatewayFlowRule("course-service").setCount(2000).setIntervalSec(1));
        // 用户服务：登录/注册高并发
        rules.add(new GatewayFlowRule("user-service").setCount(1000).setIntervalSec(1));
        // 购物车服务：中等并发
        rules.add(new GatewayFlowRule("cart-service").setCount(800).setIntervalSec(1));
        // 学习服务：视频学习进度上报，中等并发
        rules.add(new GatewayFlowRule("learning-service").setCount(800).setIntervalSec(1));
        // 订单服务：下单为关键操作，适度限流保障稳定性
        rules.add(new GatewayFlowRule("order-service").setCount(500).setIntervalSec(1));
        // 支付服务：与订单联动，相同量级
        rules.add(new GatewayFlowRule("payment-service").setCount(500).setIntervalSec(1));
        // 管理后台：仅管理员访问
        rules.add(new GatewayFlowRule("admin-service").setCount(100).setIntervalSec(1));

        GatewayRuleManager.loadRules(rules);
    }
}
