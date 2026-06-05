package com.yatidle.backend.vo.admin;

import com.yatidle.backend.entity.AdminActionLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminActionLogVO {
    private Long id;
    private Long adminId;
    private String adminUsername;
    private String action;
    private String actionText;
    private String targetType;
    private String targetTypeText;
    private Long targetId;
    private String targetName;
    private String beforeStatus;
    private String beforeStatusText;
    private String afterStatus;
    private String afterStatusText;
    private String remark;
    private LocalDateTime createTime;

    public static AdminActionLogVO from(AdminActionLog log) {
        AdminActionLogVO vo = new AdminActionLogVO();
        vo.setId(log.getId());
        vo.setAdminId(log.getAdminId());
        vo.setAction(log.getAction());
        vo.setActionText(AdminText.actionText(log.getAction()));
        vo.setTargetType(log.getTargetType());
        vo.setTargetTypeText(AdminText.targetTypeText(log.getTargetType()));
        vo.setTargetId(log.getTargetId());
        vo.setBeforeStatus(log.getBeforeStatus());
        vo.setBeforeStatusText(AdminText.statusText(log.getBeforeStatus(), log.getAction(), log.getTargetType()));
        vo.setAfterStatus(log.getAfterStatus());
        vo.setAfterStatusText(AdminText.statusText(log.getAfterStatus(), log.getAction(), log.getTargetType()));
        vo.setRemark(log.getRemark());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }
}
