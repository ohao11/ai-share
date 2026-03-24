-- =============================================
-- ai-share 数据库初始化脚本
-- =============================================

-- 启用扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

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
-- 8. 文件表 (files)
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
CREATE INDEX idx_articles_category ON articles(category_id);
CREATE INDEX idx_articles_status ON articles(status);
CREATE INDEX idx_articles_published_at ON articles(published_at);
CREATE INDEX idx_comments_article ON comments(article_id);
CREATE INDEX idx_comments_user ON comments(user_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider, provider_id);
CREATE INDEX idx_files_uploader ON files(uploader_id);
CREATE INDEX idx_articles_title_trgm ON articles USING gin (title gin_trgm_ops);
CREATE INDEX idx_categories_parent ON categories(parent_id);

-- =============================================
-- 初始化数据
-- =============================================

-- 默认管理员账号 (密码需要后端加密后插入)
INSERT INTO users (username, email, role, provider)
VALUES ('admin', 'admin@example.com', 3, 'local');

-- 默认分类
INSERT INTO categories (name, slug, description, sort_order) VALUES
('技术专栏', 'tech', '技术相关文章', 1),
('行业洞察', 'insight', '行业趋势与洞察', 2),
('教程系列', 'tutorial', '入门与进阶教程', 3),
('社区内容', 'community', '社区用户投稿', 4);

-- 默认标签
INSERT INTO tags (name, slug) VALUES
('Spring Boot', 'spring-boot'),
('Vue', 'vue'),
('PostgreSQL', 'postgresql'),
('Docker', 'docker'),
('MinIO', 'minio'),
('Java', 'java'),
('前端', 'frontend'),
('后端', 'backend');
