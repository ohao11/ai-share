package com.example.aishare.controller;

import com.example.aishare.common.result.Result;
import com.example.aishare.dto.request.LoginRequest;
import com.example.aishare.dto.request.RegisterRequest;
import com.example.aishare.dto.response.LoginResponse;
import com.example.aishare.dto.response.UserResponse;
import com.example.aishare.security.RsaUtil;
import com.example.aishare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RsaUtil rsaUtil;

    /**
     * 获取 RSA 公钥
     */
    @GetMapping("/public-key")
    public Result<Map<String, String>> getPublicKey() {
        return Result.success(Map.of("publicKey", rsaUtil.getPublicKeyBase64()));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 解密密码
        String decryptedPassword = rsaUtil.decrypt(request.getPassword());
        request.setPassword(decryptedPassword);
        return Result.success(userService.login(request));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 解密密码
        String decryptedPassword = rsaUtil.decrypt(request.getPassword());
        request.setPassword(decryptedPassword);
        return Result.success(userService.register(request));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    /**
     * Google OAuth2 回调
     */
    @GetMapping("/google/callback")
    public void googleCallback() {
        // OAuth2 成功登录后由 Spring Security 处理
    }

    /**
     * GitHub OAuth2 回调
     */
    @GetMapping("/github/callback")
    public void githubCallback() {
        // OAuth2 成功登录后由 Spring Security 处理
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 前端清除 token 即可，这里只返回成功
        return Result.success();
    }
}
