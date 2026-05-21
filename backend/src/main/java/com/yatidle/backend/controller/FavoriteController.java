package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.service.FavoriteService;
import com.yatidle.backend.vo.favorite.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
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
    public Result<List<FavoriteVO>> listMyFavorites(@RequestParam Long userId) {
        List<FavoriteVO> list = favoriteService.listMyFavorites(userId);
        return Result.success(list);
    }
}
