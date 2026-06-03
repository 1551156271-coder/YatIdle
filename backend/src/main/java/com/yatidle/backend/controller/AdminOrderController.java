package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.admin.AdminStatusUpdateDTO;
import com.yatidle.backend.service.AdminOrderService;
import com.yatidle.backend.vo.admin.AdminOrderLogVO;
import com.yatidle.backend.vo.admin.AdminOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public Result<Page<AdminOrderVO>> list(@RequestParam(required = false) String status,
                                           @RequestParam(required = false) Long userId,
                                           @RequestParam(required = false) Long itemId,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminOrderService.list(status, userId, itemId, page, size));
    }

    @GetMapping("/{id}")
    public Result<AdminOrderVO> detail(@PathVariable Long id) {
        return Result.success(adminOrderService.detail(id));
    }

    @GetMapping("/{id}/logs")
    public Result<List<AdminOrderLogVO>> logs(@PathVariable Long id) {
        return Result.success(adminOrderService.logs(id));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestBody(required = false) AdminStatusUpdateDTO dto, HttpServletRequest request) {
        adminOrderService.cancel(AdminControllerSupport.currentAdminId(request), id, dto == null ? null : dto.getReason());
        return Result.success();
    }
}
