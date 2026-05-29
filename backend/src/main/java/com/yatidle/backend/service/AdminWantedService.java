package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.Wanted;
import com.yatidle.backend.mapper.WantedMapper;
import org.springframework.stereotype.Service;

@Service
public class AdminWantedService {
    private final WantedMapper wantedMapper;
    private final AdminLogService adminLogService;

    public AdminWantedService(WantedMapper wantedMapper, AdminLogService adminLogService) {
        this.wantedMapper = wantedMapper;
        this.adminLogService = adminLogService;
    }

    public Page<Wanted> list(String keyword, Long categoryId, String status, int page, int size) {
        LambdaQueryWrapper<Wanted> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wanted::getIsDeleted, 0);
        if (keyword != null && !keyword.isBlank()) wrapper.like(Wanted::getTitle, keyword);
        if (categoryId != null) wrapper.eq(Wanted::getCategoryId, categoryId);
        if (status != null && !status.isBlank()) wrapper.eq(Wanted::getStatus, status);
        wrapper.orderByDesc(Wanted::getCreateTime);
        return wantedMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public Wanted detail(Long id) {
        Wanted wanted = wantedMapper.selectById(id);
        if (wanted == null || (wanted.getIsDeleted() != null && wanted.getIsDeleted() == 1)) throw new BusinessException("求购不存在");
        return wanted;
    }

    public void updateStatus(Long adminId, Long id, String status, String reason) {
        requireReason(reason);
        Wanted wanted = detail(id);
        String before = wanted.getStatus();
        wanted.setStatus(status);
        wantedMapper.updateById(wanted);
        adminLogService.log(adminId, "UPDATE_WANTED_STATUS", "WANTED", id, before, status, reason);
    }

    public void delete(Long adminId, Long id, String reason) {
        requireReason(reason);
        Wanted wanted = detail(id);
        String before = wanted.getStatus();
        wanted.setIsDeleted(1);
        wantedMapper.updateById(wanted);
        adminLogService.log(adminId, "DELETE_WANTED", "WANTED", id, before, "DELETED", reason);
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) throw new BusinessException("操作原因不能为空");
    }
}
