package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.admin.AdminRoleUpdateDTO;
import com.yatidle.backend.dto.admin.AdminStatusUpdateDTO;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Result<Page<User>> list(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) Integer role,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminUserService.list(keyword, status, role, page, size));
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(adminUserService.detail(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody AdminStatusUpdateDTO dto, HttpServletRequest request) {
        adminUserService.updateStatus(AdminControllerSupport.currentAdminId(request), id, dto.getStatus(), dto.getReason());
        return Result.success();
    }

    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody AdminRoleUpdateDTO dto, HttpServletRequest request) {
        adminUserService.updateRole(AdminControllerSupport.currentAdminId(request), id, dto.getRole(), dto.getReason());
        return Result.success();
    }
}
