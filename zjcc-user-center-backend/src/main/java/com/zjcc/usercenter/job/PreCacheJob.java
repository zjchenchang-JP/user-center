package com.zjcc.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.zjcc.usercenter.utils.StaticConst.REDIS_PRE_RECOMMEND;
import static com.zjcc.usercenter.utils.StaticConst.R_DISTRIBUTED_LOCK;

/**
 * @author zjchenchang
 * @createDate 2026/5/2 16:33
 * @description 缓存预热: 提高 首页首次访问性能
 */
@Slf4j
@Component
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // vip客户ID名单。TODO 放入配置中心，不停机动态更新
    private final List<Long> vipUserList = Arrays.asList(1L, 3L, 6L);

    // 每天凌晨1点执行，预热推荐用户
    @Scheduled(cron = "0 20 1 * * *")   // 设置时间
    public void doCacheRecommendUser() {
        // 分布式锁。防止多个服务器实例都执行本次预热任务
        RLock lock = redissonClient.getLock(R_DISTRIBUTED_LOCK);

        // 尝试获取锁
        try {
            // 每天只执行一次，锁等待时间0.抢不到锁就不执行
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                // 只有一个线程能获取到锁
                System.out.println("has getLock" + Thread.currentThread().getId());
                for (Long userId : vipUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format(REDIS_PRE_RECOMMEND, userId);
                    try {
                        // 写缓存
                        redisTemplate.opsForValue().set(redisKey, userPage, 30, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("redis set error, key={}", redisKey, e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error ", e);
        } finally {
            // 释放锁 只能释放自己的锁。redisson已经封装好逻辑
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
