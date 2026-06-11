package com.yatidle.backend.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatWsPayload {
    private String type;
    private Object data;
    private Long timestamp;

    public static ChatWsPayload of(String type, Object data) {
        return new ChatWsPayload(type, data, System.currentTimeMillis());
    }
}
