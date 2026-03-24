# ============================================
# 运行阶段 - 直接使用宿主机打好的 jar 包
# ============================================
FROM registry.cn-shanghai.aliyuncs.com/bountyteam/eclipse-temurin:21-jre-alpine

WORKDIR /app

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

# 复制宿主机构建好的 jar 包
COPY target/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
