package com.wei.create.strategy;

/**
 * 策略模式实现接口 - 开绿灯策略
 *
 * @author huangw
 * @since 2022-12-06
 */
public class GivenGreenLightStrategy implements IStrategy {
    @Override
    public void operate() {
        System.out.println("策略2：求吴国太开绿灯,放行！");
    }
}
