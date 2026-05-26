package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yatidle.backend.dto.review.CreateReviewDTO;
import com.yatidle.backend.entity.Review;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.mapper.ReviewMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.vo.review.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {

    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;

    @Transactional
    public ReviewVO create(Long userId, CreateReviewDTO dto) {
        if (dto == null || dto.getOrderId() == null || dto.getRevieweeId() == null) {
            throw new RuntimeException("订单ID和被评价人ID不能为空");
        }
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }
        if (dto.getRevieweeId().equals(userId)) {
            throw new RuntimeException("不能评价自己");
        }

        boolean exists = reviewMapper.hasReviewed(dto.getOrderId(), userId);
        if (exists) {
            throw new RuntimeException("该订单已评价");
        }

        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setReviewerId(userId);
        review.setRevieweeId(dto.getRevieweeId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setIsDeleted(0);

        reviewMapper.insert(review);

        updateUserCreditScore(dto.getRevieweeId());

        return toVO(review);
    }

    @Transactional
    public ReviewVO update(Long reviewId, Long userId, CreateReviewDTO dto) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null || review.getIsDeleted() != null && review.getIsDeleted() == 1) {
            throw new RuntimeException("评价不存在");
        }
        if (!review.getReviewerId().equals(userId)) {
            throw new RuntimeException("无权修改他人评价");
        }
        if (dto.getRating() != null) {
            if (dto.getRating() < 1 || dto.getRating() > 5) {
                throw new RuntimeException("评分必须在1-5之间");
            }
            review.setRating(dto.getRating());
        }
        if (dto.getContent() != null) {
            review.setContent(dto.getContent());
        }

        reviewMapper.updateById(review);

        updateUserCreditScore(review.getRevieweeId());

        return toVO(review);
    }

    @Transactional
    public void delete(Long reviewId, Long userId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null || review.getIsDeleted() != null && review.getIsDeleted() == 1) {
            throw new RuntimeException("评价不存在");
        }
        if (!review.getReviewerId().equals(userId)) {
            throw new RuntimeException("无权删除他人评价");
        }

        review.setIsDeleted(1);
        reviewMapper.updateById(review);

        updateUserCreditScore(review.getRevieweeId());
    }

    public List<ReviewVO> getUserReviews(Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRevieweeId, userId)
                .eq(Review::getIsDeleted, 0)
                .orderByDesc(Review::getCreateTime);
        List<Review> list = reviewMapper.selectList(wrapper);
        List<ReviewVO> result = new ArrayList<>();
        for (Review r : list) {
            result.add(toVO(r));
        }
        return result;
    }

    public List<ReviewVO> getMyReviews(Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getReviewerId, userId)
                .eq(Review::getIsDeleted, 0)
                .orderByDesc(Review::getCreateTime);
        List<Review> list = reviewMapper.selectList(wrapper);
        List<ReviewVO> result = new ArrayList<>();
        for (Review r : list) {
            result.add(toVO(r));
        }
        return result;
    }

    public boolean checkReviewed(Long orderId, Long userId) {
        return reviewMapper.hasReviewed(orderId, userId);
    }

    public double getAverageRating(Long revieweeId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRevieweeId, revieweeId)
                .eq(Review::getIsDeleted, 0);
        List<Review> list = reviewMapper.selectList(wrapper);
        if (list.isEmpty()) return 0.0;
        double sum = 0;
        for (Review r : list) {
            sum += r.getRating();
        }
        return sum / list.size();
    }

    public int getReviewCount(Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRevieweeId, userId)
                .eq(Review::getIsDeleted, 0);
        return reviewMapper.selectCount(wrapper).intValue();
    }

    private void updateUserCreditScore(Long userId) {
        double avg = getAverageRating(userId);
        int score = (int) (avg * 20);
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setCreditScore(score);
            userMapper.updateById(user);
        }
    }

    private ReviewVO toVO(Review review) {
        if (review == null) return null;
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setReviewerId(review.getReviewerId());
        vo.setRevieweeId(review.getRevieweeId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreateTime(review.getCreateTime());
        vo.setUpdateTime(review.getUpdateTime());

        User reviewer = userMapper.selectById(review.getReviewerId());
        if (reviewer != null) {
            vo.setReviewerName(reviewer.getNickname() != null ? reviewer.getNickname() : reviewer.getUsername());
            vo.setReviewerAvatar(reviewer.getAvatar());
        }
        return vo;
    }
}
