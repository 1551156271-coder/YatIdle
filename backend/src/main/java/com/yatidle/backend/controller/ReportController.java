package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.admin.AdminReportHandleDTO;
import com.yatidle.backend.dto.report.CreateReportDTO;
import com.yatidle.backend.entity.Report;
import com.yatidle.backend.service.AdminReportService;
import com.yatidle.backend.util.ImageUploadValidator;
import com.yatidle.backend.vo.admin.AdminReportVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
public class ReportController {
    private final AdminReportService adminReportService;

    public ReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @PostMapping("/api/reports")
    public Result<Report> create(@RequestBody CreateReportDTO dto) {
        return Result.success(adminReportService.create(dto));
    }

    @PostMapping("/api/reports/images/upload")
    public Result<Map<String, String>> uploadReportImage(@RequestParam("file") MultipartFile file) throws IOException {
        String ext = ImageUploadValidator.validate(file);
        String filename = UUID.randomUUID() + ext;
        Path uploadDir = Paths.get("uploads", "reports").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(filename);
        file.transferTo(target);
        return Result.success(Map.of("url", "/uploads/reports/" + filename));
    }

    @GetMapping("/api/admin/reports")
    public Result<Page<AdminReportVO>> list(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) String reason,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminReportService.list(status, reason, page, size));
    }

    @GetMapping("/api/admin/reports/{id}")
    public Result<AdminReportVO> detail(@PathVariable Long id) {
        return Result.success(adminReportService.detail(id));
    }

    @PutMapping("/api/admin/reports/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @RequestBody AdminReportHandleDTO dto, HttpServletRequest request) {
        adminReportService.handle(AdminControllerSupport.currentAdminId(request), id, dto.getStatus(), dto.getResult(), dto.getActionType());
        return Result.success();
    }

    @PutMapping("/api/admin/reports/{id}/restore")
    public Result<Void> restore(@PathVariable Long id, @RequestBody AdminReportHandleDTO dto, HttpServletRequest request) {
        adminReportService.restoreAction(AdminControllerSupport.currentAdminId(request), id, dto.getResult());
        return Result.success();
    }
}
