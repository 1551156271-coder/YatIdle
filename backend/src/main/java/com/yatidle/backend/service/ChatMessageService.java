package com.yatidle.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yatidle.backend.entity.ChatMessage;
import com.yatidle.backend.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessage> {
}
