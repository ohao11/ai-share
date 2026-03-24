package com.example.aishare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aishare.dto.request.LoginRequest;
import com.example.aishare.dto.request.RegisterRequest;
import com.example.aishare.dto.response.LoginResponse;
import com.example.aishare.dto.response.UserResponse;
import com.example.aishare.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    LoginResponse register(RegisterRequest request);

    /**
     * OAuth2 登录
     */
    LoginResponse oauthLogin(String provider, String email, String name, String avatar, String providerId);

    /**
     * 获取用户信息
     */
    UserResponse getUserById(Long id);

    /**
     * 获取当前用户信息
     */
    UserResponse getCurrentUser();
}
