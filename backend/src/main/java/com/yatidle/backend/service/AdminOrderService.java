package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.TradeOrder;
import com.yatidle.backend.entity.TradeOrderLog;
import com.yatidle.backend.mapper.TradeOrderLogMapper;
import com.yatidle.backend.mapper.TradeOrderMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminOrderService {
    private final TradeOrderMapper tradeOrderMapper;
    private final TradeOrderLogMapper tradeOrderLogMapper;
    private final AdminLogService adminLogService;

    public AdminOrderService(TradeOrderMapper tradeOrderMapper, TradeOrderLogMapper tradeOrderLogMapper, AdminLogService adminLogService) {
        this.tradeOrderMapper = tradeOrderMapper;
        this.tradeOrderLogMapper = tradeOrderLogMapper;
        this.adminLogService = adminLogService;
    }

    public Page<TradeOrder> list(String status, Long userId, Long itemId, int page, int size) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrder::getIsDeleted, 0);
        if (status != null && !status.isBlank()) wrapper.eq(TradeOrder::getStatus, status);
        if (userId != null) wrapper.and(w -> w.eq(TradeOrder::getBuyerId, userId).or().eq(TradeOrder::getSellerId, userId));
        if (itemId != null) wrapper.eq(TradeOrder::getItemId, itemId);
        wrapper.orderByDesc(TradeOrder::getCreateTime);
        return tradeOrderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public TradeOrder detail(Long id) {
        TradeOrder order = tradeOrderMapper.selectById(id);
        if (order == null || (order.getIsDeleted() != null && order.getIsDeleted() == 1)) throw new BusinessException("订单不存在");
        return order;
    }

    public List<TradeOrderLog> logs(Long id) {
        return tradeOrderLogMapper.selectList(new LambdaQueryWrapper<TradeOrderLog>().eq(TradeOrderLog::getOrderId, id).orderByDesc(TradeOrderLog::getCreateTime));
    }

    public void cancel(Long adminId, Long id, String reason) {
        requireReason(reason);
        TradeOrder order = detail(id);
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
}
