package com.yatidle.backend.dto.order;

import lombok.Data;

@Data
public class CreateOrderDTO{
    private Long itemID;
    private String tradeLocation;
    private String remark;
}