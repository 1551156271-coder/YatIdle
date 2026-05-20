package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.order.CancelOrderDTO;
import com.yatidle.backend.dto.order.CreateOrderDTO;
import com.yatidle.backend.service.TradeOrderService;
import com.yatidle.backend.vo.PageVO;
import com.yatidle.backend.vo.order.TradeOrderVO;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    @PostMapping
    public Result<TradeOrderVO> createOrder(
            @RequestBody CreateOrderDTO dto,
            @RequestParam Long userId) {

        TradeOrderVO vo = tradeOrderService.createOrder(dto, userId);;
        return Result.success(vo);
    }
    @GetMapping("/my-buy")
    public Result<PageVO<TradeOrderVO>> listMyBuyOrders(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {

        PageVO<TradeOrderVO> page = tradeOrderService.listMyBuyOrders(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/my-sell")
    public Result<PageVO<TradeOrderVO>> listMySellOrders(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {

        PageVO<TradeOrderVO> page = tradeOrderService.listMySellOrders(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/{id}/cancel")
    public Result<TradeOrderVO> cancelOrder(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestBody(required = false) CancelOrderDTO dto) {


        TradeOrderVO vo = tradeOrderService.cancelOrder(id, dto, userId);
        return Result.success(vo);
    }

    @PutMapping("/{id}/complete")
    public Result<TradeOrderVO> completeOrder(
            @PathVariable Long id,
            @RequestParam Long userId) {

        TradeOrderVO vo = tradeOrderService.completeOrder(id, userId);
        return Result.success(vo);
    }
}
