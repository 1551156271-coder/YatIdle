package com.yatidle.backend.dto.review;

import lombok.Data;

@Data
public class CreateReviewDTO {

    private Long orderId;

    private Long revieweeId;

    private Integer rating;

    private String content;
}
