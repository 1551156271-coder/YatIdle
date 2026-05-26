package com.yatidle.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yatidle.backend.entity.Review;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    default boolean hasReviewed(Long orderId, Long reviewerId) {
        if (orderId == null || reviewerId == null) return false;
        return exists(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId)
                .eq(Review::getReviewerId, reviewerId)
                .eq(Review::getIsDeleted, 0));
    }
}
