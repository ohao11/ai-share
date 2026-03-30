package com.example.aishare.security;

import com.example.aishare.common.exception.BusinessException;
import com.example.aishare.dto.response.LoginResponse;
import com.example.aishare.dto.response.UserResponse;
import com.example.aishare.entity.User;
import com.example.aishare.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 登录成功处理器
 * 成功后生成 JWT token 并重定向到前端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        log.info("OAuth2 授权成功：provider={}", provider);

        // 获取用户信息
        String email = null;
        String name = null;
        String avatar = null;
        String providerId = null;

        switch (provider) {
            case "google" -> {
                email = oauthUser.getAttribute("email");
                name = oauthUser.getAttribute("name");
                avatar = oauthUser.getAttribute("picture");
                providerId = oauthUser.getAttribute("sub");
                log.info("Google 用户信息：email={}, name={}, providerId={}", email, name, providerId);
            }
            case "github" -> {
                email = oauthUser.getAttribute("email");
                name = oauthUser.getAttribute("name");
                avatar = oauthUser.getAttribute("avatar_url");
                Object idObj = oauthUser.getAttribute("id");
                providerId = String.valueOf(idObj);
                log.info("GitHub 用户信息：email={}, name={}, id={}({}), providerId={}",
                        email, name, idObj, idObj.getClass(), providerId);
            }
        }

        if (email == null) {
            log.error("OAuth 登录失败：无法获取邮箱地址，provider={}", provider);
            response.sendRedirect("/login?error=" + URLEncoder.encode("无法获取邮箱地址", StandardCharsets.UTF_8));
            return;
        }

        // OAuth 登录或注册并获取 JWT
        log.info("开始处理 OAuth 登录：provider={}, email={}", provider, email);
        LoginResponse loginResponse;
        try {
            loginResponse = userService.oauthLogin(provider, email, name, avatar, providerId);
        } catch (BusinessException e) {
            log.warn("OAuth 登录失败：{}", e.getMessage());
            response.sendRedirect("/login?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
            return;
        }

        // 构建前端重定向 URL，带上 token
        String targetUrl = String.format(
                "/oauth-callback?accessToken=%s&user=%s",
                loginResponse.getAccessToken(),
                URLEncoder.encode(objectMapper.writeValueAsString(loginResponse.getUser()), StandardCharsets.UTF_8)
        );

        log.info("OAuth 登录成功，重定向到：{}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
