package com.yatidle.backend.controller;

import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.user.LoginDTO;
import com.yatidle.backend.dto.user.RegisterDTO;
import com.yatidle.backend.dto.user.UpdateProfileDTO;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.service.UserService;
import com.yatidle.backend.vo.user.UserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterDTO dto) {
        User user = userService.register(dto.getUsername(), dto.getPassword());
        return Result.success(UserVO.from(user));
    }

    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginDTO dto) {
        User user = userService.login(dto.getUsername(), dto.getPassword());
        return Result.success(UserVO.from(user));
    }

    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return Result.success(UserVO.from(user));
    }

    @GetMapping("/show_me_all!")
    public Result<List<UserVO>> findAll() {
        List<User> users = userService.findAll();
        List<UserVO> voList = users.stream().map(UserVO::from).collect(Collectors.toList());
        return Result.success(voList);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.success(null);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UpdateProfileDTO dto) {
        userService.updateProfile(dto.getUserId(), dto.getPassword(), dto.getPhone(), dto.getAvatar());
        return Result.success(null);
    }
}
