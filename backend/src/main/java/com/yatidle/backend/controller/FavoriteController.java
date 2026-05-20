package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.service.FavoriteService;
import com.yatidle.backend.vo.PageVO;
import com.yatidle.backend.vo.favorite.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{itemId}")
    public Result<Void> addFavorite(
            @PathVariable Long itemId,
            @RequestParam Long userId) {
        favoriteService.addFavorite(itemId, userId);
        return Result.success();
    }

    @DeleteMapping("/{itemId}")
    public Result<Void> cancelFavorite(
            @PathVariable Long itemId,
            @RequestParam Long userId) {
        favoriteService.cancelFavorite(itemId, userId);
        return Result.success();
    }

    @GetMapping
    public Result<PageVO<FavoriteVO>> listMyFavorites(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {

        PageVO<FavoriteVO> page = favoriteService.listMyFavorites(userId, pageNum, pageSize);
        return Result.success(page);
    }
}
