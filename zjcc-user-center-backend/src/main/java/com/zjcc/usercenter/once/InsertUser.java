package com.zjcc.usercenter.once;
import java.util.Date;

import com.zjcc.usercenter.mapper.UserMapper;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author zjchenchang
 * @createDate 2026/5/1 15:52
 */
@Component
public class InsertUser {

    @Resource
    private UserMapper userMapper;

    /**
     * 循环插入用户
     */
    // 5秒后执行，间隔Long.MAX_VALUE时间再执行。此处相当于单次任务。
    // @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE)
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        int successCount = 0;
        int skipCount = 0;

        for (int i = 0; i < INSERT_NUM; i++) {
            try {
                User user = new User();
                user.setUsername("假用户mockUser_" + i);
                user.setUserAccount("mocker_" + i);
                user.setAvatarUrl("https://img1.baidu.com/it/u=467212011,1034521901&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=500");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("12344321000");
                user.setEmail("abcd_123@gmail.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setProfile("全都是泡沫");
                user.setTags("[]");
                user.setPlanetCode(String.valueOf(i));
                userMapper.insert(user);
                successCount++;
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // planetCode 重复，跳过该条数据
                // 生产环境可以用SQL语句 ON DUPLICATE KEY UPDATE
                skipCount++;
                System.out.println("跳过重复的 planetCode: " + i);
            }
        }

        stopWatch.stop();
        System.out.println("插入完成，成功: " + successCount + "，跳过: " + skipCount + "，耗时: " + stopWatch.getLastTaskTimeMillis() + "ms");
    }
}
