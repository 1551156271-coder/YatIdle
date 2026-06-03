package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.entity.Wanted;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.mapper.WantedMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AdminUserService {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final WantedMapper wantedMapper;
    private final AdminLogService adminLogService;

    public AdminUserService(UserMapper userMapper, AdminLogService adminLogService) {
        this(userMapper, null, null, adminLogService);
    }

    @Autowired
    public AdminUserService(UserMapper userMapper, ItemMapper itemMapper, WantedMapper wantedMapper, AdminLogService adminLogService) {
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
        this.wantedMapper = wantedMapper;
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
        if ("inactive".equals(status) && adminId != null && adminId.equals(userId)) {
            throw new BusinessException("管理员不能封禁自己");
        }
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        String before = user.getStatus();
        user.setStatus(status);
        userMapper.updateById(user);
        adminLogService.log(adminId, "UPDATE_USER_STATUS", "USER", userId, before, status, reason);
        if ("inactive".equals(status)) {
            removeVisibleContentByUser(adminId, userId, reason);
        }
    }

    public void updateRole(Long adminId, Long userId, Integer role) {
        updateRole(adminId, userId, role, null);
    }

    public void updateRole(Long adminId, Long userId, Integer role, String reason) {
        if (role == null || (role != 0 && role != 1)) throw new BusinessException("用户角色不合法");
        if (role == 0 && adminId != null && adminId.equals(userId)) {
            throw new BusinessException("管理员不能取消自己的管理员权限");
        }
        requireReason(reason);
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
    private void removeVisibleContentByUser(Long adminId, Long userId, String reason) {
        removeVisibleItemsByUser(adminId, userId, reason);
        closeVisibleWantedByUser(adminId, userId, reason);
    }

    private void removeVisibleItemsByUser(Long adminId, Long userId, String reason) {
        if (itemMapper == null) return;
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getUserId, userId)
                .eq(Item::getIsDeleted, 0)
                .eq(Item::getStatus, "ON_SALE");
        List<Item> items = itemMapper.selectList(wrapper);
        for (Item item : items) {
            String before = item.getStatus();
            item.setStatus("REMOVED");
            itemMapper.updateById(item);
            adminLogService.log(adminId, "UPDATE_ITEM_STATUS", "ITEM", item.getId(), before, "REMOVED", reason);
        }
    }

    private void closeVisibleWantedByUser(Long adminId, Long userId, String reason) {
        if (wantedMapper == null) return;
        LambdaQueryWrapper<Wanted> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wanted::getUserId, userId)
                .eq(Wanted::getIsDeleted, 0)
                .in(Wanted::getStatus, Set.of("pending", "active"));
        List<Wanted> wantedPosts = wantedMapper.selectList(wrapper);
        for (Wanted wanted : wantedPosts) {
            String before = wanted.getStatus();
            wanted.setStatus("closed");
            wantedMapper.updateById(wanted);
            adminLogService.log(adminId, "UPDATE_WANTED_STATUS", "WANTED", wanted.getId(), before, "closed", reason);
        }
    }
}
