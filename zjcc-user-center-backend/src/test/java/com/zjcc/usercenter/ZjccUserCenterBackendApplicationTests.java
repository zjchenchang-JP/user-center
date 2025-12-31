package com.zjcc.usercenter;

// Junit4
// Junit5
import com.zjcc.usercenter.utils.StaticConst;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

@SpringBootTest
class ZjccUserCenterBackendApplicationTests {

    @Test
    void contextLoads() {
        String encryptPassword = DigestUtils.md5DigestAsHex((StaticConst.SALT + "123456").getBytes());
        System.out.println("encryptPassword = " + encryptPassword);
    }

}
