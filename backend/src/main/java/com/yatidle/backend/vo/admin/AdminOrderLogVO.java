package com.yatidle.backend.vo.admin;

import com.yatidle.backend.entity.TradeOrderLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOrderLogVO {
    private Long id;
    private Long orderId;
    private String action;
    private String actionText;
    private String beforeStatus;
    private String beforeStatusText;
    private String afterStatus;
    private String afterStatusText;
    private Long operatorId;
    private String operatorUsername;
    private String remark;
    private LocalDateTime createTime;

    public static AdminOrderLogVO from(TradeOrderLog log) {
        AdminOrderLogVO vo = new AdminOrderLogVO();
        vo.setId(log.getId());
        vo.setOrderId(log.getOrderId());
        vo.setAction(log.getAction());
        vo.setActionText(AdminText.actionText(log.getAction()));
        vo.setBeforeStatus(log.getBeforeStatus());
        vo.setBeforeStatusText(AdminText.statusText(log.getBeforeStatus()));
        vo.setAfterStatus(log.getAfterStatus());
        vo.setAfterStatusText(AdminText.statusText(log.getAfterStatus()));
        vo.setOperatorId(log.getOperatorId());
        vo.setRemark(log.getRemark());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }
}
