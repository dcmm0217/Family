package com.wei.create.factroy;

/**
 * 工程方法模式 的抽象工厂
 *
 * @author huangw
 * @since 2022-12-01 14:52:12
 */
public abstract class AbstractHumanFactory {

    /**
     * 提供一个创建人类的方法
     * 这里采用泛型的目的是 对createHuman的输入参数进行限制 1、必须是Class类型 2、必须是Human的实现类
     * T 表示 只要是实现了Human的接口的类都可以作为参数 （减少Bean之间的转换）
     *
     * @param c 要创建创建对象的反射
     * @return 返回具体的类型
     */
    public abstract <T extends Human> T createHuman(Class<T> c);

}
