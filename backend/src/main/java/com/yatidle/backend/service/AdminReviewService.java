package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.Review;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.mapper.ReviewMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.vo.admin.AdminReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminReviewService {
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final AdminLogService adminLogService;

    public AdminReviewService(ReviewMapper reviewMapper, AdminLogService adminLogService) {
        this(reviewMapper, null, adminLogService);
    }

    @Autowired
    public AdminReviewService(ReviewMapper reviewMapper, UserMapper userMapper, AdminLogService adminLogService) {
        this.reviewMapper = reviewMapper;
        this.userMapper = userMapper;
        this.adminLogService = adminLogService;
    }

    public Page<AdminReviewVO> list(Long reviewerId, Long revieweeId, Integer rating, int page, int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getIsDeleted, 0);
        if (reviewerId != null) wrapper.eq(Review::getReviewerId, reviewerId);
        if (revieweeId != null) wrapper.eq(Review::getRevieweeId, revieweeId);
        if (rating != null) wrapper.eq(Review::getRating, rating);
        wrapper.orderByDesc(Review::getCreateTime);
        return toVOPage(reviewMapper.selectPage(new Page<>(page, size), wrapper));
    }

    public AdminReviewVO detail(Long id) {
        Review review = findReview(id);
        return enrich(AdminReviewVO.from(review), mapUsersFromReviews(List.of(review)));
    }

    public void delete(Long adminId, Long id, String reason) {
        requireReason(reason);
        Review review = findReview(id);
        review.setIsDeleted(1);
        reviewMapper.updateById(review);
        adminLogService.log(adminId, "DELETE_REVIEW", "REVIEW", id, "ACTIVE", "DELETED", reason);
    }

    private Review findReview(Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null || (review.getIsDeleted() != null && review.getIsDeleted() == 1)) {
            throw new BusinessException("Review does not exist");
        }
        return review;
    }

    private Page<AdminReviewVO> toVOPage(Page<Review> source) {
        Page<AdminReviewVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setPages(source.getPages());
        Map<Long, User> users = mapUsersFromReviews(source.getRecords());
        result.setRecords(source.getRecords().stream()
                .map(AdminReviewVO::from)
                .map(vo -> enrich(vo, users))
                .toList());
        return result;
    }

    private AdminReviewVO enrich(AdminReviewVO vo, Map<Long, User> users) {
        User reviewer = find(users, vo.getReviewerId());
        if (reviewer != null) vo.setReviewerUsername(displayUser(reviewer));
        User reviewee = find(users, vo.getRevieweeId());
        if (reviewee != null) vo.setRevieweeUsername(displayUser(reviewee));
        return vo;
    }

    private Map<Long, User> mapUsersFromReviews(List<Review> reviews) {
        if (userMapper == null || reviews == null || reviews.isEmpty()) return Map.of();
        Set<Long> userIds = new LinkedHashSet<>();
        for (Review review : reviews) {
            if (review.getReviewerId() != null) userIds.add(review.getReviewerId());
            if (review.getRevieweeId() != null) userIds.add(review.getRevieweeId());
        }
        if (userIds.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(List.copyOf(userIds)).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private <T> T find(Map<Long, T> values, Long id) {
        return id == null ? null : values.get(id);
    }

    private String displayUser(User user) {
        return Objects.requireNonNullElse(user.getUsername(), Objects.requireNonNullElse(user.getNickname(), "user" + user.getId()));
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) throw new BusinessException("Reason is required");
    }
}
