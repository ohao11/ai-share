package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aishare.common.constants.SystemConstants;
import com.example.aishare.common.exception.BusinessException;
import com.example.aishare.dto.request.LoginRequest;
import com.example.aishare.dto.request.RegisterRequest;
import com.example.aishare.dto.response.LoginResponse;
import com.example.aishare.dto.response.UserResponse;
import com.example.aishare.entity.User;
import com.example.aishare.mapper.UserMapper;
import com.example.aishare.security.JwtTokenProvider;
import com.example.aishare.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = lambdaQuery()
                .eq(User::getEmail, request.getEmail())
                .one();

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (user.getStatus() == SystemConstants.Status.DISABLED) {
            throw new BusinessException("账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginAt(OffsetDateTime.now());
        updateById(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        UserResponse userResponse = convertToUserResponse(user);

        return LoginResponse.of(token, userResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        // 检查邮箱是否已存在
        Long count = lambdaQuery()
                .eq(User::getEmail, request.getEmail())
                .count();

        if (count > 0) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(SystemConstants.Role.USER);
        user.setStatus(SystemConstants.Status.NORMAL);
        user.setProvider(SystemConstants.Provider.LOCAL);

        save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        UserResponse userResponse = convertToUserResponse(user);

        return LoginResponse.of(token, userResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse oauthLogin(String provider, String email, String name, String avatar, String providerId) {
        User user = lambdaQuery()
                .eq(User::getEmail, email)
                .one();

        if (user == null) {
            // 创建新用户
            user = new User();
            user.setEmail(email);
            user.setUsername(name != null ? name : email.split("@")[0]);
            user.setAvatar(avatar);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setRole(SystemConstants.Role.USER);
            user.setStatus(SystemConstants.Status.NORMAL);
            save(user);
            log.info("OAuth 新用户注册：{}, provider: {}", email, provider);
        } else {
            // 更新用户信息
            user.setAvatar(avatar);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setLastLoginAt(OffsetDateTime.now());
            updateById(user);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        UserResponse userResponse = convertToUserResponse(user);

        return LoginResponse.of(token, userResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserResponse(user);
    }

    @Override
    public UserResponse getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return convertToUserResponse(user);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUuid(user.getUuid() != null ? user.getUuid().toString() : null);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setProvider(user.getProvider());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
