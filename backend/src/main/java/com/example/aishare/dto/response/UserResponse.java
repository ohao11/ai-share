package com.example.aishare.dto.response;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * 用户信息响应
 */
@Data
public class UserResponse {

    private Long id;
    private String uuid;
    private String username;
    private String email;
    private String avatar;
    private Integer role;
    private String provider;
    private ZonedDateTime createdAt;
}
