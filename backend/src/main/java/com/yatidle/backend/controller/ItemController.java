package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.item.ItemPublishDTO;
import com.yatidle.backend.dto.item.ItemSearchDTO;
import com.yatidle.backend.service.ItemService;
import com.yatidle.backend.util.ImageUploadValidator;
import com.yatidle.backend.vo.item.ItemCardVO;
import com.yatidle.backend.vo.item.ItemDetailVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final String baseUrl;

    public ItemController(ItemService itemService,
                          @Value("${app.base-url}") String baseUrl) {
        this.itemService = itemService;
        this.baseUrl = baseUrl;
    }

    @PostMapping("/publish")
    public Result<ItemDetailVO> publish(@Valid @RequestBody ItemPublishDTO dto) {
        ItemDetailVO result = itemService.publish(dto);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ItemDetailVO> getDetail(@PathVariable Long id) {
        ItemDetailVO result = itemService.getDetail(id);
        return Result.success(result);
    }

    @GetMapping("/search")
    public Result<Page<ItemCardVO>> search(ItemSearchDTO dto,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        Page<ItemCardVO> result = itemService.search(dto, page, size);
        return Result.success(result);
    }

    @GetMapping("/user/{userId}")
    public Result<Page<ItemCardVO>> listByUser(@PathVariable Long userId,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        Page<ItemCardVO> result = itemService.listByUser(userId, status, page, size);
        return Result.success(result);
    }

    @PostMapping("/images/upload")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String ext = ImageUploadValidator.validate(file);
        String filename = UUID.randomUUID() + ext;
        Path uploadDir = Paths.get("uploads", "items").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(filename);
        file.transferTo(target);

        return Result.success(Map.of("url", "/uploads/items/" + filename));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ItemPublishDTO dto) {
        itemService.update(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id, @RequestParam Long userId) {
        itemService.offline(id, userId);
        return Result.success();
    }

    @PutMapping("/{id}/online")
    public Result<Void> online(@PathVariable Long id, @RequestParam Long userId) {
        itemService.online(id, userId);
        return Result.success();
    }
}
