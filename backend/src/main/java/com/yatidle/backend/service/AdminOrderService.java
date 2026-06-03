package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.TradeOrder;
import com.yatidle.backend.entity.TradeOrderLog;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.TradeOrderLogMapper;
import com.yatidle.backend.mapper.TradeOrderMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.vo.admin.AdminOrderLogVO;
import com.yatidle.backend.vo.admin.AdminOrderVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminOrderService {
    private final TradeOrderMapper tradeOrderMapper;
    private final TradeOrderLogMapper tradeOrderLogMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final AdminLogService adminLogService;

    public AdminOrderService(TradeOrderMapper tradeOrderMapper,
                             TradeOrderLogMapper tradeOrderLogMapper,
                             ItemMapper itemMapper,
                             UserMapper userMapper,
                             AdminLogService adminLogService) {
        this.tradeOrderMapper = tradeOrderMapper;
        this.tradeOrderLogMapper = tradeOrderLogMapper;
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
        this.adminLogService = adminLogService;
    }

    public Page<AdminOrderVO> list(String status, Long userId, Long itemId, int page, int size) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrder::getIsDeleted, 0);
        if (status != null && !status.isBlank()) wrapper.eq(TradeOrder::getStatus, status);
        if (userId != null) wrapper.and(w -> w.eq(TradeOrder::getBuyerId, userId).or().eq(TradeOrder::getSellerId, userId));
        if (itemId != null) wrapper.eq(TradeOrder::getItemId, itemId);
        wrapper.orderByDesc(TradeOrder::getCreateTime);
        return toVOPage(tradeOrderMapper.selectPage(new Page<>(page, size), wrapper));
    }

    public AdminOrderVO detail(Long id) {
        return enrich(AdminOrderVO.from(findOrder(id)), Map.of(), Map.of());
    }

    private TradeOrder findOrder(Long id) {
        TradeOrder order = tradeOrderMapper.selectById(id);
        if (order == null || (order.getIsDeleted() != null && order.getIsDeleted() == 1)) throw new BusinessException("订单不存在");
        return order;
    }

    public List<AdminOrderLogVO> logs(Long id) {
        List<TradeOrderLog> logs = tradeOrderLogMapper.selectList(new LambdaQueryWrapper<TradeOrderLog>().eq(TradeOrderLog::getOrderId, id).orderByDesc(TradeOrderLog::getCreateTime));
        Set<Long> userIds = logs.stream().map(TradeOrderLog::getOperatorId).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, User> users = mapUsers(userIds);
        return logs.stream().map(AdminOrderLogVO::from).peek(vo -> {
            User user = users.get(vo.getOperatorId());
            if (user != null) vo.setOperatorUsername(displayUser(user));
        }).toList();
    }

    public void cancel(Long adminId, Long id, String reason) {
        requireReason(reason);
        TradeOrder order = findOrder(id);
        String before = order.getStatus();
        order.setStatus("CANCELLED");
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
        adminLogService.log(adminId, "CANCEL_ORDER", "ORDER", id, before, "CANCELLED", reason);
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) throw new BusinessException("操作原因不能为空");
    }

    private Page<AdminOrderVO> toVOPage(Page<TradeOrder> source) {
        Page<AdminOrderVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setPages(source.getPages());
        result.setRecords(enrichOrders(source.getRecords()));
        return result;
    }

    private List<AdminOrderVO> enrichOrders(List<TradeOrder> orders) {
        Set<Long> itemIds = new LinkedHashSet<>();
        Set<Long> userIds = new LinkedHashSet<>();
        for (TradeOrder order : orders) {
            add(itemIds, order.getItemId());
            add(userIds, order.getBuyerId());
            add(userIds, order.getSellerId());
        }
        Map<Long, Item> items = mapItems(itemIds);
        Map<Long, User> users = mapUsers(userIds);
        return orders.stream().map(AdminOrderVO::from).map(vo -> enrich(vo, items, users)).toList();
    }

    private AdminOrderVO enrich(AdminOrderVO vo, Map<Long, Item> items, Map<Long, User> users) {
        Map<Long, Item> itemMap = items.isEmpty() && itemMapper != null && vo.getItemId() != null ? mapItems(Set.of(vo.getItemId())) : items;
        Map<Long, User> userMap = users.isEmpty() && userMapper != null ? mapUsers(ids(vo.getBuyerId(), vo.getSellerId())) : users;
        Item item = itemMap.get(vo.getItemId());
        if (item != null) vo.setItemTitle(item.getTitle());
        User buyer = userMap.get(vo.getBuyerId());
        if (buyer != null) vo.setBuyerUsername(displayUser(buyer));
        User seller = userMap.get(vo.getSellerId());
        if (seller != null) vo.setSellerUsername(displayUser(seller));
        return vo;
    }

    private Map<Long, Item> mapItems(Set<Long> ids) {
        if (itemMapper == null || ids.isEmpty()) return Map.of();
        return itemMapper.selectBatchIds(List.copyOf(ids)).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, User> mapUsers(Set<Long> ids) {
        if (userMapper == null || ids.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(List.copyOf(ids)).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
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
