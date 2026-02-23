# 一、docker部署方式 线上数据库连接失败
我看到了问题所在。你的 Dockerfile 使用 --spring.profiles.active=prod，加载的是 application-prod.yml 配置，其中数据库地址是 mysql7.sqlpub.com:3312。

问题原因：Docker 容器内部可能无法访问外部 MySQL 服务器，可能是以下原因之一：

1. 容器网络隔离：Docker 容器的网络配置可能阻止了对外部 MySQL 的访问
2. 防火墙限制：MySQL 服务器的防火墙可能阻止了来自容器的请求
3. DNS 解析问题：容器内无法解析域名

诊断步骤：

请在服务器上执行以下命令来诊断：

## 1. 测试容器内是否能访问 MySQL 端口
docker exec user-center-app ping -c 3 mysql7.sqlpub.com

## 2. 测试 MySQL 端口连通性
docker exec user-center-app nc -zv mysql7.sqlpub.com 3312

## 3. 查看容器网络配置
docker inspect user-center-app | grep -A 20 "Networks"

解决方案：

你需要在运行 Docker 容器时添加 --network host 参数，让容器使用宿主机网络：

### 先停止并删除旧容器
docker stop user-center-app
docker rm user-center-app

### 使用 host 网络模式重新运行
docker run -d \
--name user-center-app \
--network host \
user-center:latest
---
或者，如果你的 Docker 版本支持，可以在 Dockerfile 中直接暴露端口并使用宿主机的网络环境。

---

# docker-compose 部署步骤：
## 1. 进入项目根目录
cd /root/zjcc-user-center

## 2. 停止并删除旧容器（如果存在）
docker stop user-center-app
docker rm user-center-app

## 3. 使用 docker-compose 构建并启动
docker-compose up -d --build

## 4. 查看日志
docker-compose logs -f user-center-app

## 5. 查看容器状态
docker-compose ps

## 常用命令：
### 停止服务
docker-compose down

### 重启服务
docker-compose restart

### 查看实时日志
docker-compose logs -f

### 进入容器
docker-compose exec user-center-app sh

关键配置说明：
- network_mode: host - 容器使用宿主机网络，解决数据库 IP 白名单问题
- restart: always - 容器崩溃或重启后自动启动
- SPRING_PROFILES_ACTIVE=prod - 使用生产环境配置
- 健康检查 - 每 30 秒检查一次应用健康状态
---

# 二、Docker 容器启动失败 
内存空间不足 到 服务器的 文件描述符上限 太低，Java 启动时需要分配大量文件描述符（用于网络连接、文件读写），系统无法满足；
即使容器内存够，宿主机的文件描述符 / 内存资源被宝塔面板、其他服务占用，导致容器内进程无法申请到必要资源。
```bash
  # 常用 docker 容器管理命令：

  # 查看运行中的容器
  docker ps

  # 查看所有容器（包括已停止的）
  docker ps -a

  # 查看容器日志
  docker logs user-center-app
  docker logs -f user-center-app  # 实时查看

  # 停止容器
  docker stop user-center-app

  # 启动已停止的容器
  docker start user-center-app

  # 删除容器（必须先停止）
  docker rm user-center-app

  # 强制删除运行中的容器
  docker rm -f user-center-app
```
Docker Run 命令详细解释：

```bash
    docker run -d \
    --network=host \
    -m 1024m \
    --ulimit nofile=65535:65535 \
    -e "JAVA_OPTS=-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport" \
    --name user-center-app \
    user-app:v1
```

  ---
📦 命令结构

docker run

作用：创建并启动一个新的容器
说明：这是 Docker 最常用的命令，相当于 docker create + docker start 的组合

  ---
1️⃣ -d （detached，后台运行模式）

作用：在后台运行容器，不占用当前终端
完整写法：--detach

对比：
# 不加 -d：前台运行，占用终端，Ctrl+C 会停止容器
docker run user-app:v1

# 加 -d：后台运行，立即返回容器 ID，终端可以继续使用
docker run -d user-app:v1

使用场景：
- 服务器部署时，必须使用 -d 让容器在后台运行
- 开发调试时，可以不加 -d 来查看实时日志

  ---
2️⃣ --network=host （网络模式）

作用：容器使用宿主机的网络命名空间
完整写法：--network host

含义：
- 容器和宿主机共享网络栈
- 容器直接使用宿主机的 IP 地址和端口
- 容器内访问 localhost 等同于访问宿主机

对比其他网络模式：
# host 模式：容器使用宿主机网络（性能最好，解决 IP 白名单问题）
--network=host

# bridge 模式（默认）：容器有独立 IP，通过 NAT 访问外网
--network=bridge  # 或不指定，默认就是 bridge

# none 模式：容器没有网络
--network=none

# 自定义网络：多个容器可以互相通信
--network=my-network

优缺点：
✅ 优点：
1. 网络性能最好（没有 NAT 转换损耗）
2. 访问外部服务时，使用的是宿主机 IP（解决 MySQL IP 白名单问题）
3. 端口不需要映射，直接使用宿主机端口

❌ 缺点：
1. 容器端口和宿主机端口不能冲突
2. 失去了网络隔离的安全性
3. 多个容器无法使用相同端口

你的场景：因为 MySQL 服务器有 IP 白名单限制，必须使用 host 模式

  ---
3️⃣ -m 1024m （内存限制）

作用：限制容器最多使用 1024MB（1GB）内存
完整写法：--memory=1024m

格式说明：
-m 512m      # 限制 512MB
-m 1g        # 限制 1GB
-m 2048000000  # 限制 2048000000 字节（约 2GB）

为什么需要内存限制？
1. 防止容器占用过多内存：如果应用有内存泄漏，可能会占满宿主机内存，导致系统崩溃
2. 资源隔离：在一台服务器上运行多个容器时，合理分配内存资源
3. 触发 OOM Killer：当容器超过内存限制时，Linux 会杀死容器内的进程，保护宿主机

注意：
- 如果不设置 -m，容器可以使用宿主机的所有内存
- Java 应用需要配合 JVM 参数使用（见下面的 JAVA_OPTS）

  ---
4️⃣ --ulimit nofile=65535:65535 （文件描述符限制）

作用：设置容器内进程可以打开的最大文件描述符数量
格式：软限制:硬限制

什么是文件描述符？
在 Linux 中，一切皆文件：
- 普通文件（.txt, .jpg 等）
- 网络连接
- 管道
- 设备文件

为什么需要设置这个？
对于高并发应用（如 Java Web 应用），可能需要同时处理大量请求，每个请求都会：
- 打开数据库连接
- 打开日志文件
- 打开网络套接字

默认值问题：
- Linux 默认值通常是 1024
- 对于高并发应用，1024 远远不够
- 设置为 65535 可以支持数万个并发连接

数值含义：
--ulimit nofile=65535:65535
#                 ^^^^^^:^^^^^^
#                 软限制:硬限制

# 软限制：实际生效的限制，可以临时调高（但不能超过硬限制）
# 硬限制：软限制的上限，只有 root 用户可以提升

如何查看当前值？
# 容器内查看
docker exec user-center-app sh -c "ulimit -n"

# 宿主机查看
ulimit -n

  ---
5️⃣ -e "JAVA_OPTS=..." （环境变量）

作用：设置容器内的环境变量 JAVA_OPTS
完整写法：--env

这个参数做了什么？
-e "JAVA_OPTS=-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"

分解说明：

JAVA_OPTS

- 这是 Java 应用的标准环境变量
- 很多 Java 应用服务器（如 Tomcat）和应用会读取这个变量
- Dockerfile 中的 CMD ["java","-jar","app.jar"] 可能不会自动读取这个变量
- 注意：你的 Dockerfile 中启动命令是 java -jar app.jar，可能不会读取 JAVA_OPTS 环境变量

-XX:MaxRAMPercentage=75.0

作用：JVM 最多使用容器限制内存的 75%

配合使用：
- 你设置了 -m 1024m（容器限制 1GB 内存）
- JVM 最多使用 1024MB × 75% = 768MB

为什么要用百分比？
传统的 JVM 参数：
# 不推荐：固定堆内存大小（不配合容器内存限制）
-Xms512m -Xmx512m  # 固定使用 512MB

容器化推荐：
# 推荐：根据容器内存限制动态调整
-XX:MaxRAMPercentage=75.0  # 使用容器内存的 75%

剩余 25% 内存用途：
- 元空间
- 线程栈
- 直接内存
- 操作系统开销

-XX:+UseContainerSupport

作用：让 JVM 识别容器的内存限制（JDK 8u191+ 支持）

历史背景：
- 早期的 JVM 不感知容器，直接使用宿主机内存
- JDK 8u191 之后，JVM 开始支持容器感知
- 这个参数告诉 JVM："我在容器中运行，请遵守容器的内存限制"

版本要求：
- JDK 8u191+ 自动启用，无需指定
- 但显式指定更安全

⚠️ 重要提示：你的 Dockerfile 可能不会读取 JAVA_OPTS

你的 Dockerfile 启动命令是：
CMD ["java","-jar","app.jar","--spring.profiles.active=prod","--server.port=8080"]

这个命令不会自动读取 JAVA_OPTS 环境变量。

解决方案：

方案 1：修改 Dockerfile
# 使用 ENTRYPOINT + JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod --server.port=8080"]

方案 2：直接在 docker run 中指定 JVM 参数
docker run -d \
--network=host \
-m 1024m \
--ulimit nofile=65535:65535 \
--name user-center-app \
user-app:v1 \
java -XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -jar /app/app.jar --spring.profiles.active=prod

  ---
6️⃣ --name user-center-app （容器名称）

作用：给容器指定一个固定的名称

为什么需要名称？
# 有名称：方便管理
docker logs user-center-app        # 查看日志
docker stop user-center-app        # 停止容器
docker exec user-center-app sh     # 进入容器

# 无名称：使用随机生成的 ID（不方便）
docker logs 6470713b2bb2           # 难记

命名规则：
- 只能包含：字母、数字、下划线、点、横线
- 不能重复（如果名称已存在，需要先删除旧容器）

  ---
7️⃣ user-app:v1 （镜像名称和标签）

作用：指定要运行的镜像

格式：镜像名:标签

示例：
user-app:v1           # 镜像名: user-app, 标签: v1
user-app:latest       # 默认标签
mysql:8.0             # MySQL 8.0 版本
nginx:alpine          # Nginx 的 Alpine 版本（更小）

标签的作用：
- 版本管理（v1, v2, v3...）
- 环境区分
- 变体区分（alpine, slim, full...）

如果不指定标签：
docker run user-app   # 默认使用 :latest 标签

  ---
📊 完整对比表

| 参数                        | 作用           | 不使用会怎样                              |
  |-----------------------------|----------------|-------------------------------------------|
| -d                          | 后台运行       | 占用终端，Ctrl+C 停止容器                 |
| --network=host              | 使用宿主机网络 | 容器有独立 IP，可能被 MySQL IP 白名单拒绝 |
| -m 1024m                    | 限制内存 1GB   | 容器可能占满宿主机内存                    |
| --ulimit nofile=65535:65535 | 限制文件描述符 | 高并发时可能报 "Too many open files"      |
| -e "JAVA_OPTS=..."          | 设置 JVM 参数  | ⚠️ 可能不生效（取决于 Dockerfile）        |
| --name user-center-app      | 容器命名       | 随机生成名称，不方便管理                  |
| user-app:v1                 | 指定镜像       | 必须指定，否则不知道运行什么              |

  ---
🎯 推荐的完整命令

基于你的场景，推荐命令：
```bash
docker run -d \
--network=host \
-m 1024m \
--ulimit nofile=65535:65535 \
--name user-center-app \
user-app:v1 \
java -XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -jar /app/app.jar --spring.profiles.active=prod --server.port=8080
```

或者使用 docker-compose（更推荐）：
docker-compose up -d --build

● 需要我帮你修改 Dockerfile 来支持 JAVA_OPTS 环境变量吗？或者帮你使用 docker-compose 来部署？

本项目实际使用docker 启动命令
```bash
docker run -d --network=host -m 1024m --ulimit nofile=65535:65535 --name user-center-app user-app:v1
```
