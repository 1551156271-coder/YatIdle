package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.entity.AdminActionLog;
import com.yatidle.backend.service.AdminLogService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {
    private final AdminLogService adminLogService;

    public AdminLogController(AdminLogService adminLogService) {
        this.adminLogService = adminLogService;
    }

    @GetMapping
    public Result<Page<AdminActionLog>> list(@RequestParam(required = false) Long adminId,
                                             @RequestParam(required = false) String targetType,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminLogService.list(adminId, targetType, page, size));
    }
}
