package com.zjcc.usercenter.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.zjcc.usercenter.utils.StaticConst.R_DISTRIBUTED_LOCK;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        // list，数据存在本地 JVM 内存中
        List<String> list = new ArrayList<>();
        list.add("shier");
        System.out.println("list:" + list.get(0));

        // list.remove(0);

        // 数据存在 redis 的内存中
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("shier");
        System.out.println("rlist:" + rList.get(0));
        // rList.remove(0);

        // map
        Map<String, Integer> map = new HashMap<>();
        map.put("shier", 10);
        map.get("shier");

        RMap<Object, Object> map1 = redissonClient.getMap("test-map");
        map1.put("hehe", 199);
        map1.get("hehe");
    }

    // 测试redisson 看门狗
    @Test
    void testWatchDog() {
        RLock lock = redissonClient.getLock(R_DISTRIBUTED_LOCK);
        try {
            // 只有一个线程能获取到锁
            // 锁的存在时间要设置为-1（开启开门狗），默认锁的过期时间是30秒; 每 10 秒续期一次
            // debug启动，会被认为是宕机.不续期，30秒到期删除
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock: " + Thread.currentThread().getId());
                // Thread.sleep(300000);// 让人任务一直不结束。方便观察锁续期
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}