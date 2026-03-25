# AI Share

一个基于 Spring Boot 4 + Vue 3 的现代化内容分享平台。

## 技术栈

### 后端
- Spring Boot 4.0.4
- JDK 21
- MyBatis-Plus 3.5.16
- Spring Security 7.0.4
- PostgreSQL 16
- Redis 7
- MinIO (对象存储)
- Docker Compose

### 前端
- Vue 3 + TypeScript
- Vite 5
- Element Plus
- Pinia
- Vue Router 4
- Axios

## 快速开始

### 环境要求
- Docker 24.x+
- Docker Compose 2.x+
- Node.js 20.x (仅开发需要)
- JDK 21 (仅开发需要)

### 1. 克隆项目
```bash
git clone <repository>
cd ai-share
```

### 2. 配置环境变量
```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，填入你的配置
# 必须配置: JWT_SECRET, Google/GitHub OAuth2 凭证
```

### 3. 启动服务
```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 4. 访问应用
- 前端：http://localhost
- 后端 API：http://localhost:8080
- MinIO 控制台：http://localhost:9001 (账号：minioadmin, 密码：minioadmin123)

### 5. 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `JWT_SECRET` | JWT 签名密钥 | 必须自定义 |
| `JWT_EXPIRATION` | JWT 有效期 (ms) | 7200000 |
| `GOOGLE_CLIENT_ID` | Google OAuth2 客户端 ID | - |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 密钥 | - |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 客户端 ID | - |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 密钥 | - |
| `MINIO_ACCESS_KEY` | MinIO 访问密钥 | minioadmin |
| `MINIO_SECRET_KEY` | MinIO 密钥 | minioadmin123 |

## 项目结构

```
ai-share/
├── backend/              # Spring Boot 后端
│   ├── src/main/
│   │   ├── java/
│   │   └── resources/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/             # Vue 3 前端
│   ├── src/
│   ├── Dockerfile
│   └── package.json
├── docs/                 # 文档
├── docker-compose.yml    # Docker 编排
├── .env                  # 环境变量配置
└── .env.example          # 环境变量模板
```

## API 接口

### 认证
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户
- `GET /api/auth/google` - Google 登录
- `GET /api/auth/github` - GitHub 登录

### 文章
- `GET /api/articles` - 获取文章列表
- `GET /api/articles/{id}` - 获取文章详情
- `POST /api/articles` - 创建文章
- `PUT /api/articles/{id}` - 更新文章
- `DELETE /api/articles/{id}` - 删除文章

### 分类
- `GET /api/categories` - 获取分类列表
- `GET /api/categories/{slug}` - 根据 Slug 获取分类

### 标签
- `GET /api/tags` - 获取标签列表
- `GET /api/tags/hot` - 获取热门标签

### 上传
- `POST /api/upload/presigned-url` - 获取预签名上传 URL
- `POST /api/upload/confirm` - 确认上传完成

## 开发指南

### 后端开发
```bash
cd backend
mvn spring-boot:run
```

### 前端开发
```bash
cd frontend
npm install
npm run dev
```

## 许可证

MIT License
