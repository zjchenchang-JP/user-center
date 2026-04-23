package com.zjcc.usercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author zjchenchang
 * @createDate 2026/4/20 22:04
 * @Description 自定义 Swagger 接口文档配置
 */
@Configuration
@EnableSwagger2 // 开启 swagger2 的自动配置
//控制该配置类在哪个环境生效 避免生产环境暴露文档接口地址
@Profile({"dev", "test"}) // knife4j http://localhost:8080/doc.html
public class SwaggerConfig {

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 这里一定要标注控制器的位置
                .apis(RequestHandlerSelectors.basePackage("com.zjcc.usercenter.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * api 信息
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("ZJCC伙伴匹配系统") // 标题
                .description("ZJCC伙伴匹配系统-接口文档") // 描述
                .termsOfServiceUrl("https://github.com/zjchenchang-JP") // 跳转连接
                .contact(new Contact("zjcc","https://github.com/zjchenchang-JP","zjchenchang@gmail.com"))
                .version("1.0")
                .build();
    }
}
