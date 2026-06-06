package com.yatidle.backend.vo.admin;

import com.yatidle.backend.entity.Report;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReportVO {
    private Long id;
    private Long reporterId;
    private String reporterUsername;
    private Long targetUserId;
    private String targetUserUsername;
    private Long itemId;
    private String itemTitle;
    private Long wantedId;
    private String wantedTitle;
    private Long orderId;
    private String orderNo;
    private Long chatSessionId;
    private String reason;
    private String description;
    private String imageUrls;
    private String status;
    private String handleResult;
    private Long handlerId;
    private String handlerUsername;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static AdminReportVO from(Report report) {
        AdminReportVO vo = new AdminReportVO();
        vo.setId(report.getId());
        vo.setReporterId(report.getReporterId());
        vo.setTargetUserId(report.getTargetUserId());
        vo.setItemId(report.getItemId());
        vo.setWantedId(report.getWantedId());
        vo.setOrderId(report.getOrderId());
        vo.setChatSessionId(report.getChatSessionId());
        vo.setReason(report.getReason());
        vo.setDescription(report.getDescription());
        vo.setImageUrls(report.getImageUrls());
        vo.setStatus(report.getStatus());
        vo.setHandleResult(report.getHandleResult());
        vo.setHandlerId(report.getHandlerId());
        vo.setHandleTime(report.getHandleTime());
        vo.setCreateTime(report.getCreateTime());
        vo.setUpdateTime(report.getUpdateTime());
        return vo;
    }
}
