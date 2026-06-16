package com.learning.admin.service.impl;

import com.learning.admin.client.CourseServiceClient;
import com.learning.admin.client.OrderServiceClient;
import com.learning.admin.client.UserServiceClient;
import com.learning.admin.dto.resp.DashboardStatsResp;
import com.learning.admin.service.AdminAuthService;
import com.learning.admin.service.DashboardService;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final AdminAuthService authService;
    private final CourseServiceClient courseServiceClient;
    private final UserServiceClient userServiceClient;
    private final OrderServiceClient orderServiceClient;

    @Override
    public DashboardStatsResp getStats(Integer role) {
        authService.checkAdmin(role);

        DashboardStatsResp stats = new DashboardStatsResp();

        // Fetch course count
        try {
            R<Long> courseCount = courseServiceClient.getCourseCount();
            if (courseCount != null && courseCount.getCode() == 200) {
                stats.setTotalCourses(courseCount.getData());
            }
        } catch (Exception e) {
            log.warn("获取课程总数失败", e);
        }

        // Fetch user count
        try {
            R<Long> userCount = userServiceClient.getUserCount();
            if (userCount != null && userCount.getCode() == 200) {
                stats.setTotalUsers(userCount.getData());
            }
        } catch (Exception e) {
            log.warn("获取用户总数失败", e);
        }

        // Fetch order count
        try {
            R<Long> orderCount = orderServiceClient.getOrderCount();
            if (orderCount != null && orderCount.getCode() == 200) {
                stats.setTotalOrders(orderCount.getData());
            }
        } catch (Exception e) {
            log.warn("获取订单总数失败", e);
        }

        // Fetch total revenue
        try {
            R<java.math.BigDecimal> revenue = orderServiceClient.getTotalRevenue();
            if (revenue != null && revenue.getCode() == 200) {
                stats.setTotalRevenue(revenue.getData());
            }
        } catch (Exception e) {
            log.warn("获取总营收失败", e);
        }

        // Fetch recent orders (last 10)
        try {
            PageReq pageReq = new PageReq();
            pageReq.setPageNum(1);
            pageReq.setPageSize(10);
            R<PageResp<Map<String, Object>>> orders = orderServiceClient.listOrders(pageReq);
            if (orders != null && orders.getCode() == 200 && orders.getData() != null) {
                stats.setRecentOrders(orders.getData().getRecords());
            }
        } catch (Exception e) {
            log.warn("获取最近订单失败", e);
        }

        return stats;
    }
}
