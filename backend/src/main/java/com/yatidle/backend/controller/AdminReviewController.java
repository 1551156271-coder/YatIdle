package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.admin.AdminStatusUpdateDTO;
import com.yatidle.backend.service.AdminReviewService;
import com.yatidle.backend.vo.admin.AdminReviewVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {
    private final AdminReviewService adminReviewService;

    public AdminReviewController(AdminReviewService adminReviewService) {
        this.adminReviewService = adminReviewService;
    }

    @GetMapping
    public Result<Page<AdminReviewVO>> list(@RequestParam(required = false) Long reviewerId,
                                            @RequestParam(required = false) Long revieweeId,
                                            @RequestParam(required = false) Integer rating,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminReviewService.list(reviewerId, revieweeId, rating, page, size));
    }

    @GetMapping("/{id}")
    public Result<AdminReviewVO> detail(@PathVariable Long id) {
        return Result.success(adminReviewService.detail(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestBody(required = false) AdminStatusUpdateDTO dto, HttpServletRequest request) {
        adminReviewService.delete(AdminControllerSupport.currentAdminId(request), id, dto == null ? null : dto.getReason());
        return Result.success();
    }

    @PutMapping("/{id}/delete")
    public Result<Void> deleteByPut(@PathVariable Long id, @RequestBody(required = false) AdminStatusUpdateDTO dto, HttpServletRequest request) {
        adminReviewService.delete(AdminControllerSupport.currentAdminId(request), id, dto == null ? null : dto.getReason());
        return Result.success();
    }
}
