package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.entity.ChatMessage;
import com.yatidle.backend.entity.ChatSession;
import com.yatidle.backend.mapper.ChatMessageMapper;
import com.yatidle.backend.mapper.ChatSessionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminChatAuditService {
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;

    public AdminChatAuditService(ChatSessionMapper chatSessionMapper, ChatMessageMapper chatMessageMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    public Page<ChatSession> sessions(Long userId, Long itemId, Long wantedId, int page, int size) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getIsDeleted, 0);
        if (userId != null) wrapper.and(w -> w.eq(ChatSession::getBuyerId, userId).or().eq(ChatSession::getSellerId, userId));
        if (itemId != null) wrapper.eq(ChatSession::getItemId, itemId);
        if (wantedId != null) wrapper.eq(ChatSession::getWantedId, wantedId);
        wrapper.orderByDesc(ChatSession::getLastMessageTime);
        return chatSessionMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<ChatMessage> messages(Long sessionId) {
        return chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSessionId, sessionId).eq(ChatMessage::getIsDeleted, 0).orderByAsc(ChatMessage::getCreateTime));
    }
}
