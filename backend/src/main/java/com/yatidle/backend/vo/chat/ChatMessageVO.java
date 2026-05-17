package com.yatidle.backend.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {
    private Long id;

    private Long sessionId;

    private Long senderId;

    private Long reiveicerId;

    private String messageType;

    private String content;

    private Integer readFlag;

    private LocalDateTime createTime;
}
