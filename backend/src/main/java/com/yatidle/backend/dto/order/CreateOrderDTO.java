package com.yatidle.backend.dto.order;

import lombok.Data;

@Data
public class CreateOrderDTO{
    private Long itemId;
    private String tradeLocation;
    private String remark;
}