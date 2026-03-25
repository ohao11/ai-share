#!/bin/bash

# AI Share 本地启动脚本
# 使用 Docker Compose 启动所有服务

set -e

echo "==================================="
echo "  AI Share 本地环境启动脚本"
echo "==================================="
echo ""

# 检查 .env 文件是否存在
if [ ! -f .env ]; then
    echo "⚠️  .env 文件不存在，正在从模板创建..."
    cp .env.example .env
    echo "✅ 已创建 .env 文件，请根据需要修改配置"
    echo ""
fi

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker"
    exit 1
fi

# 检查 docker-compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose 未安装"
    exit 1
fi

echo "🚀 正在启动服务..."
echo ""

# 停止旧的服务（如果有）
echo "🛑 停止旧的服务..."
docker-compose down > /dev/null 2>&1 || true

# 构建并启动服务
echo "🔨 构建并启动服务..."
docker-compose up -d --build

echo ""
echo "==================================="
echo "  ✅ 服务启动完成！"
echo "==================================="
echo ""
echo "访问地址："
echo "  • 前端应用: http://localhost"
echo "  • 后端 API: http://localhost:8080"
echo "  • MinIO 控制台: http://localhost:9001"
echo "     - 账号: minioadmin"
echo "     - 密码: minioadmin123"
echo ""
echo "常用命令："
echo "  • 查看日志: docker-compose logs -f"
echo "  • 停止服务: docker-compose down"
echo "  • 重启服务: docker-compose restart"
echo ""
echo "==================================="

# 显示服务状态
echo ""
echo "📊 服务状态："
docker-compose ps
