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