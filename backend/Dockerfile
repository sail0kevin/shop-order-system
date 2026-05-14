# ================================================
# Dockerfile —— 多阶段构建
# 阶段1：编译源码
# 阶段2：运行编译后的 JAR
# ================================================

# ---------- 阶段1：编译 ----------
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build

# 先复制 pom.xml 和 settings 下载依赖（利用 Docker 缓存层）
COPY pom.xml ./
COPY src ./src

# 编译打包（跳过测试，避免 Docker 构建时还需要数据库）
RUN --mount=type=cache,target=/root/.m2 \
    javac -version && \
    apk add --no-cache maven && \
    mvn package -DskipTests -q

# ---------- 阶段2：运行 ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 从 builder 阶段复制 JAR
COPY --from=builder /build/target/*.jar app.jar

# 暴露 8080 端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
