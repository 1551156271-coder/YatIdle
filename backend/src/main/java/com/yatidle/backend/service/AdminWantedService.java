package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.entity.Wanted;
import com.yatidle.backend.entity.WantedImage;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.mapper.WantedImageMapper;
import com.yatidle.backend.mapper.WantedMapper;
import com.yatidle.backend.vo.wanted.WantedDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AdminWantedService {
    private static final Set<String> VALID_STATUSES = Set.of("pending", "active", "closed", "sold");

    private final WantedMapper wantedMapper;
    private final WantedImageMapper wantedImageMapper;
    private final UserMapper userMapper;
    private final AdminLogService adminLogService;
    private final String baseUrl;

    public AdminWantedService(WantedMapper wantedMapper, AdminLogService adminLogService) {
        this(wantedMapper, null, null, adminLogService, "");
    }

    @Autowired
    public AdminWantedService(WantedMapper wantedMapper,
                              WantedImageMapper wantedImageMapper,
                              UserMapper userMapper,
                              AdminLogService adminLogService,
                              @Value("${app.base-url}") String baseUrl) {
        this.wantedMapper = wantedMapper;
        this.wantedImageMapper = wantedImageMapper;
        this.userMapper = userMapper;
        this.adminLogService = adminLogService;
        this.baseUrl = baseUrl;
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

    public WantedDetailVO detail(Long id) {
        Wanted wanted = findActiveWanted(id);
        WantedDetailVO vo = WantedDetailVO.from(wanted, imageUrls(id));
        vo.setUsername(getUsername(wanted.getUserId()));
        return vo;
    }

    public void updateStatus(Long adminId, Long id, String status, String reason) {
        if (!VALID_STATUSES.contains(status)) throw new BusinessException("求购状态不合法");
        requireReason(reason);
        Wanted wanted = findActiveWanted(id);
        String before = wanted.getStatus();
        wanted.setStatus(status);
        wantedMapper.updateById(wanted);
        adminLogService.log(adminId, "UPDATE_WANTED_STATUS", "WANTED", id, before, status, reason);
    }

    public void delete(Long adminId, Long id, String reason) {
        requireReason(reason);
        Wanted wanted = findActiveWanted(id);
        String before = wanted.getStatus();
        wanted.setIsDeleted(1);
        wantedMapper.updateById(wanted);
        adminLogService.log(adminId, "DELETE_WANTED", "WANTED", id, before, "DELETED", reason);
    }

    private Wanted findActiveWanted(Long id) {
        Wanted wanted = wantedMapper.selectById(id);
        if (wanted == null || (wanted.getIsDeleted() != null && wanted.getIsDeleted() == 1)) throw new BusinessException("求购不存在");
        return wanted;
    }

    private List<String> imageUrls(Long wantedId) {
        if (wantedImageMapper == null) return List.of();
        return wantedImageMapper.selectByWantedId(wantedId).stream().map(WantedImage::getImageUrl).map(this::resolveUrl).toList();
    }

    private String getUsername(Long userId) {
        if (userMapper == null || userId == null) return null;
        User user = userMapper.selectById(userId);
        return user == null ? null : user.getUsername();
    }

    private String resolveUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        return baseUrl + url;
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) throw new BusinessException("操作原因不能为空");
    }
}
