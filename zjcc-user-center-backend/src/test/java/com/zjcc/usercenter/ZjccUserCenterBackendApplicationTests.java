package com.zjcc.usercenter;

import com.zjcc.usercenter.mapper.UserMapper;
import com.zjcc.usercenter.model.User;
// Junit4
import org.junit.Assert;
// Junit5
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class ZjccUserCenterBackendApplicationTests {
    @Resource
    UserMapper userMapper;
    @Test
    void contextLoads() {
        List<User> userList = userMapper.selectList(null);
        Assertions.assertEquals(5, userList.size());
        userList.forEach(System.out::println);
    }

}
