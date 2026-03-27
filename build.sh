#!/bin/bash
# 构建脚本 - 打包前后端并推送 Docker 镜像到阿里云镜像仓库

set -e

# 配置
REGISTRY="registry.cn-shanghai.aliyuncs.com/bountyteam"
PROJECT_NAME="ai-share"
VERSION_FILE="version"

# 解析参数
PUSH_IMAGES=true
while [[ $# -gt 0 ]]; do
  case $1 in
    --no-push)
      PUSH_IMAGES=false
      shift
      ;;
    *)
      shift
      ;;
  esac
done

echo "=========================================="
echo "构建 实战AI 项目镜像"
echo "=========================================="

# 读取当前版本
if [ -f "$VERSION_FILE" ]; then
  CURRENT_VERSION=$(cat "$VERSION_FILE" | tr -d '[:space:]')
else
  CURRENT_VERSION="0.0.0"
fi

# 解析版本号并递增
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"
NEW_PATCH=$((PATCH + 1))
NEW_VERSION="${MAJOR}.${MINOR}.${NEW_PATCH}"

echo ""
echo "当前版本: $CURRENT_VERSION"
echo "新版本:   $NEW_VERSION"
echo ""

# 构建后端 JAR
echo ">>> 构建后端 JAR..."
cd backend
mvn clean package -DskipTests
cd ..

# 镜像名称
BACKEND_IMAGE="${REGISTRY}/${PROJECT_NAME}-backend"
FRONTEND_IMAGE="${REGISTRY}/${PROJECT_NAME}-frontend"

# 构建后端镜像
echo ""
echo ">>> 构建后端 Docker 镜像..."
docker build -t ${BACKEND_IMAGE}:${NEW_VERSION} -t ${BACKEND_IMAGE}:latest -f backend/Dockerfile backend

# 构建前端镜像
echo ""
echo ">>> 构建前端 Docker 镜像..."
docker build -t ${FRONTEND_IMAGE}:${NEW_VERSION} -t ${FRONTEND_IMAGE}:latest -f frontend/Dockerfile frontend

# 推送到镜像仓库
if [ "$PUSH_IMAGES" = true ]; then
  echo ""
  echo ">>> 推送镜像到阿里云镜像仓库..."

  echo "推送 ${BACKEND_IMAGE}:${NEW_VERSION}"
  docker push ${BACKEND_IMAGE}:${NEW_VERSION}
  docker push ${BACKEND_IMAGE}:latest

  echo "推送 ${FRONTEND_IMAGE}:${NEW_VERSION}"
  docker push ${FRONTEND_IMAGE}:${NEW_VERSION}
  docker push ${FRONTEND_IMAGE}:latest
fi

# 更新版本文件
echo "$NEW_VERSION" > "$VERSION_FILE"

echo ""
echo "=========================================="
echo "构建完成!"
echo "  - ${BACKEND_IMAGE}:${NEW_VERSION}"
echo "  - ${FRONTEND_IMAGE}:${NEW_VERSION}"
echo "=========================================="
echo ""
echo "版本已更新: $CURRENT_VERSION -> $NEW_VERSION"
if [ "$PUSH_IMAGES" = true ]; then
  echo "镜像已推送到: $REGISTRY"
else
  echo "提示: 使用 --no-push 跳过推送，镜像仅在本地"
fi
echo ""
echo "运行 'docker compose up -d' 启动服务"