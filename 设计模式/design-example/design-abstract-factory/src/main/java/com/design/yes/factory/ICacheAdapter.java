package com.design.yes.factory;

import java.util.concurrent.TimeUnit;

/**
 * 为2个不同模式的缓存，做适配的接口
 *
 * @author huangwei
 * @date 2023-06-05
 */
public interface ICacheAdapter {

    String get(String key);

    void set(String key, String value);

    void set(String key, String value, long timeout, TimeUnit timeUnit);

    void del(String key);

}
