package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.Review;
import com.yatidle.backend.mapper.ReviewMapper;
import org.springframework.stereotype.Service;

@Service
public class AdminReviewService {
    private final ReviewMapper reviewMapper;
    private final AdminLogService adminLogService;

    public AdminReviewService(ReviewMapper reviewMapper, AdminLogService adminLogService) {
        this.reviewMapper = reviewMapper;
        this.adminLogService = adminLogService;
    }

    public Page<Review> list(Long reviewerId, Long revieweeId, Integer rating, int page, int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getIsDeleted, 0);
        if (reviewerId != null) wrapper.eq(Review::getReviewerId, reviewerId);
        if (revieweeId != null) wrapper.eq(Review::getRevieweeId, revieweeId);
        if (rating != null) wrapper.eq(Review::getRating, rating);
        wrapper.orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public Review detail(Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null || (review.getIsDeleted() != null && review.getIsDeleted() == 1)) throw new BusinessException("评价不存在");
        return review;
    }

    public void delete(Long adminId, Long id, String reason) {
        requireReason(reason);
        Review review = detail(id);
        review.setIsDeleted(1);
        reviewMapper.updateById(review);
        adminLogService.log(adminId, "DELETE_REVIEW", "REVIEW", id, "ACTIVE", "DELETED", reason);
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) throw new BusinessException("操作原因不能为空");
    }
}
