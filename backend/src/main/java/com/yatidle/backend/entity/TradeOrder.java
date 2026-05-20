package com.yatidle.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class TradeOrder {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("item_id")
    private Long itemId;

    @TableField("order_no")
    private String orderNo;

    @TableField("buyer_id")
    private Long buyerId;

    @TableField("seller_id")
    private Long sellerId;

    @TableField("price")
    private BigDecimal price;

    @TableField("status")
    private String status;

    @TableField("trade_location")
    private String tradeLocation;

    @TableField("remark")
    private String remark;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    @TableField("complete_time")
    private LocalDateTime completeTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}
