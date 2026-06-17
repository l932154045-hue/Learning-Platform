package com.learning.admin.service.impl;

import com.learning.admin.client.CourseServiceClient;
import com.learning.admin.client.OrderServiceClient;
import com.learning.admin.client.UserServiceClient;
import com.learning.admin.dto.resp.DashboardStatsResp;
import com.learning.admin.service.AdminAuthService;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardServiceImpl 单元测试")
class DashboardServiceImplTest {

    @Mock
    private AdminAuthService authService;
    @Mock
    private CourseServiceClient courseServiceClient;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private OrderServiceClient orderServiceClient;
    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("正常聚合仪表盘数据")
    void shouldAggregateDashboardStats() {
        when(courseServiceClient.getCourseCount()).thenReturn(R.ok(50L));
        when(userServiceClient.getUserCount()).thenReturn(R.ok(200L));
        when(orderServiceClient.getOrderCount()).thenReturn(R.ok(100L));
        when(orderServiceClient.getTotalRevenue()).thenReturn(R.ok(BigDecimal.valueOf(9999)));
        when(orderServiceClient.listOrders(any(), any(), any())).thenReturn(R.ok(null));

        DashboardStatsResp stats = dashboardService.getStats(1);
        assertNotNull(stats);
        assertEquals(50L, stats.getTotalCourses());
        assertEquals(200L, stats.getTotalUsers());
        assertEquals(100L, stats.getTotalOrders());
        assertEquals(BigDecimal.valueOf(9999), stats.getTotalRevenue());
    }

    @Test
    @DisplayName("非管理员调用抛异常")
    void shouldThrowExceptionWhenNotAdmin() {
        doThrow(new BizException(40005, "仅管理员可操作")).when(authService).checkAdmin(0);
        assertThrows(BizException.class, () -> dashboardService.getStats(0));
    }

    @Test
    @DisplayName("下游服务异常 — 捕获不中断，返回部分数据")
    void shouldNotThrowWhenDownstreamFails() {
        when(courseServiceClient.getCourseCount()).thenThrow(new RuntimeException("服务不可用"));
        when(userServiceClient.getUserCount()).thenReturn(R.ok(200L));
        when(orderServiceClient.getOrderCount()).thenReturn(R.ok(100L));
        when(orderServiceClient.getTotalRevenue()).thenReturn(R.ok(BigDecimal.valueOf(9999)));
        when(orderServiceClient.listOrders(any(), any(), any())).thenReturn(R.ok(null));

        assertDoesNotThrow(() -> {
            DashboardStatsResp stats = dashboardService.getStats(1);
            assertNotNull(stats);
            assertNull(stats.getTotalCourses()); // failed, not set
            assertEquals(200L, stats.getTotalUsers()); // succeeded
        });
    }
}
