# AI Share 环境配置更新说明

## 更新内容 (2026-03-24)

### 1. docker-compose.yml 优化

#### 后端构建缓存
```yaml
backend:
  build:
    volumes:
      - ~/.m2/repository:/root/.m2/repository:rw
```
- 挂载本地 Maven 仓库到构建容器
- 避免每次构建重复下载依赖
- 大幅加速后端构建速度

#### 前端构建缓存
```yaml
frontend:
  build:
    volumes:
      - ~/.npm:/root/.npm:rw
```
- 挂载本地 npm 缓存到构建容器
- 避免重复下载 node_modules
- 加速前端构建

### 2. 环境变量配置 (.env)

当前已配置的环境变量：

| 变量名 | 当前值 | 说明 |
|--------|--------|------|
| JWT_SECRET | your-super-secret-jwt-key... | JWT签名密钥 |
| JWT_EXPIRATION | 7200000 | JWT过期时间(2小时) |
| GOOGLE_CLIENT_ID | 已配置 | Google OAuth2客户端ID |
| GOOGLE_CLIENT_SECRET | 已配置 | Google OAuth2密钥 |
| GITHUB_CLIENT_ID | 已配置 | GitHub OAuth2客户端ID |
| GITHUB_CLIENT_SECRET | 已配置 | GitHub OAuth2密钥 |
| DB_HOST | postgres | Docker网络中的数据库地址 |
| REDIS_HOST | redis | Docker网络中的Redis地址 |
| MINIO_ENDPOINT | http://minio:9000 | Docker网络中的MinIO地址 |

### 3. 首次启动前检查清单

```bash
# 1. 检查 .env 文件是否存在
ls -la .env

# 2. 如果不存在，从模板创建
cp .env.example .env

# 3. 检查 Docker 是否运行
docker info

# 4. 检查 Maven 缓存目录是否存在
ls -la ~/.m2/repository

# 5. 检查 npm 缓存目录是否存在
ls -la ~/.npm
```

### 4. 启动命令

#### 方式一：使用启动脚本
```bash
./start.sh
```

#### 方式二：手动启动
```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f
```

### 5. 缓存目录说明

构建缓存会存储在以下位置：

| 类型 | 本地路径 | 用途 |
|------|----------|------|
| Maven | ~/.m2/repository | Java依赖缓存 |
| npm | ~/.npm | Node.js依赖缓存 |

首次构建后，缓存会自动保存到这些目录，后续构建会复用。

### 6. 清理缓存（如需重新下载依赖）

```bash
# 清理 Maven 缓存
rm -rf ~/.m2/repository

# 清理 npm 缓存
rm -rf ~/.npm

# 清理 Docker 构建缓存
docker builder prune -f
```

### 7. 常见问题

**Q: 构建时提示权限错误**
```bash
# 修复缓存目录权限
chmod -R 755 ~/.m2
chmod -R 755 ~/.npm
```

**Q: 依赖下载慢**
- Maven: 已在 Dockerfile 中配置阿里云镜像
- npm: 已在 Dockerfile 中配置淘宝镜像

**Q: 端口冲突**
```bash
# 修改 docker-compose.yml 中的端口映射
ports:
  - "8081:8080"  # 后端改为8081
  - "8082:80"    # 前端改为8082
```
