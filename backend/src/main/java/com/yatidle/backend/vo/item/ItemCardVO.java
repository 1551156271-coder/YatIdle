package com.yatidle.backend.vo.item;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ItemCardVO {
    private Long id;
    private String title;
    private String campus;
    private BigDecimal price;
    private String conditionLevel;
    private String imageUrl;       // 封面图（第一张）
    private String status;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createTime;
}
