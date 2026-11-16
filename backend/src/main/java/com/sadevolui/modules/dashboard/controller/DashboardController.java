package com.sadevolui.modules.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.UUID;
import com.sadevolui.modules.dashboard.dto.DashboardSummaryResponse;
import com.sadevolui.modules.dashboard.service.DashboardService;
import com.sadevolui.security.CurrentUser;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(@RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID applicationId, @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return dashboardService.summary(CurrentUser.id(), projectId, applicationId, from, to);
    }
}
