package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.admin.AdminStatusUpdateDTO;
import com.yatidle.backend.service.AdminWantedService;
import com.yatidle.backend.vo.wanted.WantedDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/wanted")
public class AdminWantedController {
    private final AdminWantedService adminWantedService;

    public AdminWantedController(AdminWantedService adminWantedService) {
        this.adminWantedService = adminWantedService;
    }

    @GetMapping
    public Result<Page<WantedDetailVO>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long categoryId,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminWantedService.list(keyword, categoryId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<WantedDetailVO> detail(@PathVariable Long id) {
        return Result.success(adminWantedService.detail(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody AdminStatusUpdateDTO dto, HttpServletRequest request) {
        adminWantedService.updateStatus(AdminControllerSupport.currentAdminId(request), id, dto.getStatus(), dto.getReason());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestBody(required = false) AdminStatusUpdateDTO dto, HttpServletRequest request) {
        adminWantedService.delete(AdminControllerSupport.currentAdminId(request), id, dto == null ? null : dto.getReason());
        return Result.success();
    }

    @PutMapping("/{id}/delete")
    public Result<Void> deleteByPut(@PathVariable Long id, @RequestBody(required = false) AdminStatusUpdateDTO dto, HttpServletRequest request) {
        adminWantedService.delete(AdminControllerSupport.currentAdminId(request), id, dto == null ? null : dto.getReason());
        return Result.success();
    }
}
