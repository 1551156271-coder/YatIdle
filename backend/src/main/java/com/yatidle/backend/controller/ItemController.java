package com.yatidle.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.ItemPublishDTO;
import com.yatidle.backend.dto.ItemSearchDTO;
import com.yatidle.backend.service.ItemService;
import com.yatidle.backend.vo.ItemCardVO;
import com.yatidle.backend.vo.ItemDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

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

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ItemPublishDTO dto) {
        itemService.update(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        itemService.offline(id);
        return Result.success();
    }
}
