package com.yatidle.backend.controller;

import com.yatidle.backend.entity.User;
import com.yatidle.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        return userService.register(username, password);
    }

    @PostMapping("/login")
    public User login(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        return userService.login(username, password);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    //返回所有用户列表，被删除的不包含在内
    @GetMapping("/show_me_all!")
    public List<User> findAll() {
        return userService.findAll();
    }

    @DeleteMapping("/{id}")
    public int deleteById(@PathVariable Long id) {
        return userService.deleteById(id);
    }

    @PutMapping("/profile")
    public int updateProfile(@RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("userId")).longValue();
        String password = (String) body.get("password");
        String phone = (String) body.get("phone");
        String avatar = (String) body.get("avatar");
        return userService.updateProfile(userId, password, phone, avatar);
    }
}
