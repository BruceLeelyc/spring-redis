package com.lixl.redis.config;

import com.lixl.redis.utils.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName: RedisService
 * @Description:
 * @Author: lixl
 * @Date: 2021/6/16 21:14
 */
@Component
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 默认过期时长，单位：秒
     */
    public static final long DEFAULT_EXPIRE = 60 * 60 * 24;

    /**
     * 不设置过期时长
     */
    public static final long NOT_EXPIRE = -1;

    public boolean existsKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 重名名key，如果newKey已经存在，则newKey的原值被覆盖
     *
     * @param oldKey
     * @param newKey
     */
    public void renameKey(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * newKey不存在时才重命名
     *
     * @param oldKey
     * @param newKey
     * @return 修改成功返回true
     */
    public boolean renameKeyNotExist(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    /**
     * 删除key
     *
     * @param key
     */
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 删除多个key
     *
     * @param keys
     */
    public void deleteKey(String... keys) {
        Set<String> kSet = Stream.of(keys).map(k -> k).collect(Collectors.toSet());
        redisTemplate.delete(kSet);
    }

    /**
     * 删除Key的集合
     *
     * @param keys
     */
    public void deleteKey(Collection<String> keys) {
        Set<String> kSet = keys.stream().map(k -> k).collect(Collectors.toSet());
        redisTemplate.delete(kSet);
    }

    /**
     * 设置key的生命周期
     *
     * @param key
     * @param time
     * @param timeUnit
     */
    public void expireKey(String key, long time, TimeUnit timeUnit) {
        redisTemplate.expire(key, time, timeUnit);
    }

    /**
     * 指定key在指定的日期过期
     *
     * @param key
     * @param date
     */
    public void expireKeyAt(String key, Date date) {
        redisTemplate.expireAt(key, date);
    }

    /**
     * 查询key的生命周期
     *
     * @param key
     * @param timeUnit
     * @return
     */
    public long getKeyExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 将key设置为永久有效
     *
     * @param key
     */
    public void persistKey(String key) {
        redisTemplate.persist(key);
    }

    /**
     * redis锁定时长自动续订
     * @param field
     * @param key
     * @param value
     * @param lockTime
     */
    public void expandLockTimeHold(String field, String key, String value, int lockTime) {
        SurvivalClamProcessor survivalClamProcessor = new SurvivalClamProcessor("lockField", key, value, 10);
        Thread survivalThread = new Thread(survivalClamProcessor);
        survivalThread.setDaemon(Boolean.TRUE);
        survivalThread.start();
    }

    public class SurvivalClamProcessor implements Runnable {
        private static final int REDIS_EXPIRE_SUCCESS = 1;
        SurvivalClamProcessor(String field, String key, String value, int lockTime) {
            this.field = field;
            this.key = key;
            this.value = value;
            this.lockTime = lockTime;
            this.signal = Boolean.TRUE;
        }

        private String field;
        private String key;
        private String value;
        private int lockTime;

        //线程关闭的标记
        private volatile Boolean signal;

        void stop() {
            this.signal = Boolean.FALSE;
        }

        @Override
        public void run() {
            int waitTime = lockTime * 1000 * 2 / 3;
            while (signal) {
                try {
                    Thread.sleep(waitTime);
                    if (expandLockTime(field, key, value, lockTime)) {
                        if (logger.isInfoEnabled()) {
                            logger.info("expandLockTime 成功，本次等待{}ms，将重置锁超时时间重置为{}s,其中field为{},key为{}", waitTime, lockTime, field, key);
                        }
                    } else {
                        if (logger.isInfoEnabled()) {
                            logger.info("expandLockTime 失败，将导致SurvivalClamConsumer中断");
                        }
                        this.stop();
                    }
                } catch (InterruptedException e) {
                    if (logger.isInfoEnabled()) {
                        logger.info("SurvivalClamProcessor 处理线程被强制中断");
                    }
                } catch (Exception e) {
                    logger.error("SurvivalClamProcessor run error", e);
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("SurvivalClamProcessor 处理线程已停止");
            }
        }
    }

    public Boolean expandLockTime(String field, String key, String value, int lockTime) {
        // boolean lockField = redissonClient.getBucket(key).expire(lockTime, TimeUnit.SECONDS);
        boolean lockField = redisTemplate.expire(key, lockTime, TimeUnit.SECONDS);
        return lockField;
    }
}

