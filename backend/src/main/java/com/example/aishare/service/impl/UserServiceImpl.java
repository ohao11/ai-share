package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aishare.common.constants.SystemConstants;
import com.example.aishare.common.exception.BusinessException;
import com.example.aishare.dto.request.LoginRequest;
import com.example.aishare.dto.request.RegisterRequest;
import com.example.aishare.dto.response.LoginResponse;
import com.example.aishare.dto.response.UserResponse;
import com.example.aishare.entity.Article;
import com.example.aishare.entity.Comment;
import com.example.aishare.entity.User;
import com.example.aishare.mapper.ArticleMapper;
import com.example.aishare.mapper.CommentMapper;
import com.example.aishare.mapper.UserMapper;
import com.example.aishare.security.JwtTokenProvider;
import com.example.aishare.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = lambdaQuery()
                .eq(User::getEmail, request.getEmail())
                .one();

        // 统一错误信息，避免用户枚举攻击
        if (user == null) {
            throw new BusinessException("邮箱或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("邮箱或密码错误");
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
        Long emailCount = lambdaQuery()
                .eq(User::getEmail, request.getEmail())
                .count();

        if (emailCount > 0) {
            throw new BusinessException("邮箱已被注册");
        }

        // 检查用户名是否已存在
        Long usernameCount = lambdaQuery()
                .eq(User::getUsername, request.getUsername())
                .count();

        if (usernameCount > 0) {
            throw new BusinessException("用户名已被使用");
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
            // 检查用户状态
            if (user.getStatus() == SystemConstants.Status.DISABLED) {
                throw new BusinessException("账号已被禁用");
            }
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }

        Long userId = jwtTokenProvider.getUserIdFromAuthentication(authentication);
        User user = getById(userId);

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
        response.setStatus(user.getStatus());
        response.setProvider(user.getProvider());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    @Override
    public Page<UserResponse> getUsers(Integer page, Integer size, String keyword) {
        Page<User> userPage = new Page<>(page, size);

        Page<User> result = lambdaQuery()
                .like(keyword != null && !keyword.isBlank(), User::getUsername, keyword)
                .or()
                .like(keyword != null && !keyword.isBlank(), User::getEmail, keyword)
                .orderByDesc(User::getCreatedAt)
                .page(userPage);

        Page<UserResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(result.getTotal());
        responsePage.setRecords(result.getRecords().stream()
                .map(this::convertToUserResponse)
                .toList());

        return responsePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRole(Long id, Integer role) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setRole(role);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        removeById(id);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        Long totalUsers = lambdaQuery().count();
        Long activeUsers = lambdaQuery().eq(User::getStatus, SystemConstants.Status.NORMAL).count();

        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);

        // 文章统计
        LambdaQueryWrapper<Article> articleQuery = new LambdaQueryWrapper<>();
        Long totalArticles = articleMapper.selectCount(articleQuery);

        LambdaQueryWrapper<Article> publishedQuery = new LambdaQueryWrapper<>();
        publishedQuery.eq(Article::getStatus, SystemConstants.ArticleStatus.PUBLISHED);
        Long publishedArticles = articleMapper.selectCount(publishedQuery);

        LambdaQueryWrapper<Article> draftQuery = new LambdaQueryWrapper<>();
        draftQuery.eq(Article::getStatus, SystemConstants.ArticleStatus.DRAFT);
        Long draftArticles = articleMapper.selectCount(draftQuery);

        stats.put("totalArticles", totalArticles);
        stats.put("publishedArticles", publishedArticles);
        stats.put("draftArticles", draftArticles);

        // 评论统计
        LambdaQueryWrapper<Comment> commentQuery = new LambdaQueryWrapper<>();
        Long totalComments = commentMapper.selectCount(commentQuery);

        stats.put("totalComments", totalComments);

        // 浏览量和点赞量统计
        // 使用 SQL 聚合查询
        Long totalViews = articleMapper.selectSumViewCount();
        Long totalLikes = articleMapper.selectSumLikeCount();

        stats.put("totalViews", totalViews != null ? totalViews : 0);
        stats.put("totalLikes", totalLikes != null ? totalLikes : 0);

        return stats;
    }
}
