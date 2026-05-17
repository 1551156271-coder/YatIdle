package com.yatidle.backend.dto.user;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private Long userId;
    private String password;
    private String phone;
    private String avatar;
    private String nickname;
    private String bio;
    private String campus;
}
