package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.entity.ChatMessage;
import com.yatidle.backend.entity.ChatSession;
import com.yatidle.backend.service.AdminChatAuditService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin/chats")
public class AdminChatAuditController {
    private final AdminChatAuditService adminChatAuditService;

    public AdminChatAuditController(AdminChatAuditService adminChatAuditService) {
        this.adminChatAuditService = adminChatAuditService;
    }

    @GetMapping("/sessions")
    public Result<Page<ChatSession>> sessions(@RequestParam(required = false) Long userId,
                                              @RequestParam(required = false) Long itemId,
                                              @RequestParam(required = false) Long wantedId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminChatAuditService.sessions(userId, itemId, wantedId, page, size));
    }

    @GetMapping("/sessions/{id}/messages")
    public Result<List<ChatMessage>> messages(@PathVariable Long id) {
        return Result.success(adminChatAuditService.messages(id));
    }
}
