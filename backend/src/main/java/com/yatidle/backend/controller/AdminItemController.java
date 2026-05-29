package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.admin.AdminStatusUpdateDTO;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.service.AdminItemService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/items")
public class AdminItemController {
    private final AdminItemService adminItemService;

    public AdminItemController(AdminItemService adminItemService) {
        this.adminItemService = adminItemService;
    }

    @GetMapping
    public Result<Page<Item>> list(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Long categoryId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String campus,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminItemService.list(keyword, categoryId, status, campus, page, size));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(adminItemService.detail(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody AdminStatusUpdateDTO dto, HttpServletRequest request) {
        adminItemService.updateStatus(AdminControllerSupport.currentAdminId(request), id, dto.getStatus(), dto.getReason());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestBody(required = false) AdminStatusUpdateDTO dto, HttpServletRequest request) {
        adminItemService.delete(AdminControllerSupport.currentAdminId(request), id, dto == null ? null : dto.getReason());
        return Result.success();
    }
}
