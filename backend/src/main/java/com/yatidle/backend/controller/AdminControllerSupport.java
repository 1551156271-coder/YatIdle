package com.yatidle.backend.controller;

import com.yatidle.backend.config.AdminAuthInterceptor;
import com.yatidle.backend.entity.User;
import jakarta.servlet.http.HttpServletRequest;

final class AdminControllerSupport {
    private AdminControllerSupport() {
    }

    static Long currentAdminId(HttpServletRequest request) {
        User admin = (User) request.getAttribute(AdminAuthInterceptor.ADMIN_USER_ATTRIBUTE);
        return admin == null ? null : admin.getId();
    }
}
