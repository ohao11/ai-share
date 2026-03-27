# 实战AI

一个现代化的 AI 内容分享平台，基于 Spring Boot 4 + Vue 3 构建，支持文章发布、评论互动、OAuth2 社交登录等功能。

## ✨ 功能特性

### 内容管理
- 📝 **文章系统** - 支持 Markdown 编辑，分类/标签管理，草稿/发布状态
- 🏷️ **标签分类** - 灵活的标签筛选，文章数量统计
- 🔍 **全文搜索** - PostgreSQL 全文搜索支持
- 💬 **评论系统** - 嵌套评论回复

### 用户系统
- 🔐 **安全登录** - 密码 RSA 加密传输
- 🌐 **OAuth2 登录** - 支持 Google、GitHub 社交登录
- 👤 **个人中心** - 用户文章管理

### 管理后台
- 📊 **数据统计** - 用户、文章、浏览量统计
- 👥 **用户管理** - 禁用/启用用户
- 📑 **内容审核** - 文章管理

### 安全特性
- 🔒 JWT Token 认证
- 🛡️ 横向越权防护
- 🔐 密码 RSA 加密传输
- ✅ 密码强度验证

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| **后端框架** | Spring Boot 4.0.4, JDK 21 |
| **安全认证** | Spring Security 7, JWT, RSA |
| **数据库** | PostgreSQL 16, Redis 7 |
| **ORM** | MyBatis-Plus 3.5.16 |
| **对象存储** | MinIO |
| **前端框架** | Vue 3, TypeScript, Vite 5 |
| **UI 组件** | Element Plus |
| **状态管理** | Pinia |
| **部署** | Docker Compose |

## 🚀 快速开始

### 环境要求
- Docker 24.x+
- Docker Compose 2.x+

### 1. 克隆项目
```bash
git clone https://github.com/ohao11/ai-share.git
cd ai-share
```

### 2. 配置环境变量
```bash
cp .env.example .env
```

编辑 `.env` 文件，必须配置以下变量：
```env
# JWT 密钥（必须修改）
JWT_SECRET=your-secret-key-at-least-32-characters

# OAuth2 凭证（可选）
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
```

### 3. 构建与部署

**构建镜像：**
```bash
# 构建并推送到阿里云镜像仓库（版本号自动+1）
./build.sh

# 仅本地构建，不推送
./build.sh --no-push
```

**启动服务：**
```bash
# 拉取镜像并启动
docker compose pull && docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f backend
```

**版本管理：**
- 版本号存储在 `version` 文件中
- 每次 `./build.sh` 自动递增补丁版本号 (如 0.0.1 → 0.0.2)
- 镜像同时打上版本标签和 `latest` 标签

**镜像地址：**
| 镜像 | 地址 |
|------|------|
| 后端 | `registry.cn-shanghai.aliyuncs.com/bountyteam/ai-share-backend` |
| 前端 | `registry.cn-shanghai.aliyuncs.com/bountyteam/ai-share-frontend` |

### 4. 访问应用
| 服务 | 地址 |
|------|------|
| 前端应用 | http://localhost |
| 后端 API | http://localhost:8080 |
| MinIO 控制台 | http://localhost:9001 |

**默认账号：**
- MinIO: `minioadmin` / `minioadmin123`

## 📁 项目结构

```
ai-share/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/example/aishare/
│   │       ├── config/         # 配置类
│   │       ├── controller/     # 控制器
│   │       ├── service/        # 业务逻辑
│   │       ├── mapper/         # MyBatis Mapper
│   │       ├── entity/         # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── security/       # 安全相关
│   │       └── common/         # 通用组件
│   ├── src/main/resources/
│   │   ├── application.yml     # 配置文件
│   │   └── db/migration/       # 数据库迁移
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/                # API 接口
│   │   ├── components/         # 组件
│   │   ├── router/             # 路由
│   │   ├── stores/             # Pinia 状态
│   │   ├── types/              # TypeScript 类型
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面
│   ├── Dockerfile
│   └── package.json
├── docs/                       # 文档
│   ├── PRD.md                  # 产品需求文档
│   └── DESIGN.md               # 设计文档
├── docker-compose.yml          # Docker 编排
├── build.sh                    # 构建脚本（版本自动递增）
├── version                     # 版本号文件
└── .env.example                # 环境变量模板
```

## 📖 API 文档

### 认证接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/auth/public-key` | 获取 RSA 公钥 |
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/register` | 用户注册 |
| GET | `/api/auth/me` | 获取当前用户 |
| POST | `/api/auth/logout` | 退出登录 |

### 文章接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/articles` | 文章列表（支持分页、分类、关键词） |
| GET | `/api/articles/{id}` | 文章详情 |
| POST | `/api/articles` | 创建文章 |
| PUT | `/api/articles/{id}` | 更新文章 |
| DELETE | `/api/articles/{id}` | 删除文章 |
| POST | `/api/articles/{id}/like` | 点赞/取消点赞 |
| GET | `/api/articles/tag/{tagId}` | 按标签筛选 |

### 管理接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/stats` | 统计数据 |
| GET | `/api/admin/users` | 用户列表 |
| PUT | `/api/admin/users/{id}/status` | 更新用户状态 |
| GET | `/api/admin/articles` | 文章列表（含草稿） |

## 🔧 开发指南

### 本地开发

**后端：**
```bash
cd backend
mvn spring-boot:run
```

**前端：**
```bash
cd frontend
npm install
npm run dev
```

### 数据库迁移
项目使用 Flyway 进行数据库版本管理，迁移脚本位于 `backend/src/main/resources/db/migration/`。

## 📝 环境变量

| 变量名 | 说明 | 必填 |
|--------|------|------|
| `JWT_SECRET` | JWT 签名密钥 (≥32字符) | ✅ |
| `JWT_EXPIRATION` | Token 有效期 (ms) | ❌ |
| `GOOGLE_CLIENT_ID` | Google OAuth2 客户端 ID | ❌ |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 密钥 | ❌ |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 客户端 ID | ❌ |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 密钥 | ❌ |
| `MINIO_ACCESS_KEY` | MinIO 访问密钥 | ❌ |
| `MINIO_SECRET_KEY` | MinIO 密钥 | ❌ |

## 🔒 安全说明

1. **密码传输加密** - 前端使用 RSA 公钥加密密码，后端私钥解密
2. **横向越权防护** - 文章/评论操作验证所有者权限
3. **密码强度验证** - 最少 8 位，必须包含字母和数字
4. **统一错误信息** - 登录失败统一提示，防止用户枚举

## 📄 许可证

[MIT License](LICENSE)

---

> 💡 如有问题或建议，欢迎提交 Issue 或 Pull Request。