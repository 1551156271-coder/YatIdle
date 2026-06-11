package com.yatidle.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.server.ServerTioConfig;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatConnectionManager {
    private final ObjectMapper objectMapper;

    private volatile ServerTioConfig serverTioConfig;

    private final ConcurrentHashMap<Long, Set<ChannelContext>> userChannels = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ChannelContext, Long> channelUsers = new ConcurrentHashMap<>();

    public void setServerTioConfig(ServerTioConfig serverTioConfig){
        this.serverTioConfig = serverTioConfig;
    }
    public void bind(Long userId, ChannelContext channelContext){
        if(userId == null || channelContext == null){
            return;
        }
        channelUsers.put(channelContext, userId);
        userChannels.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(channelContext);

        Tio.bindUser(channelContext, String.valueOf(userId));

        log.info("WebSocket user connected, userId={}, onlineConnections={}", userId, getConnectionCount(userId));
    }
}
