package com.zjcc.usercenter.once;

import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;


/**
 * @author zjchenchang
 * @createDate 2026/5/1 16:22
 */
@SpringBootTest
class InsertUserTest {

    @Resource
    private UserService userService;

    // 自定义线程池
    private ExecutorService executorService = new ThreadPoolExecutor(16,
            100,
            10000,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 1.循环单行插入 见InsertUser类
     * 2.批量插入  耗时 971毫秒
     */
    @Test
    void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假数据");
            user.setUserAccount("yusha");
            user.setAvatarUrl("https://img1.baidu.com/it/u=467212011,1034521901&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=500");
            user.setProfile("一条咸鱼");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123456789108");
            user.setEmail("huoying-renzhe@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("1001");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList,100);
        stopWatch.stop();
        System.out.println( stopWatch.getLastTaskTimeMillis());

    }

    /**
     * 3.多线程异步
     * 并发批量插入用户   100000  耗时： 11584ms - 21170ms
     */
    @Test
    public void doConcurrencyInsertUser() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        // 每一批量插入5000条
        final int BATCH_SIZE = 5000;
        ArrayList<CompletableFuture<Void>> futureList = new ArrayList<>();
        // 循环次数--任务数 100000 / 5000 = 20
        for (int i = 0; i < INSERT_NUM / BATCH_SIZE; i++) {
            ArrayList<User> users = new ArrayList<>();
            // 每批500条数据
            for (int j = 0; j < BATCH_SIZE; j++) {
                User user = new User();
                user.setUsername("2026/5/2假数据");
                user.setUserAccount("Mock"+ j + 1);
                user.setAvatarUrl("https://img1.baidu.com/it/u=467212011,1034521901&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=500");
                user.setProfile("fat cat");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("923456789108");
                user.setEmail("2026/5/2_g123@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("23322");
                user.setTags("[]");
                users.add(user);
            }
            // 异步执行批量插入
            int batchNum = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    System.out.println("批次 " + batchNum + " 开始，线程：" + Thread.currentThread().getName());
                    userService.saveBatch(users, BATCH_SIZE);
                    System.out.println("批次 " + batchNum + " 完成");
                } catch (Exception e) {
                    System.err.println("批次 " + batchNum + " 失败：" + e.getMessage());
                    // throw e;
                    e.printStackTrace();  // 打印完整堆栈，看看具体是什么异常
                }
            }, executorService);
            futureList.add(future);
        }
        // 等待所有异步任务完成
        // CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        // stopWatch.stop();
        // System.out.println("总耗时：" + stopWatch.getLastTaskTimeMillis() + "ms");
        // ✅ 添加异常处理
        try {
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{}))
                    .exceptionally(ex -> {
                        System.err.println("有任务失败：" + ex.getMessage());
                        return null;
                    }).join();
        } finally {
            stopWatch.stop();
            System.out.println("总耗时：" + stopWatch.getLastTaskTimeMillis() + "ms");
        }
    }
}