package com.wei.create.chain.common;

/**
 * 儿子 实现处理权限
 *
 * @author huangwei
 * @since 2023-02-20
 */
public class Son implements IHandler {
    @Override
    public void handleMessage(IWomen women) {
        System.out.println("女儿的请示是：" + women.getRequest());
        System.out.println("儿子的回答是：同意！");
    }
}
