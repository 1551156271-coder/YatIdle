package com.yatidle.backend.vo.admin;

import com.yatidle.backend.entity.Item;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminItemVO {
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
    private String imageUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static AdminItemVO from(Item item) {
        AdminItemVO vo = new AdminItemVO();
        vo.setId(item.getId());
        vo.setUserId(item.getUserId());
        vo.setTitle(item.getTitle());
        vo.setCampus(item.getCampus());
        vo.setConditionLevel(item.getConditionLevel());
        vo.setDescription(item.getDescription());
        vo.setPrice(item.getPrice());
        vo.setCategoryId(item.getCategoryId());
        vo.setStatus(item.getStatus());
        vo.setViewCount(item.getViewCount());
        vo.setFavoriteCount(item.getFavoriteCount());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }
}
