package com.example.aishare.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具
 * 用于生成 BCrypt 加密的密码
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 要加密的密码
        String[] passwords = {"admin123", "test123456", "123456"};

        System.out.println("========================================");
        System.out.println("BCrypt 密码生成工具");
        System.out.println("========================================\n");

        for (String password : passwords) {
            String encoded = encoder.encode(password);
            System.out.println("原始密码: " + password);
            System.out.println("加密后:   " + encoded);
            System.out.println();
        }

        System.out.println("========================================");
        System.out.println("SQL 更新语句示例:");
        System.out.println("========================================\n");

        String adminPassword = encoder.encode("admin123");
        System.out.println("-- 设置 admin 用户密码为 admin123");
        System.out.println("UPDATE users SET password = '" + adminPassword + "' WHERE username = 'admin';");
        System.out.println();
        System.out.println("-- 设置 testuser 用户密码为 test123456");
        String testPassword = encoder.encode("test123456");
        System.out.println("UPDATE users SET password = '" + testPassword + "' WHERE username = 'testuser';");
    }
}