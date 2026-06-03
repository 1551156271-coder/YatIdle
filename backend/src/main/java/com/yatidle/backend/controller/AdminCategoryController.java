package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.admin.AdminCategoryDTO;
import com.yatidle.backend.entity.Category;
import com.yatidle.backend.service.AdminCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(adminCategoryService.list());
    }

    @PostMapping
    public Result<Category> create(@RequestBody AdminCategoryDTO dto, HttpServletRequest request) {
        return Result.success(adminCategoryService.create(AdminControllerSupport.currentAdminId(request), dto));
    }

    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @RequestBody AdminCategoryDTO dto, HttpServletRequest request) {
        return Result.success(adminCategoryService.update(AdminControllerSupport.currentAdminId(request), id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        adminCategoryService.delete(AdminControllerSupport.currentAdminId(request), id);
        return Result.success();
    }

    @PutMapping("/{id}/delete")
    public Result<Void> deleteByPut(@PathVariable Long id, HttpServletRequest request) {
        adminCategoryService.delete(AdminControllerSupport.currentAdminId(request), id);
        return Result.success();
    }
}
