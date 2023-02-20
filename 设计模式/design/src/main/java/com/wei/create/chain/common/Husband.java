package com.wei.create.chain.common;

/**
 * 丈夫 实现处理权限
 *
 * @author huangwei
 * @since 2023-02-20
 */
public class Husband implements IHandler {
    @Override
    public void handleMessage(IWomen women) {
        System.out.println("女儿的请示是：" + women.getRequest());
        System.out.println("丈夫的回答是：同意！");
    }
}
