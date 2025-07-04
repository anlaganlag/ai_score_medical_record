# 使用官方OpenJDK 17运行时作为基础镜像
FROM openjdk:17-jre-slim

# 设置工作目录
WORKDIR /app

# 安装curl用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制JAR文件到容器中
COPY target/ai-medical-score-1.0.0-SNAPSHOT.jar app.jar

# 创建logs目录
RUN mkdir -p logs

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:+PrintGCDetails"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 