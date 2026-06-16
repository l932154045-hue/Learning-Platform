package com.learning.admin.dto.resp;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardStatsResp {
    private Long totalCourses;
    private Long totalUsers;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private List<Map<String, Object>> recentOrders;
}
