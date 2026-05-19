package com.yatidle.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wanted")
public class Wanted {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String campus;
    private String conditionLevel;
    private String description;
    private Long categoryId;
    private String status;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
