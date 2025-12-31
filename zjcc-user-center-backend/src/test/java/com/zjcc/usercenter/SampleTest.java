package com.zjcc.usercenter;

import com.zjcc.usercenter.mapper.UserMapper;
import com.zjcc.usercenter.model.User;
// Junit4  需要搭配@RunWith(SpringRunner.class)
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest(classes = ZjccUserCenterBackendApplication.class)
@RunWith(SpringRunner.class)
public class SampleTest {
    @Autowired
    private UserMapper userMapper;
    @Test
    public void test1() {

        List<User> userList = userMapper.selectList(null);
        for (User user : userList) {
            System.out.println("user = " + user);
        }
    }
}
