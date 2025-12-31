package com.zjcc.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zjcc.usercenter.mapper")
public class ZjccUserCenterBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZjccUserCenterBackendApplication.class, args);
    }

}
