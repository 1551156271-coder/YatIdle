package com.yatidle.backend.vo.favorite;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteVO {
    private Long id;

    private Long itemId;

    private String itemTitle;

    private BigDecimal price;

    private String coverImage;

    private String itemStatus;

    private LocalDateTime createTime;
}
