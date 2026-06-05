package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.entity.AdminActionLog;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.Report;
import com.yatidle.backend.entity.TradeOrder;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.entity.Wanted;
import com.yatidle.backend.mapper.AdminActionLogMapper;
import com.yatidle.backend.mapper.CategoryMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.ReportMapper;
import com.yatidle.backend.mapper.TradeOrderMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.mapper.WantedMapper;
import com.yatidle.backend.vo.admin.AdminActionLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminLogService {

    private final AdminActionLogMapper adminActionLogMapper;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final ReportMapper reportMapper;
    private final WantedMapper wantedMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final CategoryMapper categoryMapper;

    public AdminLogService(AdminActionLogMapper adminActionLogMapper) {
        this(adminActionLogMapper, null, null, null, null, null, null);
    }

    public AdminLogService(AdminActionLogMapper adminActionLogMapper,
                           UserMapper userMapper,
                           ItemMapper itemMapper,
                           ReportMapper reportMapper,
                           WantedMapper wantedMapper,
                           TradeOrderMapper tradeOrderMapper) {
        this(adminActionLogMapper, userMapper, itemMapper, reportMapper, wantedMapper, tradeOrderMapper, null);
    }

    @Autowired
    public AdminLogService(AdminActionLogMapper adminActionLogMapper,
                           UserMapper userMapper,
                           ItemMapper itemMapper,
                           ReportMapper reportMapper,
                           WantedMapper wantedMapper,
                           TradeOrderMapper tradeOrderMapper,
                           CategoryMapper categoryMapper) {
        this.adminActionLogMapper = adminActionLogMapper;
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
        this.reportMapper = reportMapper;
        this.wantedMapper = wantedMapper;
        this.tradeOrderMapper = tradeOrderMapper;
        this.categoryMapper = categoryMapper;
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

    public Page<AdminActionLogVO> list(Long adminId, String targetType, int page, int size) {
        LambdaQueryWrapper<AdminActionLog> wrapper = new LambdaQueryWrapper<>();
        if (adminId != null) {
            wrapper.eq(AdminActionLog::getAdminId, adminId);
        }
        if (targetType != null && !targetType.isBlank()) {
            wrapper.eq(AdminActionLog::getTargetType, targetType);
        }
        wrapper.orderByDesc(AdminActionLog::getCreateTime);
        return toVOPage(adminActionLogMapper.selectPage(new Page<>(page, size), wrapper));
    }

    private Page<AdminActionLogVO> toVOPage(Page<AdminActionLog> source) {
        Page<AdminActionLogVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setPages(source.getPages());
        result.setRecords(enrichLogs(source.getRecords()));
        return result;
    }

    private List<AdminActionLogVO> enrichLogs(List<AdminActionLog> logs) {
        Set<Long> adminIds = logs.stream().map(AdminActionLog::getAdminId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, User> admins = mapUsers(adminIds);
        Map<String, String> targetNames = targetNames(logs);
        return logs.stream().map(AdminActionLogVO::from).peek(vo -> {
            User admin = admins.get(vo.getAdminId());
            if (admin != null) vo.setAdminUsername(displayUser(admin));
            vo.setTargetName(targetNames.getOrDefault(key(vo.getTargetType(), vo.getTargetId()), "-"));
        }).toList();
    }

    private Map<Long, User> mapUsers(Set<Long> ids) {
        if (userMapper == null || ids.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(List.copyOf(ids)).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private Map<String, String> targetNames(List<AdminActionLog> logs) {
        Map<String, String> names = new LinkedHashMap<>();
        fillUsers(names, idsByType(logs, "USER"));
        fillItems(names, idsByType(logs, "ITEM"));
        fillReports(names, idsByType(logs, "REPORT"));
        fillWanted(names, idsByType(logs, "WANTED"));
        fillOrders(names, idsByType(logs, "ORDER"));
        fillCategories(names, idsByType(logs, "CATEGORY"));
        return names;
    }

    private Set<Long> idsByType(List<AdminActionLog> logs, String targetType) {
        return logs.stream()
                .filter(log -> targetType.equals(log.getTargetType()))
                .map(AdminActionLog::getTargetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void fillUsers(Map<String, String> names, Set<Long> ids) {
        if (userMapper == null || ids.isEmpty()) return;
        userMapper.selectBatchIds(List.copyOf(ids)).forEach(user -> names.put(key("USER", user.getId()), displayUser(user)));
    }

    private void fillItems(Map<String, String> names, Set<Long> ids) {
        if (itemMapper == null || ids.isEmpty()) return;
        itemMapper.selectBatchIds(List.copyOf(ids)).forEach(item -> names.put(key("ITEM", item.getId()), item.getTitle()));
    }

    private void fillReports(Map<String, String> names, Set<Long> ids) {
        if (reportMapper == null || ids.isEmpty()) return;
        reportMapper.selectBatchIds(List.copyOf(ids)).forEach(report -> names.put(key("REPORT", report.getId()), "举报#" + report.getId()));
    }

    private void fillWanted(Map<String, String> names, Set<Long> ids) {
        if (wantedMapper == null || ids.isEmpty()) return;
        wantedMapper.selectBatchIds(List.copyOf(ids)).forEach(wanted -> names.put(key("WANTED", wanted.getId()), wanted.getTitle()));
    }

    private void fillOrders(Map<String, String> names, Set<Long> ids) {
        if (tradeOrderMapper == null || ids.isEmpty()) return;
        tradeOrderMapper.selectBatchIds(List.copyOf(ids)).forEach(order -> names.put(key("ORDER", order.getId()), order.getOrderNo()));
    }

    private void fillCategories(Map<String, String> names, Set<Long> ids) {
        if (categoryMapper == null || ids.isEmpty()) return;
        categoryMapper.selectBatchIds(List.copyOf(ids)).forEach(category -> names.put(key("CATEGORY", category.getId()), category.getName()));
    }

    private String key(String targetType, Long targetId) {
        return targetType + ":" + targetId;
    }

    private String displayUser(User user) {
        return Objects.requireNonNullElse(user.getUsername(), Objects.requireNonNullElse(user.getNickname(), "用户" + user.getId()));
    }
}
