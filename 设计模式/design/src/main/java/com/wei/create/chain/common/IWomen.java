package com.wei.create.chain.common;

/**
 * 女性接口类
 *
 * @author huangwei
 * @since 2023-02-20
 */
public interface IWomen {

    // 获得个人情况
    int getType();

    // 获得个人请示，你要出去干什么？逛街？约会？看电影？
    String getRequest();
}
