package com.yatidle.backend.dto.admin;

import lombok.Data;

@Data
public class AdminRoleUpdateDTO {
    private Integer role;
    private String reason;
}
