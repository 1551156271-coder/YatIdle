package com.yatidle.backend.dto.wanted;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateWantedDTO {
    private Long userId;
    @NotBlank(message = "求购标题不能为空")
    @Size(max = 50, message = "求购标题不能超过50个字")
    private String title;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String campus;
    private String conditionLevel;
    private String description;
    private Long categoryId;
    private List<String> imageUrls;
}
