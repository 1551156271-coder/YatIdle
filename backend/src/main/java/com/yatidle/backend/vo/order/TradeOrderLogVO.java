package com.yatidle.backend.vo.order;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TradeOrderLogVO {
    private Long id;

    private String action;

    private String beforeStatus;

    private String afterStatus;

    private Long operatorId;

    private String remark;

    private LocalDateTime createdTime;
}

