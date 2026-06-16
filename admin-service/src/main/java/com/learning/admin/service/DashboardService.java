package com.learning.admin.service;

import com.learning.admin.dto.resp.DashboardStatsResp;

public interface DashboardService {
    DashboardStatsResp getStats(Integer role);
}
