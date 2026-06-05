package com.yatidle.backend.config;

import com.yatidle.backend.entity.User;
import com.yatidle.backend.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String ADMIN_USER_ATTRIBUTE = "adminUser";

    private final AdminAuthService adminAuthService;

    public AdminAuthInterceptor(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        User admin = adminAuthService.requireAdmin(request.getHeader("Authorization"));
        request.setAttribute(ADMIN_USER_ATTRIBUTE, admin);
        return true;
    }
}
