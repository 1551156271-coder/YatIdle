package com.yatidle.backend.vo.admin;

import com.yatidle.backend.entity.Review;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReviewVO {
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private String reviewerUsername;
    private Long revieweeId;
    private String revieweeUsername;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static AdminReviewVO from(Review review) {
        AdminReviewVO vo = new AdminReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setReviewerId(review.getReviewerId());
        vo.setRevieweeId(review.getRevieweeId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreateTime(review.getCreateTime());
        vo.setUpdateTime(review.getUpdateTime());
        return vo;
    }
}
