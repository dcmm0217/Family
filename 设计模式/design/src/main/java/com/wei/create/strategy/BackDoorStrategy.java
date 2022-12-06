package com.wei.create.strategy;

/**
 * 策略模式实现接口 - 走后门策略
 *
 * @author huangw
 * @since 2022-12-06
 */
public class BackDoorStrategy implements IStrategy {

    @Override
    public void operate() {
        System.out.println("策略1：刘备让乔国老 帮开后门");
    }
}
