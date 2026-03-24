# ai-share 部署文档

## 部署前准备

### 1. 域名配置

将你的域名解析到服务器 IP：
```
A 记录：yourdomain.com -> 服务器 IP
A 记录：www.yourdomain.com -> 服务器 IP
```

### 2. SSL 证书申请

推荐使用 Let's Encrypt 免费证书：

```bash
# 安装 certbot
apt-get update
apt-get install -y certbot python3-certbot-nginx

# 获取证书
certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

### 3. 环境变量配置

复制并编辑环境变量文件：
```bash
cp .env.example .env
```

编辑 `.env` 文件，配置以下内容：

```bash
# JWT 密钥（必须修改）
JWT_SECRET=your-super-secret-jwt-key-$(openssl rand -hex 32)

# Google OAuth2（需要到 Google Cloud Console 申请）
GOOGLE_CLIENT_ID=xxx.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=xxx
GOOGLE_REDIRECT_URI=http://yourdomain.com/api/auth/google/callback

# GitHub OAuth2（需要到 GitHub Settings 申请）
GITHUB_CLIENT_ID=xxx
GITHUB_CLIENT_SECRET=xxx
GITHUB_REDIRECT_URI=http://yourdomain.com/api/auth/github/callback
```

### 4. OAuth2 应用申请

#### Google OAuth2
1. 访问 [Google Cloud Console](https://console.cloud.google.com/)
2. 创建新项目
3. 启用 Google+ API
4. 创建 OAuth2 凭证
5. 添加授权重定向 URI：`http://yourdomain.com/api/auth/google/callback`

#### GitHub OAuth2
1. 访问 [GitHub Developer Settings](https://github.com/settings/developers)
2. 创建新 OAuth App
3. 设置 Authorization callback URL：`http://yourdomain.com/api/auth/github/callback`
4. 复制 Client ID 和 Client Secret

## 生产环境部署

### 1. 安装 Docker

```bash
# 卸载旧版本
sudo apt-get remove docker docker-engine docker.io containerd runc

# 安装 Docker
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun

# 启动 Docker
sudo systemctl enable docker
sudo systemctl start docker

# 验证安装
docker --version
docker compose version
```

### 2. 安装 Docker Compose

```bash
# 安装 Docker Compose V2
sudo apt-get update
sudo apt-get install -y docker-compose-plugin

# 验证安装
docker compose version
```

### 3. 部署应用

```bash
# 克隆项目
git clone <repository-url>
cd ai-share

# 配置环境变量
cp .env.example .env
# 编辑 .env 文件...

# 构建并启动
docker compose up -d --build

# 查看日志
docker compose logs -f

# 检查运行状态
docker compose ps
```

### 4. 配置 Nginx 反向代理（可选）

如果需要使用自定义域名和 HTTPS，可以配置 Nginx 反向代理：

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name yourdomain.com www.yourdomain.com;

    # HTTP 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    # SSL 证书配置
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 前端
    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 常用运维命令

### 查看服务状态
```bash
docker compose ps
```

### 查看日志
```bash
# 所有服务日志
docker compose logs -f

# 单个服务日志
docker compose logs -f backend
docker compose logs -f frontend
```

### 重启服务
```bash
# 重启所有服务
docker compose restart

# 重启单个服务
docker compose restart backend
```

### 更新部署
```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker compose up -d --build
```

### 停止服务
```bash
docker compose down
```

### 清理数据（谨慎使用）
```bash
# 停止并删除容器，保留数据卷
docker compose down

# 停止并删除容器和数据卷（会删除所有数据）
docker compose down -v
```

## 数据备份

### 备份 PostgreSQL 数据库
```bash
# 导出数据库
docker compose exec postgres pg_dump -U ai_share ai_share > backup_$(date +%Y%m%d).sql

# 恢复数据库
docker compose exec -T postgres psql -U ai_share -d ai_share < backup_20260324.sql
```

### 备份 MinIO 数据
```bash
# 使用 mc 工具备份
mc mirror myminio/ai-share /path/to/backup
```

## 故障排查

### 容器无法启动
```bash
# 查看容器日志
docker compose logs backend

# 检查容器状态
docker compose ps

# 检查端口占用
netstat -tlnp | grep 8080
```

### 数据库连接失败
```bash
# 检查 PostgreSQL 是否运行
docker compose ps postgres

# 测试数据库连接
docker compose exec postgres psql -U ai_share -d ai_share
```

### MinIO 访问失败
```bash
# 检查 MinIO 状态
docker compose ps minio

# 访问 MinIO 控制台
# http://your-ip:9001
```

## 性能优化

### 1. 调整 JVM 参数
编辑 `backend/Dockerfile`：
```dockerfile
ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-jar", "app.jar"]
```

### 2. 配置 Redis 缓存
确保 Redis 持久化：
```yaml
volumes:
  - redis_data:/data
command: redis-server --appendonly yes
```

### 3. CDN 加速
将静态资源（图片、CSS、JS）配置 CDN 加速。

## 监控告警

### 1. 应用监控
推荐集成：
- Spring Boot Actuator
- Prometheus + Grafana
- SkyWalking

### 2. 日志收集
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Loki + Grafana

### 3. 告警通知
- 钉钉机器人
- 企业微信
- Slack

## 安全建议

1. **修改默认密码**：数据库、Redis、MinIO 的默认密码必须修改
2. **启用 HTTPS**：生产环境必须使用 HTTPS
3. **防火墙配置**：仅开放必要端口（80, 443）
4. **定期更新**：及时更新系统和依赖包
5. **备份策略**：配置定时备份任务
