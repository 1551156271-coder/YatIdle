package com.yatidle.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wanted_image")
public class WantedImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long wantedId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
