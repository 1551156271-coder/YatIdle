package com.yatidle.backend.service;

import com.yatidle.backend.common.JwtUtils;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.vo.user.LoginVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @Test
    void loginRejectsNormalUser() {
        AdminAuthService service = new AdminAuthService(userService, jwtUtils, "http://127.0.0.1:8080");
        User user = activeUser(7L, 0);
        when(userService.login("student", "123456")).thenReturn(user);

        assertThatThrownBy(() -> service.login("student", "123456"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无管理员权限");
    }

    @Test
    void loginRejectsInactiveAdmin() {
        AdminAuthService service = new AdminAuthService(userService, jwtUtils, "http://127.0.0.1:8080");
        User user = activeUser(8L, 1);
        user.setStatus("inactive");
        when(userService.login("admin", "123456")).thenReturn(user);

        assertThatThrownBy(() -> service.login("admin", "123456"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("账号已被禁用");
    }

    @Test
    void loginReturnsTokenForActiveAdmin() {
        AdminAuthService service = new AdminAuthService(userService, jwtUtils, "http://127.0.0.1:8080");
        User user = activeUser(1L, 1);
        when(userService.login("admin", "123456")).thenReturn(user);
        when(jwtUtils.generateToken(1L)).thenReturn("jwt-token");

        LoginVO result = service.login("admin", "123456");

        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUser().getId()).isEqualTo(1L);
        assertThat(result.getUser().getRole()).isEqualTo(1);
    }

    @Test
    void requireAdminRejectsMissingBearerToken() {
        AdminAuthService service = new AdminAuthService(userService, jwtUtils, "http://127.0.0.1:8080");

        assertThatThrownBy(() -> service.requireAdmin(null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("未登录");
    }

    @Test
    void requireAdminRejectsNormalUserToken() {
        AdminAuthService service = new AdminAuthService(userService, jwtUtils, "http://127.0.0.1:8080");
        when(jwtUtils.getUserIdFromToken("normal-token")).thenReturn(2L);
        when(userService.findById(2L)).thenReturn(activeUser(2L, 0));

        assertThatThrownBy(() -> service.requireAdmin("Bearer normal-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无管理员权限");
    }

    private static User activeUser(Long id, Integer role) {
        User user = new User();
        user.setId(id);
        user.setUsername(role == 1 ? "admin" : "student");
        user.setPassword("123456");
        user.setRole(role);
        user.setStatus("active");
        return user;
    }
}
