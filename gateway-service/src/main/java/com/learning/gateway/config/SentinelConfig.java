package com.learning.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("user-service").setCount(1000).setIntervalSec(1));
        rules.add(new GatewayFlowRule("course-service").setCount(2000).setIntervalSec(1));
        rules.add(new GatewayFlowRule("order-service").setCount(500).setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);
    }
}
