package com.learning.order.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelRulesConfig {

    @PostConstruct
    public void initRules() {
        List<FlowRule> flowRules = new ArrayList<>();
        flowRules.add(new FlowRule("createOrder")
                .setCount(100)
                .setGrade(RuleConstant.FLOW_GRADE_QPS));
        FlowRuleManager.loadRules(flowRules);

        List<DegradeRule> degradeRules = new ArrayList<>();
        degradeRules.add(new DegradeRule("createOrder")
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.2)
                .setTimeWindow(10)
                .setMinRequestAmount(10)
                .setStatIntervalMs(10000));
        DegradeRuleManager.loadRules(degradeRules);
    }
}
