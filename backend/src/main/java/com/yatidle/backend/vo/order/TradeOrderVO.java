package com.yatidle.backend.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeOrderVO {
    private Long id;

    private String orderNo;

    private Long itemId;

    private Long buyerId;

    private Long sellerId;

    private BigDecimal price;

    private String status;

    private String tradeLocation;

    private String remark;

    private String cancelReason;

    private LocalDateTime createTime;

    private LocalDateTime cancelTime;

    private LocalDateTime completeTime;

    private Boolean hasReviewed;

    private String itemTitle;

    private String itemImageUrl;

    private String buyerName;

    private String sellerName;

    private String sellerAvatar;
}
