package com.yatidle.backend.service;

import com.yatidle.backend.common.JwtUtils;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.vo.user.LoginVO;
import com.yatidle.backend.vo.user.UserVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final String baseUrl;

    public AdminAuthService(UserService userService,
                            JwtUtils jwtUtils,
                            @Value("${app.base-url}") String baseUrl) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.baseUrl = baseUrl;
    }

    public LoginVO login(String username, String password) {
        User user = userService.login(username, password);
        validateAdmin(user);
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtils.generateToken(user.getId()));
        vo.setUser(UserVO.from(user, baseUrl));
        return vo;
    }

    public User requireAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new BusinessException("未登录");
        }
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            User user = userService.findById(userId);
            validateAdmin(user);
            return user;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("登录已失效");
        }
    }

    private void validateAdmin(User user) {
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!"active".equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        if (user.getRole() == null || user.getRole() != 1) {
            throw new BusinessException("无管理员权限");
        }
    }
}
