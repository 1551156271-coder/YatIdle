package com.yatidle.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yatidle.backend.dto.order.CancelOrderDTO;
import com.yatidle.backend.dto.order.CreateOrderDTO;
import com.yatidle.backend.entity.TradeOrder;
import com.yatidle.backend.mapper.TradeOrderMapper;
import com.yatidle.backend.vo.order.TradeOrderVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TradeOrderService extends ServiceImpl<TradeOrderMapper, TradeOrder> {
    public TradeOrderVO createOrder(CreateOrderDTO createOrderDTO){
        return null;
    }

    public List<TradeOrderVO> getMyBuyOrders(Long userId){
        return new ArrayList<>();
    }

    public List<TradeOrderVO> getMySellOrders(Long userId){
        return new ArrayList<>();
    }

    public void cancelOrder(Long orderId, CancelOrderDTO cancelOrderDTO, Long currentUserId){
    }

    public void completeOrder(Long orderId, Long currentUserId){
    }
    @Resource
    private TradeOrderMapper tradeOrderMapper;

    public Long countOrders() {
        return tradeOrderMapper.selectCount(null);
    }
}