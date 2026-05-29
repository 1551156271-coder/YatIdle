package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.dto.report.CreateReportDTO;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.Report;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.ReportMapper;
import com.yatidle.backend.mapper.TradeOrderMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.mapper.WantedMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminReportService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final WantedMapper wantedMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final AdminUserService adminUserService;
    private final AdminLogService adminLogService;
    private final String baseUrl;

    public AdminReportService(ReportMapper reportMapper,
                              UserMapper userMapper,
                              ItemMapper itemMapper,
                              WantedMapper wantedMapper,
                              TradeOrderMapper tradeOrderMapper,
                              AdminUserService adminUserService,
                              AdminLogService adminLogService,
                              @Value("${app.base-url}") String baseUrl) {
        this.reportMapper = reportMapper;
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
        this.wantedMapper = wantedMapper;
        this.tradeOrderMapper = tradeOrderMapper;
        this.adminUserService = adminUserService;
        this.adminLogService = adminLogService;
        this.baseUrl = baseUrl;
    }

    public Report create(CreateReportDTO dto) {
        if (dto == null || dto.getReporterId() == null) throw new BusinessException("举报人不能为空");
        if (dto.getReason() == null || dto.getReason().isBlank()) throw new BusinessException("举报原因不能为空");
        Report report = new Report();
        report.setReporterId(dto.getReporterId());
        report.setTargetUserId(dto.getTargetUserId());
        report.setItemId(dto.getItemId());
        report.setWantedId(dto.getWantedId());
        report.setOrderId(dto.getOrderId());
        report.setChatSessionId(dto.getChatSessionId());
        report.setReason(dto.getReason());
        report.setDescription(dto.getDescription());
        report.setImageUrls(toJson(dto.getImageUrls()));
        report.setStatus("PENDING");
        report.setIsDeleted(0);
        reportMapper.insert(report);
        return report;
    }

    public Page<Report> list(String status, String reason, int page, int size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getIsDeleted, 0);
        if (status != null && !status.isBlank()) wrapper.eq(Report::getStatus, status);
        if (reason != null && !reason.isBlank()) wrapper.eq(Report::getReason, reason);
        wrapper.orderByDesc(Report::getCreateTime);
        return reportMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public Map<String, Object> detail(Long id) {
        Report report = findReport(id);
        Map<String, Object> data = new HashMap<>();
        data.put("report", report);
        data.put("reporter", safeUser(report.getReporterId()));
        data.put("targetUser", safeUser(report.getTargetUserId()));
        data.put("item", itemMapper == null || report.getItemId() == null ? null : itemMapper.selectById(report.getItemId()));
        data.put("wanted", wantedMapper == null || report.getWantedId() == null ? null : wantedMapper.selectById(report.getWantedId()));
        data.put("order", tradeOrderMapper == null || report.getOrderId() == null ? null : tradeOrderMapper.selectById(report.getOrderId()));
        data.put("baseUrl", baseUrl);
        return data;
    }

    @Transactional
    public void handle(Long adminId, Long reportId, String status, String result, String actionType) {
        if (!"HANDLED".equals(status) && !"REJECTED".equals(status)) throw new BusinessException("举报处理状态不合法");
        requireResult(result);
        Report report = findReport(reportId);
        String before = report.getStatus();
        report.setStatus(status);
        report.setHandleResult(result);
        report.setHandlerId(adminId);
        report.setHandleTime(LocalDateTime.now());
        reportMapper.updateById(report);
        if ("BAN_USER".equals(actionType) && report.getTargetUserId() != null && adminUserService != null) {
            adminUserService.updateStatus(adminId, report.getTargetUserId(), "inactive", result);
        }
        if ("OFFLINE_ITEM".equals(actionType) && report.getItemId() != null && itemMapper != null) {
            Item item = itemMapper.selectById(report.getItemId());
            if (item != null && (item.getIsDeleted() == null || item.getIsDeleted() == 0)) {
                String itemBefore = item.getStatus();
                item.setStatus("REMOVED");
                itemMapper.updateById(item);
                adminLogService.log(adminId, "UPDATE_ITEM_STATUS", "ITEM", item.getId(), itemBefore, "REMOVED", result);
            }
        }
        adminLogService.log(adminId, "HANDLE_REPORT", "REPORT", reportId, before, status, result);
    }

    private Report findReport(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null || (report.getIsDeleted() != null && report.getIsDeleted() == 1)) throw new BusinessException("举报不存在");
        return report;
    }

    private User safeUser(Long userId) {
        if (userMapper == null || userId == null) return null;
        User user = userMapper.selectById(userId);
        if (user != null) user.setPassword(null);
        return user;
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException("举报图片格式不合法");
        }
    }

    private void requireResult(String result) {
        if (result == null || result.isBlank()) throw new BusinessException("处理结果不能为空");
    }
}
