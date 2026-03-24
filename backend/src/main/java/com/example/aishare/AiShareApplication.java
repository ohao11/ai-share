package com.example.aishare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * AI Share 应用主入口
 */
@SpringBootApplication
@MapperScan("com.example.aishare.mapper")
@EnableMethodSecurity
public class AiShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiShareApplication.class, args);
    }
}
