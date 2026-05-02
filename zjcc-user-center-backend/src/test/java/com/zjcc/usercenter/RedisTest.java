package com.zjcc.usercenter;

import com.zjcc.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author: zjchenchang
 * @date: 2026/5/2
 * @Description:    Redis测试
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;


    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 增
        valueOperations.set("ccString", "fish");
        valueOperations.set("ccInt", 1);
        valueOperations.set("ccDouble", 2.0);
        User user = new User();
        user.setId(1);
        user.setUsername("ZJCC");
        valueOperations.set("ccUser", user);

        // 查
        Object shayu = valueOperations.get("ccString");
        Assertions.assertTrue("fish".equals((String) shayu));
        shayu = valueOperations.get("ccInt");
        Assertions.assertTrue(1 == (Integer) shayu);
        shayu = valueOperations.get("ccDouble");
        Assertions.assertTrue(2.0 == (Double) shayu);
        System.out.println(valueOperations.get("ccUser"));
        valueOperations.set("ccString", "fish");

        // 删
       redisTemplate.delete("ccString");
    }

}
