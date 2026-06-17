package com.learning.admin.controller;

import com.learning.admin.dto.resp.DashboardStatsResp;
import com.learning.admin.service.DashboardService;
import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public R<DashboardStatsResp> getStats(@CurrentUser UserContext userContext) {
        return R.ok(dashboardService.getStats(userContext.getRole()));
    }
}
