package com.yatidle.backend.dto.report;

import lombok.Data;

import java.util.List;

@Data
public class CreateReportDTO {
    private Long reporterId;
    private Long targetUserId;
    private Long itemId;
    private Long wantedId;
    private Long orderId;
    private Long chatSessionId;
    private String reason;
    private String description;
    private List<String> imageUrls;
}
