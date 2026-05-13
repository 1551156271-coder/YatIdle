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
    private long id;
    private long userId;
    private String title;
    private String campus;
    private String conditionLevel;
    private String description;
    private BigDecimal price;
    private long categoryId;
    private String status;
    private long viewCount;
    private long favoriteCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
