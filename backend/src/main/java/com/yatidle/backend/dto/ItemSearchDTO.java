package com.yatidle.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemSearchDTO {
    private String keyword;
    private Long categoryId;
    private String campus;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
