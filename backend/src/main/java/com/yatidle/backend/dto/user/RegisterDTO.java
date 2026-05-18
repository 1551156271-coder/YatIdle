package com.yatidle.backend.dto.user;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String nickname;
    private String bio;
    private String campus;
}
