package com.learning.admin.controller;

import com.learning.admin.dto.resp.DashboardStatsResp;
import com.learning.admin.service.DashboardService;
import com.learning.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public R<DashboardStatsResp> getStats(@RequestAttribute("role") Integer role) {
        return R.ok(dashboardService.getStats(role));
    }
}
