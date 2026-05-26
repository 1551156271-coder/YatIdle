package com.yatidle.backend.vo.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewVO {

    private Long id;

    private Long orderId;

    private Long reviewerId;

    private Long revieweeId;

    private String reviewerName;

    private String reviewerAvatar;

    private Integer rating;

    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
