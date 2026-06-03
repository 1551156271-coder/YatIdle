package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public int save(User user) {
        return userMapper.insert(user);
    }

    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    public User findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
    }

    public boolean existsByUsername(String username) {
        return userMapper.exists(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
    }

    public int updateById(User user) {
        return userMapper.updateById(user);
    }

    public int deleteById(Long id) {
        return userMapper.deleteById(id);
    }

    // 用户注册
    public User register(String username, String password, String nickname, String bio, String campus) {
        if (existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setBio(bio);
        user.setCampus(campus);
        user.setRole(0);
        userMapper.insert(user);
        return user;
    }

    // 用户登录
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    // 修改个人信息
    public int updateProfile(Long userId, String password, String phone, String avatar,
                             String nickname, String bio, String campus) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        if (password != null) {
            user.setPassword(password);
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (campus != null) {
            user.setCampus(campus);
        }
        return userMapper.updateById(user);
    }

    // 返回所有用户
    public List<User> findAll() {
        return userMapper.selectList(null);
    }

    @Transactional
    public void deduct(Long userId, BigDecimal amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("钱包余额不足");
        }
        user.setBalance(user.getBalance().subtract(amount));
        userMapper.updateById(user);
    }

    @Transactional
    public void refund(Long userId, BigDecimal amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setBalance(user.getBalance().add(amount));
        userMapper.updateById(user);
    }
}
