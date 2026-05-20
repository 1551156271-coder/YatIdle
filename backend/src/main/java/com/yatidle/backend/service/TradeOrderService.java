package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yatidle.backend.dto.order.CancelOrderDTO;
import com.yatidle.backend.dto.order.CreateOrderDTO;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.TradeOrder;
import com.yatidle.backend.entity.TradeOrderLog;
import com.yatidle.backend.enums.OrderLogActionEnum;
import com.yatidle.backend.enums.OrderStatusEnum;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.TradeOrderLogMapper;
import com.yatidle.backend.mapper.TradeOrderMapper;
import com.yatidle.backend.vo.PageVO;
import com.yatidle.backend.vo.order.TradeOrderVO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeOrderService extends ServiceImpl<TradeOrderMapper, TradeOrder> {

    private final TradeOrderMapper tradeOrderMapper;
    private final TradeOrderLogMapper tradeOrderLogMapper;
    private final ItemMapper itemMapper;

    @Transactional
    public TradeOrderVO createOrder(CreateOrderDTO dto, Long currentUserId){
        if(dto ==  null || dto.getItemId() == null){
            throw new RuntimeException("商品ID不能为空");
        }

        Item item = itemMapper.selectById(dto.getItemId());
        if(item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1){
            throw new RuntimeException("商品不存在");
        }
        if(!"ON_SALE".equals(item.getStatus())){
            throw new RuntimeException("商品当前不可交易");
        }

        Long pendingCount = tradeOrderMapper.selectCount(
                new LambdaQueryWrapper<TradeOrder>()
                        .eq(TradeOrder::getItemId, item.getId())
                        .eq(TradeOrder::getStatus, OrderStatusEnum.PENDING.name())
                        .eq(TradeOrder::getIsDeleted, 0)
        );

        if (pendingCount > 0) {
            throw new RuntimeException("该商品已有待交易订单");
        }

        if(item.getUserId().equals(currentUserId)){
            throw new RuntimeException("不能购买自己发布的商品");
        }

        TradeOrder order = new TradeOrder();
        order.setItemId(item.getId());
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(currentUserId);
        order.setSellerId(item.getUserId());
        order.setPrice(item.getPrice());
        order.setStatus(OrderStatusEnum.PENDING.name());
        order.setTradeLocation(dto.getTradeLocation());
        order.setRemark(dto.getRemark());
        order.setIsDeleted(0);

        tradeOrderMapper.insert(order);

        TradeOrderLog log = new TradeOrderLog();
        log.setOrderId(order.getId());
        log.setAction(OrderLogActionEnum.CREATE.name());
        log.setBeforeStatus(null);
        log.setAfterStatus(OrderStatusEnum.PENDING.name());
        log.setOperatorId(currentUserId);
        log.setRemark("创建订单");

        tradeOrderLogMapper.insert(log);

        return toVO(order);
    }

    public PageVO<TradeOrderVO> listMyBuyOrders(Long currentUserId, Long pageNum, Long pageSize){
        Page<TradeOrder> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrder::getBuyerId, currentUserId)
                .eq(TradeOrder::getIsDeleted, 0)
                .orderByDesc(TradeOrder::getCreateTime);

        Page<TradeOrder> resultPage = tradeOrderMapper.selectPage(page,wrapper);

        List<TradeOrderVO> records = resultPage.getRecords()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        PageVO<TradeOrderVO> vo = new PageVO<>();
        vo.setTotal(resultPage.getTotal());
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setRecords(records);

        return vo;
    }

    public PageVO<TradeOrderVO> listMySellOrders(Long currentUserId, Long pageNum, Long pageSize){
        Page<TradeOrder> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TradeOrder::getSellerId, currentUserId)
                .eq(TradeOrder::getIsDeleted, 0)
                .orderByDesc(TradeOrder::getCreateTime);

        Page<TradeOrder> resultPage = tradeOrderMapper.selectPage(page,wrapper);

        List<TradeOrderVO> records = resultPage.getRecords()
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        PageVO<TradeOrderVO> vo = new PageVO<>();
        vo.setTotal(resultPage.getTotal());
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setRecords(records);

        return vo;
    }

    public TradeOrderVO cancelOrder(Long orderId, CancelOrderDTO dto, Long currentUserId){
        if(orderId == null){
            throw new RuntimeException("订单ID不能为空");
        }

        TradeOrder order = tradeOrderMapper.selectById(orderId);

        if(order == null || (order.getIsDeleted() != null && order.getIsDeleted() == 1)){
            throw new RuntimeException("订单不存在");
        }

        if (!OrderStatusEnum.PENDING.name().equals(order.getStatus())) {
            throw new RuntimeException("当前订单状态不能取消");
        }

        boolean isBuyer = order.getBuyerId().equals(currentUserId);
        boolean isSeller = order.getSellerId().equals(currentUserId);

        if (!isBuyer && !isSeller) {
            throw new RuntimeException("无权取消该订单");
        }

        String beforeStatus = order.getStatus();

        order.setStatus(OrderStatusEnum.CANCELLED.name());
        order.setCancelReason(dto == null ? null : dto.getCancelReason());
        order.setCancelTime(LocalDateTime.now());

        tradeOrderMapper.updateById(order);

        TradeOrderLog log = new TradeOrderLog();
        log.setOrderId(order.getId());
        log.setAction(OrderLogActionEnum.CANCEL.name());
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(OrderStatusEnum.CANCELLED.name());
        log.setOperatorId(currentUserId);
        log.setRemark(dto == null ? "取消订单" : dto.getCancelReason());

        tradeOrderLogMapper.insert(log);

        return toVO(order);
    }

    public TradeOrderVO completeOrder(Long orderId, Long currentUserId){
        if(orderId == null){
            throw new RuntimeException("订单ID不能为空");
        }

        TradeOrder order = tradeOrderMapper.selectById(orderId);

        if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
            throw new RuntimeException("订单不存在");
        }

        if (!OrderStatusEnum.PENDING.name().equals(order.getStatus())) {
            throw new RuntimeException("当前订单状态不能完成");
        }

        boolean isBuyer = order.getBuyerId().equals(currentUserId);
        boolean isSeller = order.getSellerId().equals(currentUserId);

        if (!isBuyer && !isSeller) {
            throw new RuntimeException("无权完成该订单");
        }

        Item item = itemMapper.selectById(order.getItemId());

        if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1) {
            throw new RuntimeException("商品不存在");
        }

        String beforeStatus = order.getStatus();

        order.setStatus(OrderStatusEnum.COMPLETED.name());
        order.setCompleteTime(LocalDateTime.now());

        tradeOrderMapper.updateById(order);

        item.setStatus("SOLD");
        itemMapper.updateById(item);

        TradeOrderLog log = new TradeOrderLog();
        log.setOrderId(order.getId());
        log.setAction(OrderLogActionEnum.COMPLETE.name());
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(OrderStatusEnum.COMPLETED.name());
        log.setOperatorId(currentUserId);
        log.setRemark("完成交易");

        tradeOrderLogMapper.insert(log);

        return toVO(order);
    }


    public Long countOrders() {
        return tradeOrderMapper.selectCount(null);
    }

    private TradeOrderVO toVO(TradeOrder order){
        TradeOrderVO vo = new TradeOrderVO();
        vo.setId(order.getId());
        vo.setItemId(order.getItemId());
        vo.setOrderNo(order.getOrderNo());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setPrice(order.getPrice());
        vo.setStatus(order.getStatus());
        vo.setTradeLocation(order.getTradeLocation());
        vo.setRemark(order.getRemark());
        vo.setCancelReason(order.getCancelReason());
        vo.setCreateTime(order.getCreateTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCompleteTime(order.getCompleteTime());
        return vo;
    }
    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 9000) + 1000;
        return "YI" + time + random;
    }
}