package com.yatidle.backend.vo.wanted;

import com.yatidle.backend.entity.Wanted;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WantedVO {
    private Long id;
    private Long userId;
    private String title;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String campus;
    private String conditionLevel;
    private Long categoryId;
    private String status;
    private Integer viewCount;
    private LocalDateTime createTime;

    public static WantedVO from(Wanted wanted) {
        if (wanted == null) return null;
        WantedVO vo = new WantedVO();
        vo.setId(wanted.getId());
        vo.setUserId(wanted.getUserId());
        vo.setTitle(wanted.getTitle());
        vo.setBudgetMin(wanted.getBudgetMin());
        vo.setBudgetMax(wanted.getBudgetMax());
        vo.setCampus(wanted.getCampus());
        vo.setConditionLevel(wanted.getConditionLevel());
        vo.setCategoryId(wanted.getCategoryId());
        vo.setStatus(wanted.getStatus());
        vo.setViewCount(wanted.getViewCount());
        vo.setCreateTime(wanted.getCreateTime());
        return vo;
    }
}
