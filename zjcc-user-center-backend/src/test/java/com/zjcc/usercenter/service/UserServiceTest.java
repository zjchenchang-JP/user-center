package com.zjcc.usercenter.service;

import com.zjcc.usercenter.model.domain.User;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import javax.annotation.Resource;


/**
 * @description: 用户服务测试
 * @author: zjcc
 * @create: 2025-12-31 18:59:01
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Resource
    UserService userService;
    @Test
    public void testAddUser() {

        User user = userService.getById(1L);
        // List<User> userList = userService.list();
        // userList.forEach(System.out::println);
        System.out.println("user = " + user);
    }

    @Test
    public void userRegister() {
        String userAccount = "zjcc20260109";
        String userPassword = "12345678";
        String checkPassword = "12345678";
        String planetCode = "1";

        long userId = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertTrue(userId < 0);
    }
}