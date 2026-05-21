package com.yatidle.backend.dto.wanted;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateWantedDTO {
    private Long userId;
    private String title;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String campus;
    private String conditionLevel;
    private String description;
    private Long categoryId;
    private List<String> imageUrls;
}
