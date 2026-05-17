package com.yatidle.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemPublishDTO {

    @NotNull(message = "发布用户ID不能为空")
    private Long userId;

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String campus;

    private String conditionLevel;

    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private List<String> imageUrls;   // 图片URL列表
}
