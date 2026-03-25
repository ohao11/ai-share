package com.example.aishare.common.constants;

/**
 * 系统常量
 */
public final class SystemConstants {

    private SystemConstants() {
    }

    /**
     * 用户角色
     */
    public static final class Role {
        public static final int USER = 1;
        public static final int AUTHOR = 2;
        public static final int ADMIN = 3;
    }

    /**
     * 用户状态
     */
    public static final class Status {
        public static final int DISABLED = 0;
        public static final int NORMAL = 1;
    }

    /**
     * 文章状态
     */
    public static final class ArticleStatus {
        public static final int DRAFT = 0;
        public static final int PUBLISHED = 1;
        public static final int REMOVED = 2;
    }

    /**
     * 评论状态
     */
    public static final class CommentStatus {
        public static final int PENDING = 0;
        public static final int APPROVED = 1;
        public static final int REJECTED = 2;
    }

    /**
     * 提供者类型
     */
    public static final class Provider {
        public static final String LOCAL = "local";
        public static final String GOOGLE = "google";
        public static final String GITHUB = "github";
    }

    /**
     * MinIO Bucket 名称
     */
    public static final String MINIO_BUCKET = "ai-share";

    /**
     * JWT Token 前缀
     */
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
}
