package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.order.CancelOrderDTO;
import com.yatidle.backend.dto.order.CreateOrderDTO;
import com.yatidle.backend.service.TradeOrderService;
import com.yatidle.backend.vo.order.TradeOrderVO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    @PostMapping
    public Result<TradeOrderVO> createOrder(@RequestBody CreateOrderDTO dto) {
        Long currentUserId = 1L;
        TradeOrderVO vo = tradeOrderService.createOrder(dto, currentUserId);;
        return Result.success(vo);
    }
    @GetMapping("/my-buy")
    public Result<List<TradeOrderVO>> listMyBuyOrders() {
        Long currentUserId = 1L;
        List<TradeOrderVO> list = tradeOrderService.listMyBuyOrders(currentUserId);
        return Result.success(list);
    }

    @GetMapping("/my-sell")
    public Result<List<TradeOrderVO>> listMySellOrders() {
        Long currentUserId = 1L;
        List<TradeOrderVO> list = tradeOrderService.listMySellOrders(currentUserId);
        return Result.success(list);
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
