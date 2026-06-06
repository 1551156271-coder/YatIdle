package com.yatidle.backend.dto.admin;

import lombok.Data;

@Data
public class AdminCategoryDTO {
    private String name;
    private Integer sortOrder;
    private Integer status;
}
