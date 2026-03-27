# ai-share 内容分享平台 - 完整设计方案

> 版本：1.0.0
> 最后更新：2026-03-24
> 状态：已实施

---

## 目录

1. [项目概述](#1-项目概述)
2. [技术架构](#2-技术架构)
3. [数据库设计](#3-数据库设计)
4. [后端设计](#4-后端设计)
5. [前端设计](#5-前端设计)
6. [Docker 部署](#6-docker-部署)
7. [API 接口](#7-api-接口)
8. [运营方案](#8-运营方案)
9. [实施计划](#9-实施计划)

---

## 1. 项目概述

### 1.1 项目信息

| 项目 | 说明 |
|------|------|
| 名称 | ai-share |
| 类型 | 内容/博客网站 |
| 模式 | 全栈开发 + 运营一体化 |
| 目标 | 构建一个现代化的内容分享平台 |
| 状态 | **已实施** |

### 1.2 核心功能

- **内容管理**：文章发布、编辑、删除、分类、标签
- **用户系统**：注册登录、第三方登录（Google/GitHub）、个人中心
- **互动功能**：评论、点赞、收藏
- **媒体管理**：图片/文件上传（MinIO 对象存储）
- **搜索功能**：全文搜索（PostgreSQL FTS）
- **后台管理**：内容审核、用户管理、数据统计

---

## 2. 技术架构

### 2.1 技术选型总览

| 分层 | 技术栈 | 版本 |
|------|--------|------|
| **后端框架** | Spring Boot | 4.0.4 |
| **JDK** | OpenJDK | 21 |
| **前端框架** | Vue 3 | 3.4+ |
| **构建工具** | Vite | 5.x |
| **数据库** | PostgreSQL | 16-alpine |
| **ORM** | MyBatis-Plus | 3.5.16 |
| **缓存** | Redis | 7-alpine |
| **对象存储** | MinIO | latest |
| **安全框架** | Spring Security | 7.0.4 |
| **认证方式** | OAuth2 + JWT | - |
| **部署方式** | Docker Compose | - |

### 2.2 架构架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         用户层                               │
│                    (浏览器/移动端)                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      Nginx 前端                              │
│              (registry.cn-shanghai.../nginx:alpine)          │
│              静态资源 + API 反向代理                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                 Spring Boot 后端                              │
│    (registry.cn-shanghai.../eclipse-temurin:21-jre)         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Controller  │  Service  │  Repository  │  Security  │    │
│  └─────────────────────────────────────────────────────┘    │
└────┬──────────────┬──────────────┬──────────────────────────┘
     │              │              │
     ▼              ▼              ▼
┌─────────┐   ┌─────────┐   ┌─────────┐
│PostgreSQL│   │  Redis  │   │  MinIO  │
│  16-alpine│   │  7-alpine│   │ latest  │
│  数据存储 │   │  缓存   │   │ 文件存储 │
└─────────┘   └─────────┘   └─────────┘
```

### 2.3 依赖版本详情

```xml
<!-- Spring Boot 父项目 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.4</version>
</parent>

<!-- 关键依赖版本 -->
<properties>
    <java.version>21</java.version>
    <mybatis-plus.version>3.5.16</mybatis-plus.version>
    <spring-security.version>7.0.4</spring-security.version>
    <minio.version>8.5.14</minio.version>
    <jjwt.version>0.12.6</jjwt.version>
</properties>
```

---

## 3. 数据库设计

### 3.1 ER 图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   users     │       │  categories │       │    tags     │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id          │       │ id          │       │ id          │
│ uuid        │       │ name        │       │ name        │
│ username    │       │ slug        │       │ slug        │
│ email       │       │ description │       │ created_at  │
│ password    │       │ parent_id   │       └─────────────┘
│ avatar      │       │ sort_order  │             │
│ role        │       │ created_at  │             │
│ status      │       └─────────────┘             │
│ provider    │             │                     │
│ provider_id │             │                     │
└─────────────┘             │                     │
      │                     │                     │
      │                     ▼                     ▼
      │             ┌─────────────────────────────────┐
      │             │           articles              │
      │             ├─────────────────────────────────┤
      │             │ id, title, slug, summary        │
      │             │ content, cover_image            │
      │             │ author_id (→users)              │
      │             │ category_id (→categories)       │
      │             │ status, view_count, like_count  │
      │             │ published_at, created_at        │
      │             └─────────────┬───────────────────┘
      │                           │
      │             ┌─────────────┴─────────────┐
      │             │                           │
      │             ▼                           ▼
      │     ┌───────────────┐         ┌─────────────────┐
      │     │ article_tags  │         │   comments      │
      │     ├───────────────┤         ├─────────────────┤
      │     │ article_id    │         │ id, content     │
      │     │ tag_id        │         │ article_id      │
      │     └───────────────┘         │ user_id         │
      │                               │ parent_id       │
      │                               │ status          │
      │                               └─────────────────┘
      │
      │             ┌─────────────────┐
      │             │  article_likes  │
      │             ├─────────────────┤
      │             │ article_id      │
      │             │ user_id         │
      │             └─────────────────┘
      │
      │             ┌─────────────────┐
      └────────────▶│     files       │
                    ├─────────────────┤
                    │ uuid, file_name │
                    │ file_path       │
                    │ bucket, size    │
                    │ mime_type       │
                    │ uploader_id     │
                    └─────────────────┘
```

### 3.2 表结构 DDL

```sql
-- =============================================
-- 数据库初始化
-- =============================================
CREATE DATABASE ai_share
    WITH ENCODING 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- 启用扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- 模糊搜索支持

-- =============================================
-- 1. 用户表 (users)
-- =============================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    avatar VARCHAR(255),
    role SMALLINT NOT NULL DEFAULT 1, -- 1:普通用户 2:作者 3:管理员
    status SMALLINT NOT NULL DEFAULT 1, -- 0:禁用 1:正常
    provider VARCHAR(20) DEFAULT 'local', -- local/google/github
    provider_id VARCHAR(255),
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 2. 分类表 (categories)
-- =============================================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    parent_id BIGINT REFERENCES categories(id),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 3. 标签表 (tags)
-- =============================================
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 4. 文章表 (articles)
-- =============================================
CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) UNIQUE,
    summary VARCHAR(500),
    content TEXT NOT NULL,
    cover_image VARCHAR(255),
    author_id BIGINT NOT NULL REFERENCES users(id),
    category_id BIGINT REFERENCES categories(id),
    status SMALLINT DEFAULT 0, -- 0:草稿 1:已发布 2:下架
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 5. 文章标签关联表 (article_tags)
-- =============================================
CREATE TABLE article_tags (
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

-- =============================================
-- 6. 评论表 (comments)
-- =============================================
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    parent_id BIGINT REFERENCES comments(id),
    status SMALLINT DEFAULT 1, -- 0:待审核 1:通过 2:拒绝
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 7. 文章点赞表 (article_likes)
-- =============================================
CREATE TABLE article_likes (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (article_id, user_id)
);

-- =============================================
-- 8. 文件表 (files) - MinIO 元数据
-- =============================================
CREATE TABLE files (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    bucket VARCHAR(100) DEFAULT 'ai-share',
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    uploader_id BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 索引
-- =============================================
CREATE INDEX idx_articles_author ON articles(author_id);
CREATE INDEX idx_articles_status ON articles(status);
CREATE INDEX idx_articles_published_at ON articles(published_at);
CREATE INDEX idx_comments_article ON comments(article_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider, provider_id);
CREATE INDEX idx_files_uploader ON files(uploader_id);
CREATE INDEX idx_articles_title_trgm ON articles USING gin (title gin_trgm_ops);
```

---

## 4. 后端设计

### 4.1 项目结构

```
backend/
├── Dockerfile
├── pom.xml
└── src/main/
    ├── java/com/example/aishare/
    │   ├── AiShareApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java        # 安全配置
    │   │   ├── MinioConfig.java           # MinIO 配置
    │   │   ├── RedisConfig.java           # Redis 配置
    │   │   └── CorsConfig.java            # CORS 配置
    │   ├── controller/
    │   │   ├── AuthController.java        # 认证接口
    │   │   ├── ArticleController.java     # 文章接口
    │   │   ├── CategoryController.java    # 分类接口
    │   │   ├── TagController.java         # 标签接口
    │   │   ├── CommentController.java     # 评论接口
    │   │   └── UploadController.java      # 上传接口
    │   ├── service/
    │   │   ├── UserService.java
    │   │   ├── ArticleService.java
    │   │   ├── CategoryService.java
    │   │   ├── TagService.java
    │   │   ├── CommentService.java
    │   │   └── MinioService.java
    │   ├── mapper/
    │   │   ├── UserMapper.java
    │   │   ├── ArticleMapper.java
    │   │   ├── CategoryMapper.java
    │   │   ├── TagMapper.java
    │   │   └── CommentMapper.java
    │   ├── entity/
    │   │   ├── User.java
    │   │   ├── Article.java
    │   │   ├── Category.java
    │   │   ├── Tag.java
    │   │   └── Comment.java
    │   ├── dto/
    │   │   ├── request/
    │   │   └── response/
    │   ├── security/
    │   │   ├── JwtTokenProvider.java
    │   │   ├── JwtAuthenticationFilter.java
    │   │   └── OAuth2SuccessHandler.java
    │   └── common/
    │       ├── exception/
    │       ├── result/
    │       └── constants/
    └── resources/
        ├── application.yml
        ├── application-docker.yml
        └── db/migration/
```

### 4.2 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.4</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>ai-share</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>ai-share</name>
    <description>AI Share - 内容分享平台</description>

    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.16</mybatis-plus.version>
        <spring-security.version>7.0.4</spring-security.version>
        <minio.version>8.5.14</minio.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
            <version>${spring-security.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-client</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- MinIO -->
        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
            <version>${minio.version}</version>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### 4.3 后端 Dockerfile

```dockerfile
# ============================================
# 构建阶段
# ============================================
FROM registry.cn-shanghai.aliyuncs.com/bountyteam/maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================
# 运行阶段
# ============================================
FROM registry.cn-shanghai.aliyuncs.com/bountyteam/eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 5. 前端设计

### 5.1 项目结构

```
frontend/
├── Dockerfile
├── nginx.conf
├── package.json
├── vite.config.ts
├── index.html
└── src/
    ├── main.ts
    ├── App.vue
    ├── api/
    │   ├── request.ts           # Axios 配置
    │   ├── auth.ts              # 认证 API
    │   ├── article.ts           # 文章 API
    │   ├── category.ts          # 分类 API
    │   ├── tag.ts               # 标签 API
    │   ├── comment.ts           # 评论 API
    │   └── upload.ts            # 上传 API
    ├── assets/
    │   ├── styles/
    │   │   ├── variables.scss
    │   │   └── global.scss
    │   └── images/
    ├── components/
    │   ├── common/
    │   │   ├── BaseButton.vue
    │   │   ├── BaseInput.vue
    │   │   ├── BaseModal.vue
    │   │   └── BasePagination.vue
    │   ├── article/
    │   │   ├── ArticleList.vue
    │   │   ├── ArticleCard.vue
    │   │   ├── ArticleDetail.vue
    │   │   └── ArticleEditor.vue
    │   ├── comment/
    │   │   ├── CommentList.vue
    │   │   └── CommentForm.vue
    │   └── upload/
    │       ├── FileUploader.vue
    │       └── ImageUploader.vue
    ├── views/
    │   ├── home/
    │   │   └── Index.vue
    │   ├── article/
    │   │   ├── List.vue
    │   │   └── Detail.vue
    │   ├── auth/
    │   │   ├── Login.vue
    │   │   └── OAuthCallback.vue
    │   ├── user/
    │   │   └── Center.vue
    │   └── admin/
    │       ├── Dashboard.vue
    │       ├── ArticleManager.vue
    │       └── UserManager.vue
    ├── router/
    │   └── index.ts
    ├── stores/
    │   ├── auth.ts
    │   ├── article.ts
    │   └── ui.ts
    ├── composables/
    │   ├── useAuth.ts
    │   └── useUpload.ts
    ├── utils/
    │   ├── format.ts
    │   └── storage.ts
    └── types/
        └── index.ts
```

### 5.2 首页分类导航设计

#### 5.2.1 设计方案

采用**标签式分类导航**，在文章列表顶部添加分类标签栏，点击即可筛选文章。

#### 5.2.2 页面布局

```
┌─────────────────────────────────────────────────────────────────┐
│  [全部] [技术专栏] [行业洞察] [教程系列] [社区内容]             │
├─────────────────────────────────────────────────────────────────┤
│  🔍 搜索文章...                                    [搜索]       │
├─────────────────────────────────────────────────────────────────┤
│  文章列表...                                                    │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 文章标题                                                 │   │
│  │ 文章摘要...                                              │   │
│  │ 👁 128  ⭐ 32  💬 8  2026-03-25                          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### 5.2.3 交互设计

| 交互 | 行为 |
|------|------|
| 点击分类标签 | 筛选该分类下的文章，标签高亮显示 |
| 点击"全部" | 显示所有分类的文章 |
| 分类 + 搜索 | 在选中分类范围内搜索 |
| 切换分类 | 重置分页到第一页 |

#### 5.2.4 样式规范

```scss
// 分类标签栏
.category-tabs {
  display: flex;
  gap: 0.5rem;
  padding: 1rem 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 1rem;

  .category-tab {
    padding: 0.5rem 1rem;
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.2s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }

    &:hover:not(.active) {
      background: rgba(102, 126, 234, 0.1);
      color: #667eea;
    }
  }
}
```

#### 5.2.5 数据流

```
用户点击分类 → 更新 selectedCategoryId → 触发 loadArticles() → API 请求 → 更新文章列表
```

### 5.2 前端 Dockerfile

```dockerfile
# ============================================
# 构建阶段
# ============================================
FROM registry.cn-shanghai.aliyuncs.com/bountyteam/node:20-alpine AS builder

WORKDIR /app

# 设置 npm 镜像
RUN npm config set registry https://registry.npmmirror.com

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# ============================================
# 生产阶段
# ============================================
FROM registry.cn-shanghai.aliyuncs.com/bountyteam/nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 5.3 nginx.conf

```nginx
worker_processes auto;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 1000;

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        # SPA 路由
        location / {
            try_files $uri $uri/ /index.html;
        }

        # API 反向代理
        location /api/ {
            proxy_pass http://backend:8080;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_pass_header X-CSRF-TOKEN;
        }

        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }

        # 健康检查
        location = /health {
            access_log off;
            return 200 "ok\n";
            add_header Content-Type text/plain;
        }
    }
}
```

---

## 6. Docker 部署

### 6.1 Docker Compose 配置

```yaml
version: '3.8'

services:
  # PostgreSQL 数据库
  postgres:
    image: registry.cn-shanghai.aliyuncs.com/bountyteam/postgres:16-alpine
    container_name: ai-share-postgres
    environment:
      POSTGRES_DB: ai_share
      POSTGRES_USER: ai_share
      POSTGRES_PASSWORD: ai_share_password
      TZ: Asia/Shanghai
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backend/migrations:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ai_share -d ai_share"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - ai-share-network
    restart: unless-stopped

  # Redis 缓存
  redis:
    image: registry.cn-shanghai.aliyuncs.com/bountyteam/redis:7-alpine
    container_name: ai-share-redis
    environment:
      TZ: Asia/Shanghai
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - ai-share-network
    restart: unless-stopped

  # MinIO 对象存储
  minio:
    image: registry.cn-shanghai.aliyuncs.com/bountyteam/minio
    container_name: ai-share-minio
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin123
      TZ: Asia/Shanghai
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data
    command: server /data --console-address ":9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - ai-share-network
    restart: unless-stopped

  # MinIO 初始化
  minio-init:
    image: registry.cn-shanghai.aliyuncs.com/bountyteam/mc
    container_name: ai-share-minio-init
    depends_on:
      minio:
        condition: service_healthy
    entrypoint: >
      /bin/sh -c "
      mc alias set myminio http://minio:9000 minioadmin minioadmin123;
      mc mb --ignore-existing myminio/ai-share;
      mc mb --ignore-existing myminio/ai-share-public;
      mc policy set download myminio/ai-share-public;
      exit 0;
      "
    networks:
      - ai-share-network

  # Spring Boot 后端
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: ai-share-backend
    environment:
      SPRING_PROFILES_ACTIVE: docker
      TZ: Asia/Shanghai
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: ai_share
      DB_USER: ai_share
      DB_PASSWORD: ai_share_password
      REDIS_HOST: redis
      REDIS_PORT: 6379
      MINIO_ENDPOINT: http://minio:9000
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin123
      MINIO_BUCKET: ai-share
      JWT_SECRET: ${JWT_SECRET}
      GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
      GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}
      GITHUB_CLIENT_ID: ${GITHUB_CLIENT_ID}
      GITHUB_CLIENT_SECRET: ${GITHUB_CLIENT_SECRET}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      minio:
        condition: service_healthy
    networks:
      - ai-share-network
    restart: unless-stopped

  # Vue 前端
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
      args:
        VITE_API_BASE_URL: http://localhost:8080
    container_name: ai-share-frontend
    environment:
      TZ: Asia/Shanghai
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - ai-share-network
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:
  minio_data:

networks:
  ai-share-network:
    driver: bridge
```

### 6.2 Docker 镜像汇总

| 用途 | 镜像 |
|------|------|
| Maven 构建 | `registry.cn-shanghai.aliyuncs.com/bountyteam/maven:3.9-eclipse-temurin-21` |
| JRE 运行 | `registry.cn-shanghai.aliyuncs.com/bountyteam/eclipse-temurin:21-jre-alpine` |
| Node 构建 | `registry.cn-shanghai.aliyuncs.com/bountyteam/node:20-alpine` |
| Nginx 运行 | `registry.cn-shanghai.aliyuncs.com/bountyteam/nginx:alpine` |
| PostgreSQL | `registry.cn-shanghai.aliyuncs.com/bountyteam/postgres:16-alpine` |
| Redis | `registry.cn-shanghai.aliyuncs.com/bountyteam/redis:7-alpine` |
| MinIO | `registry.cn-shanghai.aliyuncs.com/bountyteam/minio` |
| MinIO Client | `registry.cn-shanghai.aliyuncs.com/bountyteam/mc` |

### 6.3 环境变量 (.env.example)

```bash
# =====================
# JWT 配置
# =====================
JWT_SECRET=your-super-secret-jwt-key-change-in-production-please
JWT_EXPIRATION=7200000

# =====================
# Google OAuth2
# =====================
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=https://yourdomain.com/api/auth/google/callback

# =====================
# GitHub OAuth2
# =====================
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
GITHUB_REDIRECT_URI=https://yourdomain.com/api/auth/github/callback

# =====================
# MinIO 配置
# =====================
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123
MINIO_BUCKET=ai-share

# =====================
# 数据库配置
# =====================
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ai_share
DB_USER=ai_share
DB_PASSWORD=ai_share_password
```

### 6.4 部署命令

```bash
# 1. 复制环境变量
cp .env.example .env

# 2. 编辑 .env，填入配置

# 3. 构建并启动
docker-compose up -d --build

# 4. 查看日志
docker-compose logs -f

# 5. 停止服务
docker-compose down

# 6. 清理数据卷（谨慎）
docker-compose down -v
```

---

## 7. API 接口

### 7.1 认证模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/auth/google` | Google 登录入口 | ❌ |
| GET | `/api/auth/github` | GitHub 登录入口 | ❌ |
| GET | `/api/auth/google/callback` | Google 回调 | ❌ |
| GET | `/api/auth/github/callback` | GitHub 回调 | ❌ |
| POST | `/api/auth/logout` | 登出 | ✅ |
| GET | `/api/auth/me` | 获取当前用户 | ✅ |

### 7.2 文章模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/articles` | 文章列表 | ❌ |
| GET | `/api/articles/{id}` | 文章详情 | ❌ |
| GET | `/api/articles/slug/{slug}` | Slug 查询 | ❌ |
| POST | `/api/articles` | 创建文章 | ✅ |
| PUT | `/api/articles/{id}` | 更新文章 | ✅ |
| DELETE | `/api/articles/{id}` | 删除文章 | ✅ |
| POST | `/api/articles/{id}/publish` | 发布文章 | ✅ |
| POST | `/api/articles/{id}/like` | 点赞文章 | ✅ |
| DELETE | `/api/articles/{id}/like` | 取消点赞 | ✅ |

### 7.3 分类模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/categories` | 分类列表 | ❌ |
| GET | `/api/categories/tree` | 分类树 | ❌ |
| POST | `/api/categories` | 创建分类 | ✅ 管理员 |
| PUT | `/api/categories/{id}` | 更新分类 | ✅ 管理员 |
| DELETE | `/api/categories/{id}` | 删除分类 | ✅ 管理员 |

### 7.4 标签模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/tags` | 标签列表 | ❌ |
| GET | `/api/tags/hot` | 热门标签 | ❌ |
| POST | `/api/tags` | 创建标签 | ✅ 管理员 |
| PUT | `/api/tags/{id}` | 更新标签 | ✅ 管理员 |
| DELETE | `/api/tags/{id}` | 删除标签 | ✅ 管理员 |

### 7.5 评论模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/articles/{articleId}/comments` | 文章评论 | ❌ |
| POST | `/api/articles/{articleId}/comments` | 发表评论 | ✅ |
| PUT | `/api/comments/{id}` | 更新评论 | ✅ 作者 |
| DELETE | `/api/comments/{id}` | 删除评论 | ✅ 作者/管理员 |

### 7.6 文件上传模块

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/upload/presigned-url` | 获取预签名 URL | ✅ |
| POST | `/api/upload/confirm` | 确认上传完成 | ✅ |
| DELETE | `/api/upload/{id}` | 删除文件 | ✅ |
| GET | `/api/upload/files` | 文件列表 | ✅ |

---

## 8. 运营方案

### 8.1 内容运营策略

```
内容分类：
├── 技术专栏
│   ├── Spring Boot 实战
│   ├── Vue 前端开发
│   └── 架构设计
├── 行业洞察
├── 教程系列
└── 社区内容

发布流程：
选题 → 创作 → 初审 → 复审 → 发布

审核机制：
一级：自动（敏感词检测、格式校验）
二级：编辑（内容质量、SEO 检查）
三级：主编（专业准确性、风险评估）
```

### 8.2 SEO 优化方案

| 优化项 | 实现方式 |
|--------|----------|
| Sitemap | 动态生成 XML，提交搜索引擎 |
| robots.txt | 配置抓取规则 |
| 结构化数据 | Schema.org Article 标记 |
| URL 优化 | SEO 友好 Slug 生成 |
| 页面速度 | Gzip 压缩、CDN 加速 |
| 移动端 | 响应式设计 |

### 8.3 用户增长策略

```
获客渠道：
├── SEO 自然搜索（P0）
├── 技术社区 - 掘金/CSDN（P0）
├── 微信公众号（P0）
├── 知乎专栏（P1）
└── GitHub 开源项目（P1）

留存策略：
├── 新手引导流程
├── 积分/会员体系
├── 邮件推送
└── 社群运营

增长目标：
启动期（1-2 月）：100 篇文章，500 注册
成长期（3-6 月）：1 万 UV，5000 注册
成熟期（7-12 月）：10 万 UV，5 万注册
```

---

## 9. 实施计划

### 9.1 任务列表

| ID | 任务 | 预计工时 | 状态 |
|---|------|----------|------|
| 1 | 项目初始化（后端 pom.xml + 前端 package.json） | 1 天 | 待开始 |
| 2 | PostgreSQL 数据库建表 | 0.5 天 | 待开始 |
| 3 | 后端 API 开发（含 OAuth2/MinIO 集成） | 5-7 天 | 待开始 |
| 4 | 前端页面开发（Vue 3） | 5-7 天 | 待开始 |
| 5 | Docker Compose 部署配置 | 1 天 | 待开始 |
| 6 | 运营体系建设 | 持续 | 待开始 |

### 9.2 项目目录结构

```
ai-share/
├── docs/
│   └── DESIGN.md              # 本设计文档
├── .env.example               # 环境变量模板
├── docker-compose.yml         # Docker 编排
│
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/aishare/
│   │       └── resources/
│   └── migrations/
│
└── frontend/
    ├── Dockerfile
    ├── nginx.conf
    ├── package.json
    ├── index.html
    └── src/
```

---

## 附录

### A. 关键配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| JWT_SECRET | JWT 签名密钥 | 需自定义 |
| JWT_EXPIRATION | JWT 有效期 | 7200000ms (2 小时) |
| MINIO_BUCKET | MinIO 存储桶 | ai-share |
| POSTGRES_DB | 数据库名 | ai_share |

### B. 第三方登录申请

- **Google OAuth2**: https://console.cloud.google.com/
- **GitHub OAuth2**: https://github.com/settings/developers

### C. 服务器要求

| 资源 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核 |
| 内存 | 4GB | 8GB |
| 磁盘 | 20GB | 50GB+ |
| Docker | 24.x+ | 24.x+ |

---

**文档结束**

如需修改或补充，请告知具体内容。
