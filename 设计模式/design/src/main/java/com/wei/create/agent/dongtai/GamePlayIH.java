package com.wei.create.agent.dongtai;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 动态代理类
 *
 * @author huangw
 * @since 2023-02-17
 */
public class GamePlayIH implements InvocationHandler {

    // 被代理者
    private Class cls = null;

    // 被代理的实例
    private Object obj = null;

    // 我要代理谁
    public GamePlayIH( Object obj) {
        this.obj = obj;
    }

    // 调用被代理的方法
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 返回被代理的对象
        return method.invoke(this.obj, args);
    }
}
