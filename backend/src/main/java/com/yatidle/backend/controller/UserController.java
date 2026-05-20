package com.yatidle.backend.controller;

import com.yatidle.backend.common.JwtUtils;
import com.yatidle.backend.common.Result;
import com.yatidle.backend.dto.user.LoginDTO;
import com.yatidle.backend.dto.user.RegisterDTO;
import com.yatidle.backend.dto.user.UpdateProfileDTO;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.service.UserService;
import com.yatidle.backend.vo.user.LoginVO;
import com.yatidle.backend.vo.user.UserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody RegisterDTO dto) {
        User user = userService.register(dto.getUsername(), dto.getPassword(),
                dto.getNickname(), dto.getBio(), dto.getCampus());
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtils.generateToken(user.getId()));
        vo.setUser(UserVO.from(user));
        return Result.success(vo);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        User user = userService.login(dto.getUsername(), dto.getPassword());
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtils.generateToken(user.getId()));
        vo.setUser(UserVO.from(user));
        return Result.success(vo);
    }

    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return Result.success(UserVO.from(user));
    }

    @GetMapping("/list")
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
        userService.updateProfile(dto.getUserId(), dto.getPassword(), dto.getPhone(), dto.getAvatar(),
                dto.getNickname(), dto.getBio(), dto.getCampus());
        return Result.success(null);
    }
}
