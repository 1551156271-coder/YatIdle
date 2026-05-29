package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.entity.AdminActionLog;
import com.yatidle.backend.mapper.AdminActionLogMapper;
import org.springframework.stereotype.Service;

@Service
public class AdminLogService {

    private final AdminActionLogMapper adminActionLogMapper;

    public AdminLogService(AdminActionLogMapper adminActionLogMapper) {
        this.adminActionLogMapper = adminActionLogMapper;
    }

    public void log(Long adminId, String action, String targetType, Long targetId,
                    String beforeStatus, String afterStatus, String remark) {
        AdminActionLog log = new AdminActionLog();
        log.setAdminId(adminId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setRemark(remark);
        adminActionLogMapper.insert(log);
    }

    public Page<AdminActionLog> list(Long adminId, String targetType, int page, int size) {
        LambdaQueryWrapper<AdminActionLog> wrapper = new LambdaQueryWrapper<>();
        if (adminId != null) {
            wrapper.eq(AdminActionLog::getAdminId, adminId);
        }
        if (targetType != null && !targetType.isBlank()) {
            wrapper.eq(AdminActionLog::getTargetType, targetType);
        }
        wrapper.orderByDesc(AdminActionLog::getCreateTime);
        return adminActionLogMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
