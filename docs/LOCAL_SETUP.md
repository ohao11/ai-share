# 本地开发环境配置指南

## 快速启动

### 方法一：使用启动脚本（推荐）

```bash
./start.sh
```

### 方法二：手动启动

```bash
# 1. 确保 .env 文件存在
cp .env.example .env

# 2. 编辑 .env 文件，配置必要的环境变量
# 特别是 JWT_SECRET 和 OAuth2 凭证

# 3. 启动服务
docker-compose up -d --build

# 4. 查看日志
docker-compose logs -f
```

## 环境变量说明

### 必需配置

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `JWT_SECRET` | JWT 签名密钥（至少32位） | `your-secret-key-here` |

### OAuth2 配置（可选）

| 变量名 | 说明 | 获取地址 |
|--------|------|----------|
| `GOOGLE_CLIENT_ID` | Google OAuth2 客户端 ID | https://console.cloud.google.com/ |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 密钥 | https://console.cloud.google.com/ |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 客户端 ID | https://github.com/settings/developers |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 密钥 | https://github.com/settings/developers |

### 数据库配置（Docker 环境已预设）

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `DB_HOST` | postgres | Docker 服务名 |
| `DB_PORT` | 5432 | PostgreSQL 端口 |
| `DB_NAME` | ai_share | 数据库名 |
| `DB_USER` | ai_share | 数据库用户 |
| `DB_PASSWORD` | ai_share_password | 数据库密码 |

### MinIO 配置（Docker 环境已预设）

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `MINIO_ENDPOINT` | http://minio:9000 | MinIO 地址 |
| `MINIO_ACCESS_KEY` | minioadmin | 访问密钥 |
| `MINIO_SECRET_KEY` | minioadmin123 | 密钥 |
| `MINIO_BUCKET` | ai-share | 存储桶名 |

## 访问服务

启动后可通过以下地址访问：

- **前端应用**: http://localhost
- **后端 API**: http://localhost:8080
- **MinIO 控制台**: http://localhost:9001
  - 账号: `minioadmin`
  - 密码: `minioadmin123`

## 常用命令

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 停止所有服务
docker-compose down

# 停止并删除数据卷（谨慎使用）
docker-compose down -v

# 重启服务
docker-compose restart

# 重新构建并启动
docker-compose up -d --build
```

## 开发模式

### 后端开发

如果需要在本地开发后端（不通过 Docker）：

1. 修改 `.env` 中的数据库连接为本地地址：
```
DB_HOST=localhost
REDIS_HOST=localhost
MINIO_ENDPOINT=http://localhost:9000
```

2. 启动数据库等依赖服务：
```bash
docker-compose up -d postgres redis minio
```

3. 在本地运行后端：
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

前端开发服务器会在 http://localhost:5173 启动，并自动代理 API 请求到 http://localhost:8080。

## 故障排查

### 1. 服务启动失败

检查日志：
```bash
docker-compose logs -f [service-name]
```

### 2. 数据库连接失败

确保 postgres 服务已健康启动：
```bash
docker-compose ps
```

### 3. 端口冲突

如果 80 或 8080 端口被占用，修改 docker-compose.yml：
```yaml
ports:
  - "8081:8080"  # 修改为其他端口
```

### 4. OAuth2 登录失败

确保 OAuth2 回调地址正确配置：
- Google: `http://localhost:8080/api/auth/google/callback`
- GitHub: `http://localhost:8080/api/auth/github/callback`

## 数据持久化

数据默认存储在 Docker 卷中：

- `postgres_data` - PostgreSQL 数据
- `redis_data` - Redis 数据
- `minio_data` - MinIO 对象存储

如需备份数据：
```bash
# 备份 PostgreSQL
docker exec ai-share-postgres pg_dump -U ai_share ai_share > backup.sql

# 恢复 PostgreSQL
docker exec -i ai-share-postgres psql -U ai_share ai_share < backup.sql
```
