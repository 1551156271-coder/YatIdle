package com.yatidle.backend.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("item_image")
public class ItemImage {
    private Long id;
    private Long itemId;
    private String imageUrl;
    private Long sortOrder;
    private LocalDateTime createTime;
    private Integer isDeleted;

}
