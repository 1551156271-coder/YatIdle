package com.yatidle.backend.dto.chat;

import lombok.Data;

@Data
public class SendMessageDTO {
    private Long sessionId;
    private String content;
}
