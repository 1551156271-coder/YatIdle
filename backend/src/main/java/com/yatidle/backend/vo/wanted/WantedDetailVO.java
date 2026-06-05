package com.yatidle.backend.vo.wanted;

import com.yatidle.backend.entity.Wanted;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WantedDetailVO {
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
    private List<String> images;
    private String username;
    private String nickname;
    private String avatar;

    public static WantedDetailVO from(Wanted wanted, List<String> images) {
        if (wanted == null) return null;
        WantedDetailVO vo = new WantedDetailVO();
        vo.setId(wanted.getId());
        vo.setUserId(wanted.getUserId());
        vo.setTitle(wanted.getTitle());
        vo.setBudgetMin(wanted.getBudgetMin());
        vo.setBudgetMax(wanted.getBudgetMax());
        vo.setCampus(wanted.getCampus());
        vo.setConditionLevel(wanted.getConditionLevel());
        vo.setDescription(wanted.getDescription());
        vo.setCategoryId(wanted.getCategoryId());
        vo.setStatus(wanted.getStatus());
        vo.setViewCount(wanted.getViewCount());
        vo.setCreateTime(wanted.getCreateTime());
        vo.setImages(images);
        return vo;
    }
}
