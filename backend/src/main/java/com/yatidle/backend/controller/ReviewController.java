package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.review.CreateReviewDTO;
import com.yatidle.backend.service.ReviewService;
import com.yatidle.backend.vo.review.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<ReviewVO> create(@RequestParam Long userId, @RequestBody CreateReviewDTO dto) {
        ReviewVO vo = reviewService.create(userId, dto);
        return Result.success(vo);
    }

    @PutMapping("/{id}")
    public Result<ReviewVO> update(@PathVariable Long id, @RequestParam Long userId, @RequestBody CreateReviewDTO dto) {
        ReviewVO vo = reviewService.update(id, userId, dto);
        return Result.success(vo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        reviewService.delete(id, userId);
        return Result.success(null);
    }

    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserReviews(@PathVariable Long userId) {
        List<ReviewVO> reviews = reviewService.getUserReviews(userId);
        double avgRating = reviewService.getAverageRating(userId);
        int totalCount = reviewService.getReviewCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("reviews", reviews);
        data.put("avgRating", Math.round(avgRating * 10) / 10.0);
        data.put("totalCount", totalCount);
        return Result.success(data);
    }

    @GetMapping("/my")
    public Result<List<ReviewVO>> getMyReviews(@RequestParam Long userId) {
        List<ReviewVO> list = reviewService.getMyReviews(userId);
        return Result.success(list);
    }

    @GetMapping("/check")
    public Result<Boolean> checkReviewed(@RequestParam Long orderId, @RequestParam Long userId) {
        boolean reviewed = reviewService.checkReviewed(orderId, userId);
        return Result.success(reviewed);
    }
}
