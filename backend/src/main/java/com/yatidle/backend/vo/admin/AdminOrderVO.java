package com.yatidle.backend.vo.admin;

import com.yatidle.backend.entity.TradeOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderVO {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private String orderNo;
    private Long buyerId;
    private String buyerUsername;
    private Long sellerId;
    private String sellerUsername;
    private BigDecimal price;
    private String status;
    private String tradeLocation;
    private String remark;
    private String cancelReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime cancelTime;
    private LocalDateTime completeTime;

    public static AdminOrderVO from(TradeOrder order) {
        AdminOrderVO vo = new AdminOrderVO();
        vo.setId(order.getId());
        vo.setItemId(order.getItemId());
        vo.setOrderNo(order.getOrderNo());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setPrice(order.getPrice());
        vo.setStatus(order.getStatus());
        vo.setTradeLocation(order.getTradeLocation());
        vo.setRemark(order.getRemark());
        vo.setCancelReason(order.getCancelReason());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCompleteTime(order.getCompleteTime());
        return vo;
    }
}
