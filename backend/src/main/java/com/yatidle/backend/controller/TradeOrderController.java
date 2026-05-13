package com.yatidle.backend.controller;

import com.yatidle.backend.service.TradeOrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.Result;

@RestController
public class TradeOrderController {

    @Resource
    private TradeOrderService tradeOrderService;

    @GetMapping("/api/orders/test")
    public String test() {
        Long count = tradeOrderService.countOrders();
        return "trade_order count = " + count;
    }
}
