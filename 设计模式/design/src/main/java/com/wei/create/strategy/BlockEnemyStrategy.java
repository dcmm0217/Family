package com.wei.create.strategy;

/**
 * 策略模式实现接口 - 媳妇断后策略
 *
 * @author huangw
 * @since 2022-12-06
 */
public class BlockEnemyStrategy implements IStrategy {
    @Override
    public void operate() {
        System.out.println("策略3：孙夫人断后，挡住追兵");
    }
}
