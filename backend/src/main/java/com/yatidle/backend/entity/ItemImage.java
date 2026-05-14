package com.yatidle.backend.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("item_image")
public class ItemImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private Integer isDeleted;

}
