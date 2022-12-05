package com.wei.create.template;

/**
 * 模板方法模式 抽象模板类
 *
 * @author huangw
 * @since 2022/12/5 22:57
 */
public abstract class AbstractClass {

    /**
     * 定义第1个基本方法
     */
    protected abstract void doSomething();

    /**
     * 定义第2个基本方法
     */
    protected abstract void doAnything();

    /**
     * 定义模板方法
     * 主要用于对基本方法进行一个顺序的控制
     */
    public final void templateMethod() {
        // 调用基本方法，完成相关的逻辑
        this.doSomething();
        this.doAnything();
    }
}
