package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.dto.chat.CreateChatSessionDTO;
import com.yatidle.backend.dto.chat.SendMessageDTO;
import com.yatidle.backend.service.ChatService;
import com.yatidle.backend.vo.chat.ChatMessageVO;
import com.yatidle.backend.vo.chat.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    @PostMapping("/sessions")
    public Result<ChatSessionVO> createSession(
            @RequestBody CreateChatSessionDTO dto,
            @RequestParam Long userId){

        ChatSessionVO vo = chatService.createChatSession(dto, userId);
        return Result.success(vo);
    }

    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> listMySessions(@RequestParam Long userId){
        List<ChatSessionVO> list = chatService.listMySessions(userId);
        return Result.success(list);
    }

    @PostMapping("/messages")
    public Result<ChatMessageVO> sendMessage(
            @RequestBody SendMessageDTO dto,
            @RequestParam Long userId){

        ChatMessageVO vo = chatService.sendMessage(dto, userId);
        return Result.success(vo);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatMessageVO>> listMessages(
            @PathVariable Long sessionId,
            @RequestParam Long userId){

        List<ChatMessageVO> list = chatService.listMessages(sessionId, userId);
        return Result.success(list);
    }

    @PutMapping("/sessions/{sessionId}/read")
    public Result<Void> markAsRead(
            @PathVariable Long sessionId,
            @RequestParam Long userId) {

        chatService.markAsRead(sessionId, userId);
        return Result.success();
    }

    @PostMapping("/images/upload")
    public Result<Map<String, String>> uploadChatImage(@RequestParam("file") MultipartFile file) throws IOException {
        if(file == null || file.isEmpty()){
            throw new BusinessException("图片文件不能为空");
        }

        String contentType = file.getContentType();
        if(contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );

        String ext = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if(dotIndex >= 0){
            ext = originalFilename.substring(dotIndex);
        }

        String fileName = UUID.randomUUID() + ext;
        Path uploadDir = Paths.get("uploads", "chat").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        Path target = uploadDir.resolve(fileName);
        file.transferTo(target);

        return Result.success(Map.of("url", "/uploads/chat/" + fileName));
    }
}
