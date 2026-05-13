package com.yatidle.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yatidle.backend.entity.ChatSession;
import com.yatidle.backend.mapper.ChatSessionMapper;
import org.springframework.stereotype.Service;

@Service
public class ChatSessionService extends ServiceImpl<ChatSessionMapper, ChatSession> {
}
