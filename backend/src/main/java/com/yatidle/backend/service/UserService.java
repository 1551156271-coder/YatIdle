package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    //增加用户
    public int save(User user) {
        return userMapper.insert(user);
    }

    //查找（id方法，username方法）
    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    public User findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
    }

    //查看用户名是否已经被使用~~
    public boolean existsByUsername(String username) {
        return userMapper.exists(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
    }

    //更新用户信息
    public int updateById(User user) {
        return userMapper.updateById(user);
    }

    //根据id删除
    public int deleteById(Long id) {
        return userMapper.deleteById(id);
    }

    //以下是具体业务逻辑方法
    //用户注册
    public User register(String username, String password) {
        if (existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(0);
        userMapper.insert(user);
        return user;
    }

    //用户登录
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

    //用户修改个人信息
    public int updateProfile(Long userId,String password ,String phone, String avatar) {
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
        return userMapper.updateById(user);
    }

    //返回所有用户，测试用
    public List<User> findAll() {
        return userMapper.selectList(null);
    }



}
