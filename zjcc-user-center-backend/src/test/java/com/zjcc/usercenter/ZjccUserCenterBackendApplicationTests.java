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
    void equalsLong() {
        Long l1 = new Long(6);
        Long l2 = new Long(6);
        System.out.println(l1 == l2);// false
        Long l3 = 6L;
        Long l4 = new Long(6);
        System.out.println(l3 == l4);// false

        Long l5 = 6L;
        Long l6 = 6L;
        System.out.println(l5 == l6);// true 缓存池生效


    }

    @Test
    void contextLoads() {
        String encryptPassword = DigestUtils.md5DigestAsHex((StaticConst.SALT + "123456").getBytes());
        System.out.println("encryptPassword = " + encryptPassword);
    }

}
