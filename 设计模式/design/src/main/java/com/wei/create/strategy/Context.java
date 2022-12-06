package com.wei.create.strategy;

/**
 * 策略模式封装类 - 具体选择使用哪个策略 以及承装策略
 *
 * @author huangw
 * @since 2022-12-06
 */
public class Context {
    /**
     * 构造函数，你要使用哪个妙计
     */
    private IStrategy strategy;

    public Context(IStrategy strategy) {
        this.strategy = strategy;
    }

    public void operate() {
        this.strategy.operate();
    }
}
