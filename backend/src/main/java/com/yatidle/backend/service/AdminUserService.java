package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final UserMapper userMapper;
    private final AdminLogService adminLogService;

    public AdminUserService(UserMapper userMapper, AdminLogService adminLogService) {
        this.userMapper = userMapper;
        this.adminLogService = adminLogService;
    }

    public Page<User> list(String keyword, String status, Integer role, int page, int size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        if (status != null && !status.isBlank()) wrapper.eq(User::getStatus, status);
        if (role != null) wrapper.eq(User::getRole, role);
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(user -> user.setPassword(null));
        return result;
    }

    public User detail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(null);
        return user;
    }

    public void updateStatus(Long adminId, Long userId, String status, String reason) {
        if (!"active".equals(status) && !"inactive".equals(status)) throw new BusinessException("用户状态不合法");
        requireReason(reason);
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        String before = user.getStatus();
        user.setStatus(status);
        userMapper.updateById(user);
        adminLogService.log(adminId, "UPDATE_USER_STATUS", "USER", userId, before, status, reason);
    }

    public void updateRole(Long adminId, Long userId, Integer role) {
        updateRole(adminId, userId, role, null);
    }

    public void updateRole(Long adminId, Long userId, Integer role, String reason) {
        if (role == null || (role != 0 && role != 1)) throw new BusinessException("用户角色不合法");
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        String before = String.valueOf(user.getRole());
        user.setRole(role);
        userMapper.updateById(user);
        adminLogService.log(adminId, "UPDATE_USER_ROLE", "USER", userId, before, String.valueOf(role), reason);
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) throw new BusinessException("操作原因不能为空");
    }
}
