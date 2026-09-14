# 使用OpenJDK镜像作为基础镜像
FROM openjdk:17-jdk-slim

# 设置维护者信息（可选）
LABEL maintainer="holic512@163.com"

WORKDIR /app

# 将Spring Boot Jar包复制到容器中
ARG JAR_FILE=target/backend-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Mount this path to retain SQLite data and user uploads across container replacement.
VOLUME ["/app/file"]

# 暴露端口（与Spring Boot的server.port配置一致）
EXPOSE 8080

# 启动Spring Boot应用
ENTRYPOINT ["java", "-jar", "/app.jar"]
