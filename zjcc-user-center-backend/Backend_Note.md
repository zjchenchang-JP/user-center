# 2026/01/12
## 日志 log
```java
if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
    log.warn("账户密码不能为空！");
    // TODO 修改为全局自定义异常
    throw new BusinessException(ErrorCode.PARAMS_ERROR);
}
```
`log.warn` 在已经有 `ErrorCode.PARAMS_ERROR` 返回错误信息的情况下是否还有必要，答案是：**这句日志不仅有必要，而且在生产环境中是不可或缺的**。日志的意义和价值并不等同于返回给前端的错误信息，二者各司其职、互为补充，核心价值体现在以下几个方面：

### 一、日志的核心价值：服务于后端排查问题（与前端错误信息的定位不同）
返回给前端的 `ErrorCode.PARAMS_ERROR` 对应的错误信息（如“请求参数错误”），核心目的是**给前端/用户一个简洁、易懂的提示**，无需暴露后端具体的业务细节和排查线索；而 `log.warn` 打印的日志，核心目的是**给后端开发、测试、运维人员提供排查问题的依据**，解决“线上出现错误，如何快速找到根因”的问题。

二者的核心区别可以用表格清晰区分：
| 内容                | 目标人群       | 核心目的                     | 信息粒度       |
|---------------------|----------------|------------------------------|----------------|
| `ErrorCode` 错误信息 | 前端开发、终端用户 | 告知“操作失败”，提供简洁提示 | 粗粒度、易懂   |
| `log.warn` 后端日志  | 后端开发、运维人员 | 排查“为何失败”，定位问题根因 | 细粒度、详尽   |

### 二、日志的具体实用意义（落地场景）
1.  **线上问题追溯：快速定位“什么时候、什么场景出现了这个错误”**
    线上环境中，前端反馈“注册失败，提示参数错误”，但前端无法提供更详细的上下文。此时后端人员可以通过查询日志中 `log.warn` 打印的内容，获取以下关键信息：
- 错误发生的**时间戳**（精确到毫秒），便于关联其他相关日志（如请求日志、数据库日志）；
- 错误对应的**具体场景**（日志明确打印“用户账户、密码、确认密码、星球编号不能为空”，直接缩小排查范围，无需去阅读代码判断参数校验逻辑）；
- 结合日志中的**请求ID/用户信息**（可扩展添加），还能定位到具体是哪个用户、哪个请求出现了该错误，快速复现场景。

如果没有这句日志，后端人员只能通过前端反馈的“请求参数错误”去猜测可能的原因，效率极低，甚至无法定位问题。

2.  **辅助线上监控与告警：实现错误的量化和预警**
    生产环境中，通常会部署日志收集系统（如 ELK、Prometheus + Grafana），这些系统可以对日志进行解析、统计和监控：
- 可以统计该 `warn` 日志的**出现频率**（如1分钟内出现100次），判断是否是批量问题（如前端版本迭代后，注册参数传递遗漏）；
- 可以基于日志频率配置**告警规则**（如5分钟内出现超过50次，触发钉钉/邮件告警），实现问题的早发现、早处理，避免影响更多用户。

而返回给前端的错误信息，无法被后端监控系统捕获和统计，无法实现这种批量问题的预警。

3.  **日志具有不可替代性：前端错误信息无法覆盖后端排查需求**
    你可能会疑惑“ErrorCode 已经包含了错误信息，为什么还要重复打印日志”，但实际上二者无法互相替代，原因有二：
-  **信息传递链路不同**：前端错误信息是通过 HTTP 响应返回给前端，不会留存到后端服务器，一旦前端没有反馈，后端无法知晓该错误发生过；而日志会持久化存储在后端服务器（或日志平台），即使没有前端反馈，也能通过日志审计发现潜在问题。
-  **信息安全要求不同**：前端错误信息需要遵循“最小必要原则”，不能暴露后端业务逻辑和敏感信息（如不能返回“数据库查询用户账户重复”，避免泄露数据库结构）；而后端日志可以包含更详尽的业务细节，且仅对内可见，不存在安全泄露风险。

### 三、优化建议：让日志更具排查价值（补充上下文信息）
原日志已经具备核心价值，还可以进一步优化，添加具体的参数信息，让排查更高效（避免只知道“参数为空”，却不知道具体哪个参数为空）：
```java
// 优化前
log.warn("用户账户、密码、确认密码、星球编号不能为空");

// 优化后：补充具体参数值，快速定位哪个参数为空
log.warn("用户注册参数非空校验失败，账户：{}，密码：{}，确认密码：{}，星球编号：{}", 
         userAccount, userPassword, checkPassword, planetCode);
```

这样一来，当日志打印后，后端人员可以直接看到具体的参数值，比如发现 `userAccount` 为 `null`、`planetCode` 为空串，无需再去复现场景，直接定位问题根因。

### 总结
1.  `log.warn` 日志与前端错误信息**各司其职**，前者服务于后端排查，后者服务于前端提示，二者缺一不可；
2.  日志的核心价值是**问题追溯、监控告警、审计留存**，这些都是前端错误信息无法实现的；
3.  优化日志的关键是**补充上下文信息**，让日志更具排查价值，提高线上问题的定位效率。

## 日志级别 对应场景
为什么这里使用 `warn` 日志级别而非 `error`，核心原因是：**日志级别是根据「问题的严重程度」和「是否影响系统正常运行」来划分的，用户注册参数校验失败属于「业务异常」，而非「系统异常」，用 `warn` 更符合日志级别的设计规范和实际使用场景**。

下面我们详细拆解二者的区别及选择 `warn` 的核心原因：

### 一、先明确 `warn` 和 `error` 级别的核心定义（行业通用规范）
日志级别（从低到高：`trace` < `debug` < `info` < `warn` < `error` < `fatal`）的核心划分逻辑，是「是否影响系统本身的运行」和「是否需要运维/开发人员紧急介入」：
1.  **`error` 级别**：用于记录「**系统级异常/错误**」，这类问题会**导致系统功能无法正常运行、流程中断，甚至影响后续请求的处理**，通常需要开发/运维人员及时介入排查和修复。
    - 典型场景：数据库连接失败、NPE 空指针异常（非业务预期）、接口调用超时（第三方服务不可用）、SQL 语法错误、文件读写失败等。
    - 举例：用户注册时，插入数据到数据库时出现「数据库连接池耗尽」，此时需要记录 `error`，因为这是系统层面的故障，会导致所有后续注册请求都失败，需要紧急修复。
2.  **`warn` 级别**：用于记录「**业务级异常/预期内的异常**」，这类问题是「系统运行正常，只是业务流程不符合预期（由用户操作或合法参数问题导致）」，不会影响系统本身的稳定性，也不需要紧急介入，仅需留存记录供后续审计/分析。
    - 典型场景：用户参数校验失败、登录密码错误、账户已存在、权限不足（用户无该操作权限）等。
    - 举例：本次用户注册的「参数为空」「账户包含特殊字符」，都是系统提前预判到的业务场景，系统本身运行正常，只是该用户的本次请求无法继续执行，属于预期内的失败，用 `warn` 更合适。

### 二、选择 `warn` 而非 `error` 的3个核心原因
1.  **问题性质不同：业务预期内异常 vs 系统非预期异常**
    用户注册参数校验失败，是「系统提前设计并预判到的场景」（通过 `if` 判断、注解校验等方式拦截），属于「业务逻辑上的失败」，而非「系统本身的故障」。
    - 系统依然可以正常接收其他用户的注册请求，不会因为这个校验失败而停止运行，也不会影响系统的其他功能（如登录、查询）。
    - 而 `error` 级别对应的是「系统本身出了问题，超出了业务预期」，比如突然无法连接数据库，这种情况是系统无法预判的，会影响整体服务，才需要用 `error`。

2.  **避免「告警噪音」，提高关键问题的响应效率**
    生产环境中，`error` 级别日志通常会配置「实时告警」（如钉钉、邮件、短信），用于提醒运维/开发人员紧急处理严重问题。
    - 如果将「用户参数校验失败」这类高频次、非紧急的业务异常记录为 `error`，会导致告警系统被大量无效信息淹没（比如有恶意用户反复输入错误参数，会产生大量 `error` 日志），形成「告警噪音」。
    - 这种情况下，当真正出现「数据库连接失败」等致命 `error` 时，告警信息可能会被淹没在大量无效告警中，无法被及时发现和处理，造成更严重的损失。
    - 而 `warn` 级别日志通常不会配置紧急告警，仅用于日志统计和后续分析（如统计每日注册失败的原因分布），既留存了必要信息，又不会干扰关键问题的响应。

3.  **符合日志分级的设计初衷，便于日志筛选和分析**
    日志分级的核心目的之一，是「便于后续按级别筛选、统计和分析」：
    - 开发/运维人员排查系统故障时，通常会优先筛选 `error` 级别日志，快速定位系统层面的问题。
    - 如果将业务异常和系统异常都混在 `error` 中，会增加排查成本（需要从大量无效日志中筛选出真正的系统故障）。
    - 而将业务异常放在 `warn` 中，系统故障放在 `error` 中，边界清晰，便于后续通过日志平台（如 ELK）进行分类统计和分析（比如：统计 `error` 日志的出现频率，判断系统稳定性；统计 `warn` 日志的分布，优化业务流程和前端提示）。

### 三、补充：日志级别选择的实操原则（避免踩坑）
1.  凡是「用户操作导致的、预期内的业务失败」，优先用 `warn`；
2.  凡是「系统自身故障导致的、非预期的流程中断」，优先用 `error`；
3.  避免「滥用 `error`」，不要把所有异常都记录为 `error`，否则会失去日志级别的区分价值；
4.  避免「滥用 `warn`」，如果业务异常会影响核心流程（如支付失败），即使是预期内的，也可以根据实际场景记录 `warn` 并配置非紧急告警（如每日汇总统计）。

### 总结
1.  `warn` 用于记录「业务预期内异常，不影响系统运行」，`error` 用于记录「系统非预期异常，影响系统运行」；
2.  用户注册参数校验失败是业务异常，用 `warn` 可避免告警噪音、便于日志筛选，符合行业规范；
3.  日志级别的选择核心，是「匹配问题的严重程度」，而非「只要有错误就用 `error`」。

# 2026/01/20
## 后端解决跨域问题四种方式
CorsFilter / WebMvcConfigurer / @CrossOrigin 需要SpringMVC 4.2 以上的版本才支持，对应SpringBoot 1.3 版本以上都支持这些CORS特性。
不过，使用SpringMVC4.2 以下版本，直接使用方式4通过手工添加响应头来授权CORS跨域访问也是可以的。
### 方式1：返回新的CorsFilter
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域资源共享(CORS)配置类
 * 同域名 / 端口 / 协议  => 有一个不同就是跨域
 * 用于解决前后端分离架构中，浏览器的同源策略限制导致的跨域请求问题
 */
@Configuration  // 标记该类为Spring的配置类，会被Spring容器扫描并加载
public class CorsConfig {

    /**
     * 构建CORS配置信息对象
     * 封装所有跨域相关的配置规则
     * @return 配置完成的CorsConfiguration对象
     */
    private CorsConfiguration buildConfig() {
        // 创建CORS配置对象，用于存储跨域配置规则
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 允许所有来源的跨域请求（生产环境建议指定具体域名，如https://your-frontend.com）
        // * 表示通配符，允许任何域名访问
        corsConfiguration.addAllowedOrigin("*");

        // 允许所有请求头（如Content-Type、Authorization等）
        corsConfiguration.addAllowedHeader("*");

        // 允许所有HTTP请求方法（GET、POST、PUT、DELETE、OPTIONS等）
        corsConfiguration.addAllowedMethod("*");

        // 设置预检请求（OPTIONS）的缓存时间，单位秒
        // 3600秒内同一请求不需要重复发送预检请求，提升性能
        corsConfiguration.setMaxAge(3600L);

        // 允许跨域请求携带Cookie等凭证信息
        // 注意：如果设置为true，addAllowedOrigin不能使用*，必须指定具体域名
        // 生产环境中需要将 addAllowedOrigin("*") 改为具体的前端域名
        // 例如：corsConfiguration.addAllowedOrigin("https://www.example.com")；如果需要允许多个域名，可以多次调用 addAllowedOrigin 方法
        // 允许 example.com 下的所有子域名
        // corsConfiguration.addAllowedOriginPattern("https://*.example.com");
        // 或者 corsConfiguration.addAllowedOriginPattern("*"); // 支持通配符且兼容allowCredentials=true
        corsConfiguration.setAllowCredentials(true);

        // 返回配置好的CORS配置对象
        return corsConfiguration;
    }

    /**
     * 注册CORS过滤器Bean
     * Spring会自动将该过滤器加入到请求处理链中
     * @return CorsFilter 跨域过滤器
     */
    @Bean  // 将方法返回的对象注册为Spring容器中的Bean
    public CorsFilter corsFilter() {
        // 创建基于URL的CORS配置源对象
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 为所有URL路径（/**）注册上面构建的CORS配置
        // /** 表示匹配应用中所有的请求路径
        source.registerCorsConfiguration("/**", buildConfig());

        // 创建并返回CORS过滤器，传入配置源
        return new CorsFilter(source);
    }
}
```
>/*	匹配当前层级的所有路径（仅一层），无法匹配子层级	/user、/order、/api	/user/123、/api/v1/login

>/** 匹配所有层级的所有路径（任意层），包括当前层和所有子层级	/user、/user/123、/api/v1
### 方式2：重写WebMvcConfigurer
```java

@Configuration
public class WebMvcConfg implements WebMvcConfigurer {
 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                //当**Credentials为true时，**Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
                .allowedOrigins("http://localhost:9527", "http://127.0.0.1:9527", "http://127.0.0.1:8082", "http://127.0.0.1:8083")
                //是否允许证书 不再默认开启
                .allowCredentials(true)
                //设置允许的方法
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }
}
```
### 方式3：使用注解（@CrossOrigin）
```java
@Controller
@RequestMapping("/admin/sysLog")
// @CrossOrigin 什么参数也不写 = 默认配置：允许所有源、所有请求方法、所有请求头，预检缓存30分钟
// 等价于下面
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {}, maxAge = 1800)
public class SysLogController {
}
```
```java
// 仅允许前端域名访问，允许携带凭证，限定请求方法
@CrossOrigin(
    originPatterns = {"http://localhost:8080", "https://*.example.com"}, // 支持通配符
    allowCredentials = true, // 允许携带Cookie
    methods = {RequestMethod.GET, RequestMethod.POST}, // 仅允许GET/POST
    maxAge = 3600 // 预检缓存1小时
)
```
### 方式4：手工设置响应头（HttpServletResponse ）
这种方式，可以自己手工加到具体的controller，inteceptor，filter等逻辑里
```java
@RequestMapping("/test")
@ResponseBody
public String test(){
    response.addHeader("Access-Control-Allow-Origin", "http://localhost:8080");
    return "success";
}
```

# 2026/02/22
## 📋 打包问题总结

🔴 问题原因

pom.xml:93 配置了 <skip>true</skip>，导致 Spring Boot Maven Plugin 跳过打包，生成的 jar 包不完整，无法运行。

  ---
✅ 解决步骤

步骤 1：修改 pom.xml

删除第 93 行的 <skip>true</skip>：
```java
<plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <version>${spring-boot.version}</version>
      <configuration>
          <mainClass>com.zjcc.usercenter.ZjccUserCenterBackendApplication</mainClass>
          <!-- ❌ 删除这行：<skip>true</skip> -->
      </configuration>
      <executions>
          <execution>
              <id>repackage</id>
              <goals>
                  <goal>repackage</goal>
              </goals>
          </execution>
      </executions>
  </plugin>
```
  ---
步骤 2：重新打包

# 进入后端目录
cd E:\development\Java\codefather-projects\user-center\zjcc-user-center\zjcc-user-center-backend

# 清理并重新打包（跳过测试，加快速度）
mvn clean package -DskipTests

  ---
步骤 3：运行 jar 包

# 进入 target 目录
cd target

# 运行开发环境
java -jar user-center-0.0.1-SNAPSHOT.jar

# 或者运行生产环境
java -jar user-center-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# 或者指定编码（解决中文乱码）
java -Dfile.encoding=UTF-8 -jar user-center-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

  ---
📝 常用命令清单

| 命令                                            | 作用                          |
  |-------------------------------------------------|-------------------------------|
| mvn clean package -DskipTests                   | 清理并打包（跳过测试）        |
| mvn clean package                               | 清理并打包（包含测试）        |
| java -jar xxx.jar                               | 运行 jar 包（开发环境）       |
| java -jar xxx.jar --spring.profiles.active=prod | 运行 jar 包（生产环境）       |
| netstat -ano | findstr :8080                    | 检查 8080 端口占用            |
| jps -l                                          | 查看运行中的 Java 进程        |
| chcp 65001                                      | 修改 Windows 终端编码为 UTF-8 |

  ---
⚠️ 注意事项

1. 每次修改 pom.xml 后必须重新打包
2. 生产环境会使用 application-prod.yml 配置（远程数据库）
3. 开发环境使用 application.yml 配置（本地数据库）
4. 中文乱码问题：使用 -Dfile.encoding=UTF-8 参数

  ---
修改完 pom.xml 后，重新执行步骤 2 和 3 即可！

  ---
# 云服务器部署SpringBoot项目端口冲突解决方案总结
## 核心思路
部署在云服务器上的SpringBoot项目遇到8080端口被占用时，**无需修改源代码/重新打包**，核心采用「运行时动态指定端口」的方式解决，兼顾灵活性与企业级部署规范。
## 一、快速解决端口冲突（核心命令）
### 1. 命令行参数指定端口（首选，简单直观）
启动时直接通过`--server.port`指定未被占用的端口，同时可搭配生产环境激活（`spring.profiles.active=prod`）：
```bash
# 基础版：仅指定端口
java -jar 你的项目包名.jar --server.port=8081

# 综合版：指定端口 + 激活prod生产环境
java -jar 你的项目包名.jar --server.port=8081 --spring.profiles.active=prod

# 后台运行版（云服务器常用，避免终端关闭服务停止）
nohup java -jar 你的项目包名.jar --server.port=8081 --spring.profiles.active=prod > app.log 2>&1 &
```

### 2. 环境变量传递端口（适配容器/自动化部署）
通过系统环境变量传递端口，无需修改命令结构，符合Docker/K8s等容器化部署规范：
```bash
# Linux/Mac系统
SERVER_PORT=8081 SPRING_PROFILES_ACTIVE=prod java -jar 你的项目包名.jar

# Windows服务器（cmd）
set SERVER_PORT=8081 && set SPRING_PROFILES_ACTIVE=prod && java -jar 你的项目包名.jar

# Windows服务器（PowerShell）
$env:SERVER_PORT=8081; $env:SPRING_PROFILES_ACTIVE="prod"; java -jar 你的项目包名.jar
```

### 3. 外部配置文件指定端口（适合多配置场景）
若生产环境有大量配置（如数据库、Redis），可通过外部配置文件固定端口，启动时加载：
```bash
# 步骤1：服务器新建配置文件（如/opt/config/application-prod.properties）
echo "server.port=8081" > /opt/config/application-prod.properties

# 步骤2：启动时加载外部配置 + 激活prod环境
java -jar 你的项目包名.jar --spring.config.location=/opt/config/application-prod.properties --spring.profiles.active=prod
```

## 二、企业级长效解决方案（避免反复端口冲突）
### 1. 自动检查端口的启动脚本（推荐）
创建`start.sh`脚本，自动检测端口占用并切换可用端口启动，一键解决冲突：
```bash
#!/bin/bash
# 配置项
JAR_NAME="你的项目包名.jar"       # 替换为实际jar包名
BASE_PORT=8080                   # 基础端口
LOG_FILE="user-center.log"       # 日志文件

# 自动检测可用端口
PORT=$BASE_PORT
while lsof -i:$PORT >/dev/null; do
    echo "端口 $PORT 被占用，尝试端口 $((PORT+1))..."
    PORT=$((PORT+1))
done

# 后台启动服务（激活prod环境 + 指定可用端口）
echo "开始启动项目，端口：$PORT，环境：prod..."
nohup java -jar $JAR_NAME --server.port=$PORT --spring.profiles.active=prod > $LOG_FILE 2>&1 &

# 输出启动信息
echo "项目启动成功！PID：$!"
echo "日志文件：$LOG_FILE（可执行 tail -f $LOG_FILE 查看）"
echo "访问地址：http://服务器IP:$PORT"
```
**使用步骤**：
```bash
chmod +x start.sh  # 赋予执行权限
./start.sh         # 一键启动
```

### 2. 端口管理规范
- 为每个SpringBoot项目分配**专属固定端口**（如用户中心8085、订单服务8086），记录在服务器文档中；
- 容器化部署时，通过Docker/K8s的端口映射（如`-p 8081:8080`）隔离容器内端口，避免宿主机端口冲突。

## 三、关键知识点补充
### 1. 配置优先级（冲突时以此为准）
命令行参数（`--server.port`） > 环境变量（`SERVER_PORT`） > 外部配置文件 > jar内置配置。

### 2. 命令行参数 vs 环境变量区别
| 方式                | 核心优势                  | 适用场景                          |
|---------------------|---------------------------|-----------------------------------|
| `--server.port=8081` | 直观、优先级最高、无依赖  | 传统服务器部署、临时调整端口      |
| `SERVER_PORT=8081`   | 适配容器化、批量传配置    | Docker/K8s部署、自动化脚本部署    |

## 四、总结关键点
1. 解决端口冲突的核心是「运行时动态指定端口」，拒绝修改源代码重新打包；
2. 快速解决用`java -jar 包名.jar --server.port=新端口`，长效解决用自动检查端口的启动脚本；
3. 容器化部署优先用环境变量传递端口，传统部署优先用命令行参数/外部配置文件。

## 两个Cookie 对比 Cookie和Session
1. 浏览器端 Cookie（JSESSIONID）

- 显示"会话" = Session Cookie
- 没有设置 Max-Age 或 Expires 属性
- 浏览器关闭时自动删除
- 这就是为什么你看到过期时间是"会话"

2. 服务器端 Session

- 在 application.yml 中配置了 timeout: 86400
- 服务器在内存中保存 1 天
- 1天后服务器会销毁这个 Session

  ---
两者之间的关系

┌─────────────────────────────────────────────────────┐
│  浏览器 Cookie (JSESSIONID)                          │
│  过期时间：浏览器关闭时删除                           │
├─────────────────────────────────────────────────────┤
│  服务器 Session (USER_LOGIN_STATE)                  │
│  过期时间：86400秒（1天）后删除                      │
└─────────────────────────────────────────────────────┘

场景分析

场景1：正常使用（1天内不关闭浏览器）
- ✅ Cookie 和 Session 都存在
- ✅ 用户保持登录状态

场景2：1天内关闭浏览器再重新打开
- ❌ 浏览器删除了 JSESSIONID
- ✅ 服务器 Session 可能还存在（但找不到对应的 Cookie 了）
- 结果：需要重新登录

场景3：超过1天不关闭浏览器
- ✅ JSESSIONID Cookie 还在（浏览器没关闭）
- ❌ 服务器 Session 已经过期被删除
- 结果：需要重新登录（Cookie 有，但服务器找不到对应 Session）

  ---
如何验证服务器 Session 的过期时间？

方法1：通过代码查看 Session 配置

在代码中临时添加一个接口查看 Session 信息：

```java
    @GetMapping("/session-info")
    public BaseResponse<Map<String, Object>> getSessionInfo(HttpServletRequest request) {
    HttpSession session = request.getSession();
    Map<String, Object> info = new HashMap<>();
    info.put("sessionId", session.getId());
    info.put("maxInactiveInterval", session.getMaxInactiveInterval()); // 秒
    info.put("creationTime", new Date(session.getCreationTime()));
    info.put("lastAccessedTime", new Date(session.getLastAccessedTime()));
    info.put("attributeNames", Collections.list(session.getAttributeNames()));
    return ResponseResult.ok(info);
    }

访问后会看到：
    {
    "sessionId": "a5279e8ab73b5bdfe2e4ae90a6eb0b0f",
    "maxInactiveInterval": 86400,  // ← 这就是配置的过期时间（秒）
    "creationTime": "2026-02-23T...",
    "lastAccessedTime": "2026-02-23T...",
    "attributeNames": ["USER_LOGIN_STATE"]
    }
```

方法2：修改 application.yml，让 Cookie 也有过期时间

如果你想让 Cookie 和 Session 的过期时间保持一致，取消注释你配置文件中的这段：

```yml
    server:
        servlet:
            session:
                cookie:
                max-age: 86400  # Cookie 也设置1天过期
                http-only: true
                secure: false   # 如果用 HTTPS 改为 true
```
```yaml    
# 考虑到用户体验，建议：
    server:
      servlet:
        session:
          timeout: 86400  # Session 1天过期
          cookie:
            max-age: 86400      # Cookie 也1天过期
            http-only: true     # 防止 XSS 窃取
            secure: false       # HTTP 环境设为 false
            same-site: lax      # 防止 CSRF 攻击
```
这样 JSESSIONID 也会显示具体的过期时间，而不是"会话"。

>
```yaml
配置 servlet.cookie.max-age

行为：
- JSESSIONID 显示具体过期时间（1天后）
- 浏览器关闭后，Cookie 仍然保留
- 用户关闭浏览器后再打开，如果未超过 1 天，仍然保持登录

优点：
- ✅ 用户体验好（不用频繁登录）
- ✅ Cookie 和 Session 过期时间一致

缺点：
- ❌ 安全性略低（公共设备上有风险）

  ---
我的建议

对于你的用户中心项目：

不需要配置 servlet，原因：

1. 你的应用特点：用户中心需要频繁登录/注销，Session Cookie 更安全
2. 已经配置了 Session timeout：服务器端 1 天过期已经足够（防止无限占用内存）
3. 简单即是好：当前配置已经满足需求

当前配置已经足够：
spring:
session:
timeout: 86400  # ← 这个就够了

  ---
什么时候需要配置 servlet？

- 电商网站（希望用户保持购物车）
- 后台管理系统（内部使用，长期保持登录）
- 移动端应用（没有"关闭浏览器"的概念）

总结：你的项目当前配置已经合理，不需要额外配置 servlet。
```

---

# 2026/05/01
## CompletableFuture 多线程并发批量插入详解

### 完整代码示例

```java
@Test
public void doConcurrencyInsertUser() {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    final int INSERT_NUM = 100000;      // 总共插入10万条
    final int BATCH_SIZE = 5000;        // 每批5000条
    ArrayList<CompletableFuture<Void>> futureList = new ArrayList<>();

    // 循环次数 = 100000 / 5000 = 20 次
    for (int i = 0; i < INSERT_NUM / BATCH_SIZE; i++) {
        ArrayList<User> users = new ArrayList<>();

        // 每批生成 5000 条数据
        for (int j = 0; j < BATCH_SIZE; j++) {
            User user = new User();
            user.setUsername("假数据");
            user.setUserAccount("yusha");
            user.setPlanetCode("23322");
            // ... 其他字段
            users.add(user);
        }

        // 异步执行批量插入
        int batchNum = i;
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("批次 " + batchNum + "，线程：" + Thread.currentThread().getName());
            userService.saveBatch(users, BATCH_SIZE);
        }, executorService);
        futureList.add(future);
    }

    // 等待所有异步任务完成
    CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

    stopWatch.stop();
    System.out.println("总耗时：" + stopWatch.getLastTaskTimeMillis() + "ms");
}
```

### 核心概念：把"任务"和"结果"分开

```
┌─────────────────────────────────────────────────────────────┐
│  CompletableFuture = 任务（做什么）+ Future（什么时候完成）  │
└─────────────────────────────────────────────────────────────┘
```

### 工作流程详解

#### 第 1 步：创建异步任务（准备干活）

```java
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    // 这里的代码会在新线程中执行
    userService.saveBatch(users, BATCH_SIZE);
}, executorService);
```

**拆解：**

| 部分 | 作用 | 类比 |
|------|------|------|
| `CompletableFuture.runAsync()` | 启动一个异步任务 | 喊一声"去干活了"，不等人做完 |
| `() -> { ... }` | Lambda 表达式，实际要执行的代码 | 具体的活儿：插入5000条数据 |
| `executorService` | 线程池，提供工作线程 | 工人团队（16个人） |
| `CompletableFuture<Void>` | 返回值：Future 表示"这个任务的承诺" | 一张"取货单"，以后可以查进度 |

**为什么又 Runnable 又 Future？**

```
CompletableFuture.runAsync(Runnable 任务) 返回 CompletableFuture
                    ↓                        ↓
              "做什么事"              "什么时候做完"
```

- `Runnable` 部分：告诉它要做什么（插入数据）
- `Future` 部分：给你一个"凭证"，可以查状态、等待完成

#### 第 2 步：收集所有任务的"凭证"

```java
futureList.add(future);  // 把凭证保存起来
```

循环 20 次，`futureList` 里就有 20 张"凭证"。

#### 第 3 步：等待所有任务完成

```java
CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
```

**拆解：**

| 方法 | 作用 |
|------|------|
| `allOf(...)` | 把多个 Future 合并成一个大 Future |
| `.join()` | **阻塞等待**，直到所有任务都完成 |

**类比：**
- 你点了 20 份外卖（同时下单）
- `allOf` 拿到 20 张订单号
- `join()` 就坐在门口等，直到 20 份都送到

### 完整流程图

```
主线程
  │
  ├─ 循环第 1 次
  │   └─ 创建任务1 → 提交给线程池 → 返回 future1 ─────┐
  │                                                  │
  ├─ 循环第 2 次                                    │
  │   └─ 创建任务2 → 提交给线程池 → 返回 future2 ─────┤
  │                                                  │
  ├─ 循环第 3 次                                    │ 线程池（16个工人）
  │   └─ 创建任务3 → 提交给线程池 → 返回 future3 ─────┤   同时干活
  │   ...                                           │
  │                                                  │
  ├─ 循环第 20 次                                   │
  │   └─ 创建任务20 → 提交给线程池 → 返回 future20 ────┘
  │
  └─ allOf(future1, future2, ..., future20).join()
      │
      └─ 主线程阻塞，等待所有任务完成
          │
          └─ 当所有任务都完成 → 继续执行下面的代码
```

### 为什么用 CompletableFuture 而不是直接 execute？

| 方式 | 能否等待完成 | 能否获取结果 | 能否组合任务 |
|------|-------------|-------------|-------------|
| `executorService.execute(Runnable)` | ❌ | ❌ | ❌ |
| `executorService.submit(Runnable)` | ✅ 通过 Future.get() | ❌ | ❌ |
| `CompletableFuture.runAsync()` | ✅ 通过 .join() | ❌ | ✅ 可链式调用 |

**场景需要"等待所有任务完成再计时"，所以用 CompletableFuture。**

### 线程池配置

```java
private ExecutorService executorService = new ThreadPoolExecutor(
    16,                          // 核心线程数
    100,                         // 最大线程数
    10000,                       // 空闲线程存活时间
    TimeUnit.MINUTES,            // 时间单位
    new ArrayBlockingQueue<>(1000),  // 任务队列
    new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
);
```

**参数说明：**
- **核心线程 16**：常驻线程，立即处理任务
- **最大线程 100**：任务多时扩展到 100 个线程
- **队列容量 1000**：最多缓存 1000 个等待任务
- **CallerRunsPolicy**：队列满了由调用线程（主线程）执行

### 性能对比

| 方式 | 1000条数据 | 10万条数据 | 说明 |
|------|-----------|-----------|------|
| 循环单条插入 | ~2000ms | ~200000ms | 每次一条，慢 |
| 批量插入 | ~971ms | ~26830ms | 20个批次并行 |
| 多线程批量 | N/A | ~26830ms | 使用 CompletableFuture |

### 一句话总结
> `CompletableFuture` = 把任务扔给线程池去做 + 给你一个凭证可以等它做完

---

# 2026/05/03
## @Transactional 事务管理详解

### 一、@Transactional 注意事项

| 问题 | 说明 | 本代码影响 |
|------|------|-----------|
| **默认传播行为** | `REQUIRED` 会加入现有事务，如果外层也有事务可能产生意外回滚 | ⚠️ 需注意 |
| **异常类型** | 你已正确设置 `rollbackFor = Exception.class` | ✅ 正确 |
| **仅限本类调用** | 如果被 `this.addTeam()` 内部调用，事务不会生效 | ⚠️ 需注意 |
| **final/private/static** | 这些修饰符的方法无法被代理，事务不生效 | ❌ 无影响（public方法） |

### 二、事务失效的常见场景

| 场景 | 原因 | 是否影响本代码 |
|------|------|----------------|
| 方法不是 `public` | 代理只能拦截 public 方法 | ❌ 否（本方法是 public） |
| **本类内部调用** | `this.method()` 绕过代理，不触发事务 | ⚠️ **是**（需注意） |
| 异常被吞掉 | `try-catch` 没有重新抛出 | ⚠️ **是**（如果有 try-catch） |
| final 方法 | 无法被继承/代理 | ❌ 否 |
| static 方法 | 属于类不属于实例，无法代理 | ❌ 否 |
| 数据库引擎不支持 | 如 MySQL 的 MyISAM | ⚠️ 检查表引擎 |
| 多线程调用 | 线程不在原事务上下文 | ⚠️ 注意异步场景 |
| 错误的异常类型 | 默认只回滚 `RuntimeException` | ❌ 否（已配置 Exception） |

### 三、编程式事务精细化控制方案

#### 1. 注入 `TransactionTemplate`

```java
@Resource
private TransactionTemplate transactionTemplate;
```

#### 2. 改造 `addTeam` 方法

```java
@Override
public long addTeam(Team team, User loginUser) {
    // ========== 事务外：参数校验 ==========
    if (team == null) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    if (loginUser == null) {
        throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    // 队伍人数校验
    int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
    if (maxNum <= 1 || maxNum > 20) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
    }

    // 队伍标题校验
    String teamName = team.getName();
    if (StringUtils.isBlank(teamName) || teamName.length() > 20) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
    }

    // 描述校验
    String description = team.getDescription();
    if (StringUtils.isNotBlank(description) && description.length() > 512) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
    }

    // 状态校验
    Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
    TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
    if (statusEnum == null) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不符合要求");
    }

    // 密码校验
    if (TeamStatusEnum.SECRET.equals(statusEnum)) {
        String password = team.getPassword();
        if (StringUtils.isBlank(password) || password.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
        }
    }

    // 超时时间校验
    Date expireTime = team.getExpireTime();
    if (expireTime != null && new Date().after(expireTime)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间早于当前时间");
    }

    final long userId = loginUser.getId();

    // ========== 事务外：用户已创建队伍数量查询 ==========
    QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
    teamQueryWrapper.eq("userId", userId);
    long hasTeamNum = this.count(teamQueryWrapper);
    if (hasTeamNum >= 5) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建5个队伍");
    }

    // ========== 事务内：数据库操作 ==========
    return transactionTemplate.execute(status -> {
        // 4. 插入队伍信息
        team.setId(null);
        team.setUserId(userId);
        boolean isSave = this.save(team);
        Long teamId = team.getId();
        if (!isSave) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }

        // 5. 插入用户-队伍关系
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        isSave = userTeamService.save(userTeam);
        if (!isSave) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }

        return teamId;
    });
}
```

### 四、编程式事务回滚机制

`TransactionTemplate` 的回滚行为：

```java
// TransactionTemplate 源码简化逻辑
public T execute(TransactionCallback<T> action) {
    TransactionStatus status = ...;
    try {
        T result = action.doInTransaction(status);
        transactionManager.commit(status);  // 成功则提交
        return result;
    } catch (Exception ex) {
        transactionManager.rollback(status);  // ❌ 任何异常都回滚
        throw ex;
    }
}
```

**关键点**：
- `TransactionTemplate` **不需要手动调用** `status.setRollbackOnly()`
- 抛出 `BusinessException` **会自动回滚**
- 正常返回结果 **会自动提交**

### 五、@Transactional vs TransactionTemplate 对比

| 对比项 | `@Transactional` | `TransactionTemplate` |
|--------|------------------|----------------------|
| 异常回滚 | 需要配置 `rollbackFor` | **任何异常都自动回滚** |
| 配置方式 | 注解参数 | 编程式，无配置项 |
| 粒度控制 | 整个方法 | 代码块级别 |
| 回滚时机 | 异常自动触发 | 异常自动触发（也可 `status.setRollbackOnly()`） |
| 性能 | 最小事务范围 | 可精确控制事务边界 |
| 可读性 | 声明式，简洁 | 稍显冗长 |
| 场景适配 | 简单CRUD | 复杂业务逻辑 |

### 六、推荐做法

| 场景 | 推荐方案 |
|------|----------|
| 当前 `addTeam` | **保持 `@Transactional`**（简单清晰） |
| 需要部分代码不参与事务 | 改用编程式事务 |
| 复杂业务逻辑+多次DB操作 | 编程式事务 + `TransactionTemplate` |
| 需要手动控制回滚时机 | `status.setRollbackOnly()` |

### 七、实际分析

对于当前场景：

```
┌─────────────────────────────────────────────────┐
│  校验逻辑（无 DB 操作）    │  占用时间：~1ms    │
├─────────────────────────────────────────────────┤
│  count 查询（有 DB 操作）  │  占用时间：~10ms   │
├─────────────────────────────────────────────────┤
│  插入 team + userTeam     │  占用时间：~20ms   │
└─────────────────────────────────────────────────┘
```

**结论**：
- 校验放不放事务内，性能差异极小（1ms vs 30ms）
- `count` 查询本身就是 DB 操作，在事务内外都可以
- **`@Transactional` 方案已经足够好**

### 八、编程式事务适用于

| 场景 | 示例 |
|------|------|
| 需要远程调用后再决定是否提交 | 调用第三方 API 成功后才提交 |
| 复杂条件决定事务边界 | 根据用户等级决定是否开启事务 |
| 需要部分操作不回滚 | 日志记录即使失败也不影响业务 |

---

**总结**：你的 `@Transactional` 写法没问题，编程式事务适合更复杂的场景。

---

# 2026/05/04
## MyBatis-Plus QueryWrapper：.eq() 和 .and() 的区别

### 核心区别

| 写法 | 作用 | SQL 示例 | 使用场景 |
|------|------|----------|----------|
| `.eq()` | 单独的 AND 条件 | `AND id = ?` | 简单等值条件 |
| `.and(lambda -> ...)` | 分组，控制 OR 的作用范围 | `AND (条件1 OR 条件2)` | 内部有 OR 时必须分组 |

### 1. .eq() - 单个独立条件

直接使用 `.eq()` 是**单独的 AND 条件**，与其他条件是**并列关系**：

```java
// TeamServiceImpl.java 第 128、149、154、167 行
queryWrapper.eq(Team::getId, teamId);        // AND id = ?
queryWrapper.eq(Team::getMaxNum, maxNum);    // AND maxNum = ?
queryWrapper.eq(Team::getUserId, userId);    // AND userId = ?
queryWrapper.eq(Team::getStatus, statusEnum.getValue());  // AND status = ?
```

生成的 SQL：
```sql
WHERE id = ? AND maxNum = ? AND userId = ? AND status = ?
```

### 2. .and() - 分组条件（括号优先级）

使用 `.and()` 是**将一组条件用括号包裹**，确保这些条件内部的逻辑优先计算：

#### 示例 1：名称或描述匹配关键词（第 134-136 行）

```java
queryWrapper.and(qw -> qw.like(Team::getName, searchText)
        .or().like(Team::getDescription, searchText));
```

生成的 SQL：
```sql
WHERE (name LIKE ? OR description LIKE ?)
```

#### 示例 2：未过期或未设置过期时间（第 172-173 行）

```java
queryWrapper.and(qw -> qw.gt(Team::getExpireTime, new Date())
        .or().isNull(Team::getExpireTime));
```

生成的 SQL：
```sql
WHERE (expireTime > ? OR expireTime IS NULL)
```

### 为什么需要 .and()？

**如果不使用 `.and()`，会导致逻辑错误**（AND 优先级高于 OR）：

#### 错误写法（不用 and）

假设查询条件是：
- 名称含"游戏" **或** 描述含"游戏"
- **并且** 状态 = 0

```java
queryWrapper.like(Team::getName, searchText)
    .or().like(Team::getDescription, searchText)
    .eq(Team::getStatus, 0);
```

生成的 SQL：
```sql
WHERE name LIKE '%游戏%' OR description LIKE '%游戏%' AND status = 0
-- 由于 AND 优先级高于 OR，实际逻辑是：
-- name LIKE '%游戏%' OR (description LIKE '%游戏%' AND status = 0)
```

❌ **问题**：只要名称匹配就返回，不管状态如何！

#### 正确写法（用 and）

```java
queryWrapper.and(qw -> qw.like(Team::getName, searchText)
        .or().like(Team::getDescription, searchText))
    .eq(Team::getStatus, 0);
```

生成的 SQL：
```sql
WHERE (name LIKE '%游戏%' OR description LIKE '%游戏%') AND status = 0
```

✅ **正确**：名称或描述匹配，**并且**状态为 0

### SQL 运算符优先级参考

| 优先级 | 运算符 |
|--------|--------|
| 1（最高） | 括号 `()` |
| 2 | NOT |
| 3 | AND |
| 4（最低） | OR |

### 实战对比

#### 场景：搜索队伍，支持关键词（名称/描述）+ 状态过滤

```java
// 前端传入：searchText="游戏", status=0
LambdaQueryWrapper<Team> qw = new LambdaQueryWrapper<>();

// ❌ 错误：不用 .and()
qw.like(Team::getName, "游戏")
  .or().like(Team::getDescription, "游戏")
  .eq(Team::getStatus, 0);
// SQL: name LIKE '%游戏%' OR description LIKE '%游戏%' AND status = 0
// 结果：会返回名称含"游戏"的所有队伍（包括非公开的）

// ✅ 正确：用 .and()
qw.and(w -> w.like(Team::getName, "游戏")
           .or().like(Team::getDescription, "游戏"))
  .eq(Team::getStatus, 0);
// SQL: (name LIKE '%游戏%' OR description LIKE '%游戏%') AND status = 0
// 结果：只返回公开的、名称或描述含"游戏"的队伍
```

### 一句话总结

> 当内部有 `.or()` 时，必须用 `.and()` 包裹，避免 OR 扩散到其他条件。

### 速查表

| 需求 | 代码写法 |
|------|----------|
| A 且 B 且 C | `.eq(A).eq(B).eq(C)` |
| A 或 B | `.and(w -> w.eq(A).or().eq(B))` |
| (A 或 B) 且 C | `.and(w -> w.eq(A).or().eq(B)).eq(C)` |
| A 且 (B 或 C) | `.eq(A).and(w -> w.eq(B).or().eq(C))` |

---

## TeamUserVO 的设计意图

### 核心目的：聚合展示 + 安全脱敏

`TeamUserVO` 是一个 **VO（View Object）**，用于返回给前端。它的设计意图是解决两个问题：

| 问题 | 解决方案 |
|------|----------|
| **一个队伍需要展示创建人信息** | 添加 `createUser` 字段（UserVO 类型） |
| **敏感数据不能返回给前端** | 移除 `password` 字段，使用 `UserVO` 而非完整 `User` |

### 1. 字段对比：Team vs TeamUserVO

```java
// Team（数据库实体）
├── id, name, description, maxNum
├── expireTime, userId, status
├── password  ❌ 敏感信息
├── createTime, updateTime
└── isDelete  ❌ 内部字段

// TeamUserVO（返回给前端）
├── id, name, description, maxNum
├── expireTime, userId, status
├── createTime, updateTime
├── createUser     ✅ 新增：创建人信息（UserVO）
├── hasJoinNum     ✅ 新增：已加入人数
└── hasJoin        ✅ 新增：当前用户是否已加入
```

### 2. 为什么不用 Team 直接返回？

| 原因 | 说明 |
|------|------|
| **敏感信息泄露** | `Team.password` 会被返回给前端 |
| **信息不完整** | 前端需要显示创建人昵称、头像，但 `Team.userId` 只是 ID |
| **业务字段缺失** | 前端需要"已加入人数"、"是否已加入"等展示字段 |

### 3. 为什么用 UserVO 而不是 User？

```java
// User 包含敏感信息
private String userPassword;  // ❌ 不能返回
private String phone;         // ❌ 不能返回
private String email;         // ❌ 不能返回
private Integer isDelete;     // ❌ 内部字段

// UserVO（脱敏后的用户信息）
// 只包含可以公开的字段
```

### 4. 设计模式：VO vs DTO vs Entity

```
┌─────────────┐
│   Entity    │  Team / User - 数据库实体
└──────┬──────┘
       │ 映射/转换
       ↓
┌─────────────┐
│    DTO      │  TeamQuery / TeamUserVO - 数据传输对象
└──────┬──────┘
       │
       ↓
┌─────────────┐
│    VO       │  TeamUserVO - 视图对象（给前端看的）
└─────────────┘
```

### 5. 实际使用场景

```java
// 前端请求：获取队伍列表
GET /api/team/list

// 后端返回
[
  {
    "id": 1,
    "name": "游戏开黑",
    "description": "一起来玩",
    "maxNum": 5,
    "createUser": {
      "id": 10,
      "username": "张三",
      "avatarUrl": "https://..."
    },
    "hasJoinNum": 3,
    "hasJoin": false  // 当前用户未加入
  }
]
```

### 6. 总结

| 优点 | 说明 |
|------|------|
| **安全** | 隐藏敏感字段（password、phone 等） |
| **完整** | 聚合关联数据（创建人信息） |
| **灵活** | 可根据不同接口返回不同字段 |
| **解耦** | 数据库结构与前端展示分离 |

**一句话总结**：`TeamUserVO` = `Team`（移除敏感字段）+ `UserVO`（创建人信息）+ 业务展示字段（已加入人数等）

---

## DTO（Data Transfer Object）的作用

### 1. 核心定义

DTO = **数据传输对象**，用于**接收前端请求参数**的封装类。

```
前端请求 → DTO → Service → Entity → 数据库
```

### 2. DTO vs VO vs Entity 完整对比

| 类型 | 英文 | 作用 | 方向 | 示例 |
|------|------|------|------|------|
| **Entity** | 实体 | 数据库表映射 | ↔ DB | `Team`、`User` |
| **DTO** | Data Transfer Object | 接收请求参数 | 前端 → 后端 | `TeamQuery` |
| **VO** | View Object | 返回响应数据 | 后端 → 前端 | `TeamUserVO` |

```
┌─────────┐          ┌─────────┐          ┌─────────┐
│ 前端    │ ───────> │  DTO    │ ───────> │ Service │
│ 请求    │  传入参数 │TeamQuery│  查询条件 │         │
└─────────┘          └─────────┘          └─────────┘
                                            │
                                            ↓
┌─────────┐          ┌─────────┐          ┌─────────┐
│ 前端    │ <─────── │   VO    │ <─────── │  Entity │
│ 展示    │  返回数据 │TeamUserVO│  查询结果 │  Team   │
└─────────┘          └─────────┘          └─────────┘
```

### 3. TeamQuery DTO 的实际价值

#### 价值 1：接收前端查询条件

```java
// 前端请求
GET /api/team/list?id=1&name=游戏&maxNum=5&status=0&pageNum=1&pageSize=10

// 后端接收（Controller）
public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
    // teamQuery 已自动封装好所有参数：
    // teamQuery.id = 1
    // teamQuery.name = "游戏"
    // teamQuery.maxNum = 5
    // teamQuery.status = 0
    // teamQuery.pageNum = 1
    // teamQuery.pageSize = 10
}
```

#### 价值 2：包含业务逻辑字段

```java
public class TeamQuery extends PageRequest {
    // 数据库字段
    private Long id;
    private String name;
    private Integer maxNum;
    private Integer status;

    // ⭐ 业务逻辑字段（数据库不存在）
    private String searchText;  // 同时搜索名称和描述
}
```

在 `listTeams` 中的使用：

```java
// 第 132-136 行
String searchText = teamQuery.getSearchText();
if (StringUtils.isNotBlank(searchText)) {
    queryWrapper.and(qw -> qw.like(Team::getName, searchText)
            .or().like(Team::getDescription, searchText));
}
```

#### 价值 3：继承分页基类

```java
// TeamQuery 继承 PageRequest
public class TeamQuery extends PageRequest {
    // 队伍查询字段...
}

// 自动拥有分页能力
@Data
public class PageRequest {
    protected int pageSize;  // 页面大小
    protected int pageNum;   // 当前第几页
}
```

### 4. 为什么不用 Entity 直接接收？

| 问题 | 说明 |
|------|------|
| **字段过多** | Entity 有 10+ 个字段，前端只查询 2-3 个 |
| **安全风险** | 前端可能传入 `isDelete`、`createTime` 等不该修改的字段 |
| **业务字段缺失** | `searchText` 这种业务字段不属于数据库表 |
| **职责混乱** | Entity 应只关注数据库映射，不应参与 HTTP 交互 |

**错误示例**：

```java
// ❌ 直接用 Team 接收查询条件
public List<Team> listTeams(Team team) {
    // 问题：
    // 1. 前端可以传入 password 字段（安全风险）
    // 2. 前端可以传入 isDelete=1（逻辑漏洞）
    // 3. 无法支持 searchText 这种业务字段
}
```

**正确示例**：

```java
// ✅ 用 TeamQuery DTO 接收
public List<TeamUserVO> listTeams(TeamQuery teamQuery) {
    // 只接收查询需要的字段，安全且清晰
}
```

### 5. DTO 常见命名规范

| 后缀 | 含义 | 示例 |
|------|------|------|
| `Query` | 查询条件 | `TeamQuery`、`UserQuery` |
| `Request` | 请求参数 | `LoginRequest`、`AddTeamRequest` |
| `Command` | 命令（写操作） | `CreateTeamCommand` |

### 6. 数据流转完整图

```
┌─────────────────────────────────────────────────────────────┐
│                    数据流转过程                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  前端请求 → DTO → Service → Entity → 数据库                  │
│           (接收)      (查询)      (存储)                     │
│                                                             │
│  数据库 → Entity → Service → VO → 前端展示                   │
│            (映射)      (组装)   (返回)                      │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                    职责划分                                  │
├─────────────────────────────────────────────────────────────┤
│  Entity: 数据库表映射                                        │
│  DTO:   接收前端请求（可能包含业务逻辑字段）                  │
│  VO:    返回前端响应（可能包含脱敏、聚合数据）                │
└─────────────────────────────────────────────────────────────┘
```

### 7. 一句话总结

- **DTO** = 前端传什么，我就收什么（如 `TeamQuery`）
- **VO** = 前端要什么，我就给什么（如 `TeamUserVO`）
- **Entity** = 数据库存什么，我就映射什么（如 `Team`）

---

---

## 通过 SQL + mapper.xml 实现关联查询已加入队伍的用户信息

### 问题背景

原代码使用循环查询（N+1 问题）：
```java
// 查询队伍列表
List<Team> teamList = this.list(queryWrapper);

// 循环查询创建人信息（N+1 问题）
for (Team team : teamList) {
    User user = userService.getById(team.getUserId());  // 每次循环都查一次数据库
    // ...
}
```

**性能问题**：
- 假设有 100 个队伍
- 查询队伍列表：1 次 SQL
- 循环查询创建人：100 次 SQL
- **总计：101 次 SQL**

### 解决方案：使用 MyBatis 自定义 SQL 关联查询

#### 1. 在 TeamMapper 接口中添加自定义方法

```java
public interface TeamMapper extends BaseMapper<Team> {

    /**
     * 查询队伍列表（包含创建人信息、已加入人数、当前用户是否已加入）
     */
    List<TeamUserVO> listTeamUserVOByIds(@Param("teamIds") List<Long> teamIds, @Param("loginUserId") Long loginUserId);
}
```

#### 2. 在 TeamMapper.xml 中编写 SQL 和结果映射

```xml
<!-- TeamUserVO 结果映射 -->
<resultMap id="TeamUserVOMap" type="com.zjcc.usercenter.model.vo.TeamUserVO">
    <!-- 队伍基本信息 -->
    <id property="id" column="id" />
    <result property="name" column="name" />
    <result property="description" column="description" />
    <result property="maxNum" column="maxNum" />
    <result property="expireTime" column="expireTime" />
    <result property="userId" column="userId" />
    <result property="status" column="status" />
    <result property="createTime" column="createTime" />
    <result property="updateTime" column="updateTime" />
    
    <!-- 已加入人数 -->
    <result property="hasJoinNum" column="hasJoinNum" />
    
    <!-- 当前用户是否已加入 -->
    <result property="hasJoin" column="hasJoin" />
    
    <!-- 创建人信息（关联查询） -->
    <association property="createUser" javaType="com.zjcc.usercenter.model.vo.UserVO">
        <id property="id" column="createUserId" />
        <result property="username" column="createUsername" />
        <result property="userAccount" column="createUserAccount" />
        <result property="avatarUrl" column="createAvatarUrl" />
        <result property="gender" column="createGender" />
        <result property="email" column="createEmail" />
        <result property="userStatus" column="createUserStatus" />
        <result property="createTime" column="createUserCreateTime" />
        <result property="userRole" column="createUserRole" />
        <result property="planetCode" column="createPlanetCode" />
        <result property="profile" column="createProfile" />
        <result property="tags" column="createTags" />
    </association>
</resultMap>

<!-- 查询队伍列表（包含创建人信息、已加入人数、当前用户是否已加入） -->
<select id="listTeamUserVOByIds" resultMap="TeamUserVOMap">
    SELECT
        t.id, t.name, t.description, t.maxNum, t.expireTime, t.userId, t.status, t.createTime, t.updateTime,
        
        -- 已加入该队伍的人数
        COUNT(DISTINCT ut.userId) AS hasJoinNum,
        
        -- 当前用户是否已加入该队伍（0=未加入，1=已加入）
        MAX(CASE WHEN ut.userId = #{loginUserId} THEN 1 ELSE 0 END) AS hasJoin,
        
        -- 创建人用户信息
        u.id AS createUserId, u.username AS createUsername, u.userAccount AS createUserAccount,
        u.avatarUrl AS createAvatarUrl, u.gender AS createGender, u.email AS createEmail,
        u.userStatus AS createUserStatus, u.createTime AS createUserCreateTime,
        u.userRole AS createUserRole, u.planetCode AS createPlanetCode,
        u.profile AS createProfile, u.tags AS createTags

    FROM team t
    -- 关联创建人信息
    LEFT JOIN `user` u ON t.userId = u.id
    -- 关联已加入队伍的用户信息
    LEFT JOIN user_team ut ON t.id = ut.teamId AND ut.isDelete = 0

    WHERE t.id IN
    <foreach collection="teamIds" item="teamId" open="(" separator="," close=")">
        #{teamId}
    </foreach>
    AND t.isDelete = 0

    GROUP BY t.id
</select>
```

#### 3. 修改 TeamServiceImpl 使用自定义 SQL

```java
@Override
public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
    // ... 前面的查询条件拼接逻辑不变 ...
    
    // 不展示已过期的队伍
    queryWrapper.and(qw -> qw.gt(Team::getExpireTime, new Date())
            .or().isNull(Team::getExpireTime));

    // 查询符合条件的队伍
    List<Team> teamList = this.list(queryWrapper);
    if (CollectionUtils.isEmpty(teamList)) {
        return new ArrayList<>();
    }

    // ✅ 提取所有队伍ID
    List<Long> teamIds = teamList.stream()
            .map(Team::getId)
            .collect(java.util.stream.Collectors.toList());

    // ✅ 使用自定义 SQL 关联查询：创建人信息、已加入人数、当前用户是否已加入
    List<TeamUserVO> teamUserVOList = teamMapper.listTeamUserVOByIds(teamIds, null);

    return teamUserVOList;
}
```

### 核心技术点

#### 1. LEFT JOIN 多表关联

```sql
FROM team t
-- 关联创建人信息
LEFT JOIN `user` u ON t.userId = u.id
-- 关联已加入队伍的用户信息
LEFT JOIN user_team ut ON t.id = ut.teamId AND ut.isDelete = 0
```

#### 2. 聚合函数计算已加入人数

```sql
COUNT(DISTINCT ut.userId) AS hasJoinNum
```

#### 3. CASE WHEN 判断当前用户是否已加入

```sql
MAX(CASE WHEN ut.userId = #{loginUserId} THEN 1 ELSE 0 END) AS hasJoin
```

#### 4. resultMap 嵌套映射

```xml
<association property="createUser" javaType="com.zjcc.usercenter.model.vo.UserVO">
    <!-- 创建人信息字段映射 -->
</association>
```

#### 5. foreach 遍历 IN 参数

```xml
WHERE t.id IN
<foreach collection="teamIds" item="teamId" open="(" separator="," close=")">
    #{teamId}
</foreach>
```

### 性能对比

| 实现方式 | SQL 次数（100 个队伍） | 网络开销 | 优点 | 缺点 |
|---------|---------------------|---------|------|------|
| 原实现（循环查询） | 101 次 | 高 | 代码简单 | ❌ 性能差 |
| 新实现（关联查询） | **1 次** | 低 | ✅ 高性能 | SQL 稍复杂 |

### MyBatis 核心概念速查

| 概念 | 作用 | 示例 |
|------|------|------|
| `<resultMap>` | 结果映射配置 | 将数据库字段映射到 VO 对象 |
| `<association>` | 一对一关联映射 | 队伍 ← 创建人 |
| `<collection>` | 一对多关联映射 | 用户 → 多个队伍 |
| `<foreach>` | 遍历集合参数 | IN 条件、批量插入 |
| `<if>` | 动态 SQL 条件 | 根据参数拼接 SQL |
| `<choose>` | 多选一条件 | 类似 Java switch |

### SQL 关键字总结

| 关键字 | 作用 | 示例 |
|--------|------|------|
| `LEFT JOIN` | 左连接，保留左表所有数据 | 关联创建人信息 |
| `COUNT(DISTINCT ...)` | 去重计数 | 统计已加入人数 |
| `CASE WHEN ... THEN ... ELSE ... END` | 条件表达式 | 判断是否已加入 |
| `GROUP BY` | 分组聚合 | 按队伍ID分组 |
| `IN (...)` | 多值匹配 | 查询多个队伍 |
| `AS` | 别名 | 字段重命名 |

---

---

## 分布式锁
```java
Redisson
  1.看门狗的工作机制：看门狗是一个运行在 JVM 进程内的后台定时任务（基于 netty 的 HashedWheelTimer），每隔
  lockWatchdogTimeout / 3（默认 30s / 3 = 10秒）向 Redis 发送续期请求，将锁的 TTL 重新设为 lockWatchdogTimeout。
  2. JVM 崩溃时发生了什么：
    - 看门狗线程是 JVM 进程内的线程，进程死亡 = 看门狗死亡
    - 没有看门狗续期，Redis 中锁的 key 在剩余 TTL 到期后自动过期删除
    - 其他等待的客户端可以正常获取锁
  3. 不会死锁：最坏情况下，锁会在 JVM 崩溃后 30秒内自动释放（即 lockWatchdogTimeout 的默认值）。
```

### 为什么 joinTeam 需要幂等性考虑，而 addTeam 不需要？

#### 核心区别

创建队伍：用户 A 创建队伍 → 只有用户 A 能操作 → 不存在"多个用户同时创建同一个队伍"的情况

加入队伍：
- 多个用户可以加入同一个队伍
- 同一个用户可以多次点击"加入"按钮
- ❌ 存在"重复加入"的风险

#### 创建队伍（add）为什么不需要幂等性？

原因：
- 每次创建都是新队伍，team.id 是自增主键，不会重复
- 每个用户独立创建，不存在多个用户创建同一个队伍的情况
- 即使前端重复点击，最多也只是创建多个不同的队伍

#### 加入队伍（join）为什么需要幂等性？

问题场景：
- 用户快速多次点击"加入"按钮
- 分布式环境下，多个请求同时通过校验
- 结果：插入多条相同的 user_team 记录

---

### 并发场景详解

#### 场景 1：同一用户连续快速点击（单机环境）

用户操作：
- 10:00:00.100 - 点击"加入队伍"
- 10:00:00.110 - 再次点击"加入队伍"（双击/快速点击）

后端处理：
- 10:00:00.120 - 请求1 到达，查询：count = 0
- 10:00:00.125 - 请求1 查询数据库：hasUserJoinTeam = 0
- 10:00:00.130 - 请求2 到达，开始处理
- 10:00:00.135 - 请求2 查询数据库：hasUserJoinTeam = 0
- ❌ 两个请求都通过了校验！
- 10:00:00.150 - 请求1 插入 user_team 记录（id=1）
- 10:00:00.155 - 请求2 插入 user_team 记录（id=2）

结果：user_team 表中有 2 条重复记录！

#### 场景 2：负载均衡分发到不同服务器（分布式环境）

用户操作：
- 10:00:00.100 - 点击"加入队伍"
- 10:00:00.110 - 快速点击第二次（或网络重传）

负载均衡：
- 请求1 → 分发到服务器 A
- 请求2 → 分发到服务器 B

服务器 A：
- 10:00:00.120 - 查询数据库：hasUserJoinTeam = 0
- 10:00:00.130 - 通过校验

服务器 B：
- 10:00:00.125 - 查询数据库：hasUserJoinTeam = 0
- 10:00:00.135 - 通过校验

❌ 两个服务器的请求都通过了校验！

- 10:00:00.150 - 服务器 A 插入 user_team 记录
- 10:00:00.155 - 服务器 B 插入 user_team 记录

结果：user_team 表中有 2 条重复记录！

---
时序图
```mermaid
┌──────────┐                 ┌──────────┐                 ┌──────────┐
│ 请求1    │                 │ 服务器   │                 │  数据库  │
└────┬─────┘                 └────┬─────┘                 └────┬─────┘
│                            │                             │
│  1. 点击"加入"               │                             │
├───────────────────────────>│                             │
│                            │                             │
│                     2. 查询：count = 0 ────────────────>│
│                            │                             │
│                     3. 返回：0   <──────────────────────│
│                            │                             │
│                            │  ❌ 时间片切换               │
┌────┴─────┐                 ┌────┴─────┐                 ┌────┴─────┐
│ 请求2    │                 │ 服务器   │                 │  数据库  │
└────┬─────┘                 └────┬─────┘                 └────┬─────┘
│  4. 再次点击               │                             │
├───────────────────────────>│                             │
│                            │                             │
│                     5. 查询：count = 0 ────────────────>│
│                            │                             │
│                     6. 返回：0   <──────────────────────│
│                            │                             │
│                            │  ❌ 请求2 也通过校验！       │
┌────┴─────┘                 ┌────┴─────┘                 ┌────┴─────┐
│ 请求1    │                 │ 服务器   │                 │  数据库  │
└────┬─────┘                 └────┬─────┘                 └────┬─────┘
│                            │                             │
│                     7. 插入记录   ────────────────────>│
│                            │                             │
┌────┴─────┐                 ┌────┴─────┐                 ┌────┴─────┐
│ 请求2    │                 │ 服务器   │                 │  数据库  │
└────┬─────┘                 └────┬─────┘                 └────┴─────┘
│                            │                             │
│                     8. 插入记录   ────────────────────>│
│                            │                             │
│                            │                             │
│                            │  ❌ 结果：两条重复记录       │
```
---

### 为什么创建队伍没有这个问题？

关键区别：

| 操作 | 独立性 | 是否会重复 |
|------|--------|-----------|
| 创建队伍 | 每个队伍有唯一的主键（自增） | ❌ 不会重复 |
| 加入队伍 | 同一个用户可以多次加入同一个队伍 | ✅ 会重复 |

创建队伍的时序：
- 请求1: 创建队伍 → team.id = 1（数据库自动生成）
- 请求2: 创建队伍 → team.id = 2（数据库自动生成）
- ✅ 结果：两个不同的队伍，没有冲突

加入队伍的时序：
- 请求1: 用户100 加入 队伍1 → user_team.id = 1
- 请求2: 用户100 加入 队伍1 → user_team.id = 2
- ❌ 结果：两条重复的记录（userId=100, teamId=1）

---

### 幂等性对比

| 操作 | 重复执行的影响 | 是否需要幂等性 |
|------|---------------|----------------|
| 创建队伍 | 创建多个不同的队伍 | ❌ 不需要（结果可接受） |
| 加入队伍 | 插入重复记录 | ✅ 需要 |
| 转账 | 重复扣款 | ✅ 需要 |
| 下单 | 创建多个订单 | ✅ 需要 |
| 查询 | 返回相同结果 | ✅ 天然幂等 |

---

### 分布式锁实现方案

推荐写法（细粒度锁）：

```java
public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
    Long teamId = teamJoinRequest.getTeamId();
    Long userId = loginUser.getId();
    
    // ========== 不加锁区域 ==========
    // 1. 查询队伍（只读操作，无需加锁）
    Team team = this.getById(teamId);
    if (team == null) {
        throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
    }
    
    // 2. 校验队伍状态、密码、人数...
    
    // ========== 加锁区域（最小范围） ==========
    String lockKey = "team:join:" + userId + ":" + teamId;
    RLock lock = redissonClient.getLock(lockKey);
    
    try {
        boolean isLocked = lock.tryLock(0, 10, TimeUnit.SECONDS);
        if (!isLocked) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请勿重复操作");
        }
        
        // 5. 再次校验（防止在加锁前已经加入）
        // 6. 检查队伍人数是否已满（防止并发加入导致超员）
        // 7. 插入记录
        
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```
---
加锁后的时序图
```mermaid
┌──────────┐                 ┌──────────┐                 ┌──────────┐
│ 请求1    │                 │  Redis   │                 │  数据库  │
└────┬─────┘                 └────┬─────┘                 └────┬─────┘
│                            │                             │
│  1. 获取锁                 │                             │
├───────────────────────────>│                             │
│                            │                             │
│                     2. 返回：成功 │                             │
│                            │                             │
│  3. 查询并插入               │                             │
├─────────────────────────────────────────────────────────>│
│                            │                             │
│                     4. 返回：成功 │                             │
│                            │                             │
│  5. 释放锁                 │                             │
├───────────────────────────>│                             │
└────┴─────┘                 └────┴─────┘                 └────┴─────┘

┌──────────┐                 ┌──────────┐                 ┌──────────┐
│ 请求2    │                 │  Redis   │                 │  数据库  │
└────┬─────┘                 └────┬─────┘                 └────┬─────┘
│                            │                             │
│  6. 获取锁（等待0秒）       │                             │
├───────────────────────────>│                             │
│                            │                             │
│                     7. 返回：失败（锁被占用）                 │
│                            │                             │
│  ❌ 抛异常：请勿重复操作     │                             │
│                            │                             │
└────┴─────┘                 └────┴─────┘                 └────┴─────┘

```



---

### 锁的粒度对比

| 方案 | 加锁范围 | 性能 | 推荐度 |
|------|---------|------|--------|
| 整个方法加锁 | 整个方法 | ❌ 低 | ❌ 不推荐 |
| 只加关键代码段 | 检查重复 + 插入 | ✅ 高 | ✅ 推荐 |

原则：锁的范围越小越好

| 操作 | 是否需要加锁 | 原因 |
|------|------------|------|
| 查询队伍信息 | ❌ 不需要 | 只读操作，不会有并发问题 |
| 校验队伍状态 | ❌ 不需要 | 状态不会在短时间内变化 |
| 校验密码 | ❌ 不需要 | 密码校验是独立的 |
| 检查重复加入 | ✅ 需要 | 并发问题发生点 |
| 插入记录 | ✅ 需要 | 并发问题发生点 |

---

### 总结

| 场景 | 是否会有并发问题 | 原因 |
|------|-----------------|------|
| 单机 + 单用户快速点击 | ✅ 会 | 线程切换导致两个请求都通过校验 |
| 分布式 + 负载均衡 | ✅ 会 | 不同服务器同时查询数据库都返回 0 |
| 创建队伍 | ❌ 不会 | 每次生成不同的主键，不会冲突 |

核心问题：查询 和 插入 之间有时间窗口，并发请求可能都通过查询，然后都执行插入。

解决方案：使用分布式锁，保证同一用户的"加入同一队伍"操作串行执行。

---

## 事务

### joinTeam 方法需要事务注解吗？

### 数据库操作分析

| 操作 | 类型 | 是否需要事务 |
|------|------|-------------|
| 查询队伍 | 只读 | ❌ 不需要 |
| 校验密码 | 计算 | ❌ 不需要 |
| 检查是否已加入 | 只读 | ❌ 不需要 |
| 插入 user_team | 单条写入 | ❌ 不需要 |

---

### 为什么不需要事务？

事务用于保证：多个操作要么全部成功，要么全部失败

例如：
- 转账：扣款A + 入款B（2个写操作）
- 下单：扣库存 + 创建订单（2个写操作）
- 创建队伍：插入team + 插入user_team（2个写操作）✅

joinTeam：
- 只有一个写操作：插入user_team
- ❌ 不需要事务

---

### 什么时候 joinTeam 需要事务？

如果加入队伍后有后续写操作：

```java
@Transactional(rollbackFor = Exception.class)
public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
    // 1. 插入 user_team
    userTeamService.save(userTeam);
    
    // 2. 更新队伍的当前人数（如果有这个字段）
    team.setCurrentCount(team.getCurrentCount() + 1);
    this.updateById(team);
    
    // 3. 记录加入日志
    joinLogService.save(log);
    
    // ✅ 需要事务：保证3个写操作同时成功或同时失败
}
```

---

### 结论对比

| 场景 | 是否需要 @Transactional | 原因 |
|------|------------------------|------|
| 当前 joinTeam | ❌ 不需要 | 只有一个写操作 |
| addTeam | ✅ 需要 | 两个写操作（插入 team + user_team） |
| joinTeam + 更新人数 | ✅ 需要 | 多个写操作 |

---

### 总结

关键原则：
- 只有当有多个写操作需要保证原子性时，才需要 @Transactional
- 单个写操作不需要事务，数据库本身就能保证一致性

当前 joinTeam 方法只有一个写操作（save(userTeam)），不需要事务。

---
