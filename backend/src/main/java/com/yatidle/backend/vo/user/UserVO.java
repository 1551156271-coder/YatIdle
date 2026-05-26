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
        return from(user, "http://127.0.0.1:8080");
    }

    public static UserVO from(User user, String baseUrl) {
        if (user == null) return null;
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setAvatar(resolveUrl(user.getAvatar(), baseUrl));
        vo.setNickname(user.getNickname());
        vo.setBio(user.getBio());
        vo.setCampus(user.getCampus());
        vo.setCreditScore(user.getCreditScore());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    private static String resolveUrl(String url) {
        return resolveUrl(url, "http://127.0.0.1:8080");
    }

    private static String resolveUrl(String url, String baseUrl) {
        if (url == null || url.isEmpty()) return url;
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        return baseUrl + url;
    }
}
