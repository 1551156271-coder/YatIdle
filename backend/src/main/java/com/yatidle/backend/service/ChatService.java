package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yatidle.backend.dto.chat.CreateChatSessionDTO;
import com.yatidle.backend.dto.chat.SendMessageDTO;
import com.yatidle.backend.entity.ChatMessage;
import com.yatidle.backend.entity.ChatSession;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.enums.MessageTypeEnum;
import com.yatidle.backend.mapper.ChatMessageMapper;
import com.yatidle.backend.mapper.ChatSessionMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.vo.chat.ChatMessageVO;
import com.yatidle.backend.vo.chat.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ItemMapper itemMapper;

    @Transactional
    public ChatSessionVO createChatSession(CreateChatSessionDTO dto, Long currentUserId) {
        if(dto == null || dto.getItemId() == null){
            throw new RuntimeException("商品ID不能为空");
        }

        Item item = itemMapper.selectById(dto.getItemId());

        if(item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)){
            throw new RuntimeException("商品不存在");
        }

        Long sellerId = item.getUserId();

        if(sellerId.equals(currentUserId)){
            throw new RuntimeException("不能与自己进行对话");
        }

        ChatSession exist = chatSessionMapper.selectOne(
                new LambdaQueryWrapper<ChatSession>()
                    .eq(ChatSession::getItemId, item.getId())
                    .eq(ChatSession::getBuyerId, currentUserId)
                    .eq(ChatSession::getSellerId, sellerId)
                    .eq(ChatSession::getIsDeleted, 0)
        );

        if (exist != null){
            return toSessionVO(exist, item, currentUserId);
        }

        ChatSession session = new ChatSession();
        session.setItemId(item.getId());
        session.setBuyerId(currentUserId);
        session.setSellerId(sellerId);
        session.setBuyerUnreadCount(0);
        session.setSellerUnreadCount(0);
        session.setIsDeleted(0);

        chatSessionMapper.insert(session);

        return toSessionVO(session, item, currentUserId);
    }

    public List<ChatSessionVO> listMySessions(Long currentUserId) {
        List<ChatSession> sessions = chatSessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                    .and(wrapper -> wrapper
                        .eq(ChatSession::getBuyerId, currentUserId)
                        .or()
                        .eq(ChatSession::getSellerId, currentUserId)
                    )
                        .eq(ChatSession::getIsDeleted, 0)
                        .orderByDesc(ChatSession::getLastMessageTime)
        );

        List<ChatSessionVO> result = new ArrayList<>();

        for(ChatSession session : sessions)
        {
            Item item = itemMapper.selectById(session.getItemId());
            result.add(toSessionVO(session, item, currentUserId));
        }
        return result;
    }

    @Transactional
    public ChatMessageVO sendMessage(SendMessageDTO dto, Long currentUserId) {
        if(dto == null || dto.getSessionId() == null){
            throw new RuntimeException("会话ID不能为空");
        }
        if(dto.getContent() == null || dto.getContent().trim().isEmpty()){
            throw new RuntimeException("消息内容不能为空");
        }

        ChatSession session = chatSessionMapper.selectById(dto.getSessionId());
        if(session == null || (session.getIsDeleted() != null && session.getIsDeleted() == 1)){
            throw new RuntimeException("会话不存在");
        }

        boolean isBuyer = session.getBuyerId().equals(currentUserId);
        boolean isSeller = session.getSellerId().equals(currentUserId);
        if(!isBuyer && !isSeller){
            throw new RuntimeException("无权发送该会话消息");
        }

        Long receiverId = isBuyer ? session.getSellerId() : session.getBuyerId();

        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setSenderId(currentUserId);
        message.setReceiverId(receiverId);
        message.setMessageType(MessageTypeEnum.TEXT.name());
        message.setContent(dto.getContent().trim());
        message.setReadFlag(0);
        message.setIsDeleted(0);

        chatMessageMapper.insert(message);

        session.setLastMessage(message.getContent());
        session.setLastSenderId(message.getSenderId());
        session.setLastMessageTime(LocalDateTime.now());

        if(isBuyer){
            session.setSellerUnreadCount(session.getSellerUnreadCount() + 1);
        }
        else{
            session.setBuyerUnreadCount(session.getBuyerUnreadCount() + 1);
        }

        chatSessionMapper.updateById(session);

        return toMessageVO(message);
    }

    public List<ChatMessageVO> listMessages(Long sessionId, Long currentUserId) {
        if(sessionId == null){
            throw new RuntimeException("会话ID不能为空");
        }

        ChatSession session = chatSessionMapper.selectById(sessionId);
        if(session == null || (session.getIsDeleted() != null && session.getIsDeleted() == 1)){
            throw new RuntimeException("会话不存在");
        }

        boolean isBuyer = session.getBuyerId().equals(currentUserId);
        boolean isSeller = session.getSellerId().equals(currentUserId);
        if(!isBuyer && !isSeller){
            throw new RuntimeException("无权查看该会话消息");
        }

        List<ChatMessage> messages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getSessionId, sessionId)
                    .eq(ChatMessage::getIsDeleted, 0)
                        .orderByAsc(ChatMessage::getCreateTime)
        );

        List<ChatMessageVO> result = new ArrayList<>();
        for(ChatMessage message : messages)
        {
            result.add(toMessageVO(message));
        }
        return result;
    }

    @Transactional
    public void markAsRead(Long sessionId, Long currentUserId){
        if(sessionId == null){
            throw new RuntimeException("会话ID不能为空");
        }
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if(session == null || (session.getIsDeleted() != null && session.getIsDeleted() == 1)){
            throw new RuntimeException("会话不存在");
        }

        boolean isBuyer = session.getBuyerId().equals(currentUserId);
        boolean isSeller = session.getSellerId().equals(currentUserId);
        if(!isBuyer && !isSeller){
            throw new RuntimeException("无权操作该会话");
        }
        if(isBuyer){
            session.setBuyerUnreadCount(0);
        }
        else{
            session.setSellerUnreadCount(0);
        }

        chatSessionMapper.updateById(session);
    }

    private ChatSessionVO toSessionVO(ChatSession session, Item item, Long currentUserId){
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setItemId(item.getId());
        vo.setItemTitle(item.getTitle());
        vo.setBuyerId(session.getBuyerId());
        vo.setSellerId(session.getSellerId());
        vo.setLastMessage(session.getLastMessage());
        vo.setLastSenderId(session.getLastSenderId());
        vo.setLastMessageTime(session.getLastMessageTime());

        if(session.getBuyerId().equals(currentUserId)){
            vo.setUnreadCount(session.getBuyerUnreadCount());
        }
        else{
            vo.setUnreadCount(session.getSellerUnreadCount());
        }
        return vo;
    }

    private ChatMessageVO toMessageVO(ChatMessage message){
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setMessageType(message.getMessageType());
        vo.setContent(message.getContent());
        vo.setReadFlag(message.getReadFlag());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
}
