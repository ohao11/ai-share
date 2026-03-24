package com.example.aishare.dto.response;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private UserResponse user;

    public static LoginResponse of(String token, UserResponse user) {
        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setUser(user);
        return response;
    }
}
