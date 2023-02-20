package com.wei.create.chain.common;

/**
 * 有处理权的人员接口
 *
 * @author huangwei
 * @since 2023-02-20
 */
public interface IHandler {

    // 一个女性（女儿、妻子、或者母亲） 要求逛街，你要处理这个请求
    void handleMessage(IWomen women);
}
