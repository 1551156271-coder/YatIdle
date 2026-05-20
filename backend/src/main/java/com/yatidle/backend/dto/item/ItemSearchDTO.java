package com.yatidle.backend.dto.item;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemSearchDTO {
    private String keyword;
    private Long categoryId;
    private String campus;
    private String conditionLevel;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String priceSort;
}
