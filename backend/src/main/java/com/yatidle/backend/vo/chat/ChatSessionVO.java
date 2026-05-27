package com.yatidle.backend.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionVO {
    private Long id;

    private Long itemId;

    private String itemTitle;

    private String itemImage;

    private Long wantedId;

    private String wantedTitle;

    private Long buyerId;

    private Long sellerId;

    private String lastMessage;

    private Long lastSenderId;

    private LocalDateTime lastMessageTime;

    private Integer unreadCount;

    private String partnerName;

    private String partnerAvatar;
}
