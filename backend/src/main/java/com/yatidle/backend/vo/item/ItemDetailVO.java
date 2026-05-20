package com.yatidle.backend.vo.item;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemDetailVO {
    private Long id;
    private Long userId;
    private String title;
    private String campus;
    private String conditionLevel;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String status;
    private Integer viewCount;
    private Integer favoriteCount;
    private List<String> imageUrls;    // 所有图片
    private LocalDateTime createTime;
}
