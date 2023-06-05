package com.design.yes;

import com.design.RedisUtils;

import java.util.concurrent.TimeUnit;

/**
 * 使用抽象工厂，而不是用工具类进行if else处理
 *
 * @author huangwei
 * @date 2023-06-05
 */
public class CacheServiceImpl implements CacheService {

    private RedisUtils redisUtils = new RedisUtils();

    @Override
    public String get(String key) {
        return redisUtils.get(key);
    }

    @Override
    public void set(String key, String value) {
        redisUtils.set(key, value);
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit timeUnit) {
        redisUtils.set(key, value, timeout, timeUnit);
    }

    @Override
    public void del(String key) {
        redisUtils.del(key);
    }
}
