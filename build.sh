#!/bin/bash
# 构建脚本 - 打包前后端并生成 Docker 镜像

set -e

echo "=========================================="
echo "构建 实战AI 项目镜像"
echo "=========================================="

# 项目名称
PROJECT_NAME="ai-share"
VERSION="${1:-latest}"

echo ""
echo ">>> 构建后端 JAR..."
cd backend
mvn clean package -DskipTests
cd ..

echo ""
echo ">>> 构建后端 Docker 镜像: ${PROJECT_NAME}-backend:${VERSION}"
docker build -t ${PROJECT_NAME}-backend:${VERSION} -f backend/Dockerfile backend

echo ""
echo ">>> 构建前端 Docker 镜像: ${PROJECT_NAME}-frontend:${VERSION}"
docker build -t ${PROJECT_NAME}-frontend:${VERSION} -f frontend/Dockerfile frontend

echo ""
echo "=========================================="
echo "构建完成!"
echo "  - ${PROJECT_NAME}-backend:${VERSION}"
echo "  - ${PROJECT_NAME}-frontend:${VERSION}"
echo "=========================================="
echo ""
echo "运行 'docker compose up -d' 启动服务"