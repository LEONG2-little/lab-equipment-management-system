# 使用 JDK 17 基础镜像
FROM openjdk:17-jdk-slim

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 设置工作目录
WORKDIR /app

# 创建图片上传目录
RUN mkdir -p /app/images /app/faultImages

# 复制 jar 包（先确保执行过 mvn package）
COPY target/LaboratoryBackend-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 使用 docker 配置文件启动
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]