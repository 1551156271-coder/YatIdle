package com.yatidle.backend.dto.wanted;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^(南校园|东校园|北校园|珠海校区|深圳校区)$", message = "校区参数不正确")
    private String campus;
    @Pattern(regexp = "^(全新|99新及以上|95新及以上|90新及以上|85新及以上|80新及以上)$", message = "期望成色参数不正确")
    private String conditionLevel;
    private String description;
    private Long categoryId;
    private List<String> imageUrls;
}
