# ====================== 构建阶段（builder）======================
# 阶段1：基于maven镜像，用于编译打包（构建阶段）
#命名为builder，典型构建阶段
FROM maven:3.5-jdk-8-alpine as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests  # 执行打包 核心构建操作
# 至此，构建阶段生成了 /app/target/*.jar 产物

# ====================== 运行阶段（最终镜像）======================
# 阶段2：基于轻量JRE镜像，仅用于运行（运行阶段）
# 无命名，是最终的运行阶段
FROM openjdk:8-jre-alpine
WORKDIR /app
# 从构建阶段复制产物到运行阶段（核心：只拿产物，不要构建工具）
COPY --from=builder /app/target/user-center-0.0.1-SNAPSHOT.jar ./app.jar
# 定义启动命令，核心运行操作
CMD ["java","-jar","app.jar","--spring.profiles.active=prod","--server.port=8080"]
# 还可以加上 "java -Xms256m -Xmx512m" 限制 Java 堆内存的大小，防止其无限制地申请内存