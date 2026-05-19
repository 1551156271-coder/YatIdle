package com.yatidle.backend.vo.user;

import com.yatidle.backend.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String phone;
    private String avatar;
    private String nickname;
    private String bio;
    private String campus;
    private Integer creditScore;
    private Integer role;
    private String status;
    private LocalDateTime createTime;

    // 统计字段，当前默认 0，后续接入实际统计
    private int dealCount;
    private int goodsCount;
    private int publishCount;
    private int reviewCount;
    private int soldCount;
    private int purchasedCount;
    private int wishCount;

    public static UserVO from(User user) {
        if (user == null) return null;
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setNickname(user.getNickname());
        vo.setBio(user.getBio());
        vo.setCampus(user.getCampus());
        vo.setCreditScore(user.getCreditScore());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
