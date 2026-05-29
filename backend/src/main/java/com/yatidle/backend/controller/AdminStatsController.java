package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.service.AdminDashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {
    private final AdminDashboardService adminDashboardService;

    public AdminStatsController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(adminDashboardService.overview());
    }
}
