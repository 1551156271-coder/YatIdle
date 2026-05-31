package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yatidle.backend.dto.chat.CreateChatSessionDTO;
import com.yatidle.backend.dto.chat.SendMessageDTO;
import com.yatidle.backend.entity.ChatMessage;
import com.yatidle.backend.entity.ChatSession;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.entity.Wanted;
import com.yatidle.backend.enums.MessageTypeEnum;
import com.yatidle.backend.mapper.ChatMessageMapper;
import com.yatidle.backend.mapper.ChatSessionMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.mapper.WantedMapper;
import com.yatidle.backend.vo.chat.ChatMessageVO;
import com.yatidle.backend.vo.chat.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final WantedMapper wantedMapper;
    private final UserMapper userMapper;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ChatSessionVO createChatSession(CreateChatSessionDTO dto, Long currentUserId) {
        if (dto == null || (dto.getItemId() == null && dto.getWantedId() == null)) {
            throw new RuntimeException("商品ID或求购ID不能为空");
        }

        boolean isWanted = dto.getWantedId() != null;
        Long refId = isWanted ? dto.getWantedId() : dto.getItemId();
        Long sellerId;
        String refTitle;

        if (isWanted) {
            Wanted wanted = wantedMapper.selectById(dto.getWantedId());
            if (wanted == null || (wanted.getIsDeleted() != null && wanted.getIsDeleted() == 1)) {
                throw new RuntimeException("求购信息不存在");
            }
            sellerId = wanted.getUserId();
            refTitle = wanted.getTitle();
        } else {
            Item item = itemMapper.selectById(dto.getItemId());
            if (item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)) {
                throw new RuntimeException("商品不存在");
            }
            sellerId = item.getUserId();
            refTitle = item.getTitle();
        }

        if (sellerId.equals(currentUserId)) {
            throw new RuntimeException("不能与自己进行对话");
        }

        LambdaQueryWrapper<ChatSession> existWrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getBuyerId, currentUserId)
                .eq(ChatSession::getSellerId, sellerId)
                .eq(ChatSession::getIsDeleted, 0);
        if (isWanted) {
            existWrapper.eq(ChatSession::getWantedId, refId);
        } else {
            existWrapper.eq(ChatSession::getItemId, refId);
        }

        ChatSession exist = chatSessionMapper.selectOne(existWrapper);
        if (exist != null) {
            return toSessionVO(exist, currentUserId, refTitle, isWanted, refId);
        }

        ChatSession session = new ChatSession();
        if (isWanted) {
            session.setWantedId(refId);
        } else {
            session.setItemId(refId);
        }
        session.setBuyerId(currentUserId);
        session.setSellerId(sellerId);
        session.setBuyerUnreadCount(0);
        session.setSellerUnreadCount(0);
        session.setIsDeleted(0);

        chatSessionMapper.insert(session);

        return toSessionVO(session, currentUserId, refTitle, isWanted, refId);
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

        for (ChatSession session : sessions) {
            String refTitle = "";
            boolean isWanted = session.getWantedId() != null;
            Long refId = isWanted ? session.getWantedId() : session.getItemId();
            if (isWanted) {
                Wanted wanted = wantedMapper.selectById(session.getWantedId());
                refTitle = wanted != null ? wanted.getTitle() : "";
            } else if (session.getItemId() != null) {
                Item item = itemMapper.selectById(session.getItemId());
                refTitle = item != null ? item.getTitle() : "";
            }
            result.add(toSessionVO(session, currentUserId, refTitle, isWanted, refId));
        }
        return result;
    }

    @Transactional
    public ChatMessageVO sendMessage(SendMessageDTO dto, Long currentUserId) {
        if(dto == null || dto.getSessionId() == null){
            throw new RuntimeException("会话ID不能为空");
        }

        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        if(content.isEmpty()){
            throw new RuntimeException("消息内容不能为空");
        }

        String messageType = dto.getMessageType();
        if(messageType == null || messageType.trim().isEmpty()) {
            messageType = MessageTypeEnum.TEXT.name();
        }
        else {
            messageType = messageType.trim().toUpperCase();
        }

        if(!MessageTypeEnum.TEXT.name().equals(messageType)
                && !MessageTypeEnum.IMAGE.name().equals(messageType)){
            throw new RuntimeException("不支持的消息类型");
        }

        if(MessageTypeEnum.IMAGE.name().equals(messageType)
                && !content.startsWith("/uploads/chat/")){
            throw new RuntimeException("图片地址不合法");
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
        message.setMessageType(messageType);
        message.setContent(content);
        message.setReadFlag(0);
        message.setIsDeleted(0);

        chatMessageMapper.insert(message);

        if(MessageTypeEnum.IMAGE.name().equals(messageType)) {
            session.setLastMessage("[图片]");
        }
        else {
            session.setLastMessage(message.getContent());
        }

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

    private ChatSessionVO toSessionVO(ChatSession session, Long currentUserId, String refTitle, boolean isWanted, Long refId) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        if (isWanted) {
            vo.setWantedId(refId);
            vo.setWantedTitle(refTitle);
        } else {
            vo.setItemId(refId);
            vo.setItemTitle(refTitle);
        }
        vo.setBuyerId(session.getBuyerId());
        vo.setSellerId(session.getSellerId());
        vo.setLastMessage(session.getLastMessage());
        vo.setLastSenderId(session.getLastSenderId());
        vo.setLastMessageTime(session.getLastMessageTime());

        if (session.getBuyerId().equals(currentUserId)) {
            vo.setUnreadCount(session.getBuyerUnreadCount());
        } else {
            vo.setUnreadCount(session.getSellerUnreadCount());
        }

        Long partnerId = session.getBuyerId().equals(currentUserId)
                ? session.getSellerId() : session.getBuyerId();
        User partner = userMapper.selectById(partnerId);
        if (partner != null) {
            vo.setPartnerName(partner.getUsername());
            vo.setPartnerAvatar(resolveUrl(partner.getAvatar()));
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

    private String resolveUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        return baseUrl + url;
    }
}
