package com.yatidle.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("item")
public class Item {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String campus;
    private String conditionLevel;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String status;
    private Integer viewCount;
    private Integer favoriteCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
