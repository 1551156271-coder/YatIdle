package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.dto.report.CreateReportDTO;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.Report;
import com.yatidle.backend.entity.TradeOrder;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.entity.Wanted;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.ReportMapper;
import com.yatidle.backend.mapper.TradeOrderMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.mapper.WantedMapper;
import com.yatidle.backend.vo.admin.AdminReportVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public Page<AdminReportVO> list(String status, String reason, int page, int size) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getIsDeleted, 0);
        if (status != null && !status.isBlank()) wrapper.eq(Report::getStatus, status);
        if (reason != null && !reason.isBlank()) wrapper.eq(Report::getReason, reason);
        wrapper.orderByDesc(Report::getCreateTime);
        return toVOPage(reportMapper.selectPage(new Page<>(page, size), wrapper));
    }

    public AdminReportVO detail(Long id) {
        Report report = findReport(id);
        return enrich(AdminReportVO.from(report),
                mapUsers(ids(report.getReporterId(), report.getTargetUserId(), report.getHandlerId())),
                mapItems(ids(report.getItemId())),
                mapWanted(ids(report.getWantedId())),
                mapOrders(ids(report.getOrderId())));
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

    private Page<AdminReportVO> toVOPage(Page<Report> source) {
        Page<AdminReportVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setPages(source.getPages());
        result.setRecords(enrichReports(source.getRecords()));
        return result;
    }

    private List<AdminReportVO> enrichReports(List<Report> reports) {
        Set<Long> userIds = new LinkedHashSet<>();
        Set<Long> itemIds = new LinkedHashSet<>();
        Set<Long> wantedIds = new LinkedHashSet<>();
        Set<Long> orderIds = new LinkedHashSet<>();
        for (Report report : reports) {
            add(userIds, report.getReporterId());
            add(userIds, report.getTargetUserId());
            add(userIds, report.getHandlerId());
            add(itemIds, report.getItemId());
            add(wantedIds, report.getWantedId());
            add(orderIds, report.getOrderId());
        }
        Map<Long, User> users = mapUsers(userIds);
        Map<Long, Item> items = mapItems(itemIds);
        Map<Long, Wanted> wanted = mapWanted(wantedIds);
        Map<Long, TradeOrder> orders = mapOrders(orderIds);
        return reports.stream()
                .map(AdminReportVO::from)
                .map(vo -> enrich(vo, users, items, wanted, orders))
                .toList();
    }

    private AdminReportVO enrich(AdminReportVO vo, Map<Long, User> users, Map<Long, Item> items,
                                 Map<Long, Wanted> wanted, Map<Long, TradeOrder> orders) {
        User reporter = find(users, vo.getReporterId());
        if (reporter != null) vo.setReporterUsername(displayUser(reporter));
        User target = find(users, vo.getTargetUserId());
        if (target != null) vo.setTargetUserUsername(displayUser(target));
        User handler = find(users, vo.getHandlerId());
        if (handler != null) vo.setHandlerUsername(displayUser(handler));
        Item item = find(items, vo.getItemId());
        if (item != null) vo.setItemTitle(item.getTitle());
        Wanted wantedItem = find(wanted, vo.getWantedId());
        if (wantedItem != null) vo.setWantedTitle(wantedItem.getTitle());
        TradeOrder order = find(orders, vo.getOrderId());
        if (order != null) vo.setOrderNo(order.getOrderNo());
        return vo;
    }

    private <T> T find(Map<Long, T> values, Long id) {
        return id == null ? null : values.get(id);
    }

    private Map<Long, User> mapUsers(Set<Long> ids) {
        if (userMapper == null || ids.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(List.copyOf(ids)).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, Item> mapItems(Set<Long> ids) {
        if (itemMapper == null || ids.isEmpty()) return Map.of();
        return itemMapper.selectBatchIds(List.copyOf(ids)).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, Wanted> mapWanted(Set<Long> ids) {
        if (wantedMapper == null || ids.isEmpty()) return Map.of();
        return wantedMapper.selectBatchIds(List.copyOf(ids)).stream()
                .collect(Collectors.toMap(Wanted::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, TradeOrder> mapOrders(Set<Long> ids) {
        if (tradeOrderMapper == null || ids.isEmpty()) return Map.of();
        return tradeOrderMapper.selectBatchIds(List.copyOf(ids)).stream()
                .collect(Collectors.toMap(TradeOrder::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private Set<Long> ids(Long... values) {
        Set<Long> ids = new LinkedHashSet<>();
        for (Long value : values) add(ids, value);
        return ids;
    }

    private void add(Set<Long> ids, Long value) {
        if (value != null) ids.add(value);
    }

    private String displayUser(User user) {
        return Objects.requireNonNullElse(user.getUsername(), Objects.requireNonNullElse(user.getNickname(), "用户" + user.getId()));
    }
}
