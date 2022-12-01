package com.wei.create.factroy;

/**
 * 工程方法模式接口
 *
 * @author huangw
 * @since 2022-12-01 14:52:12
 */
public class BlackHuman implements Human {
    public void talk() {
        System.out.println("黑人说话！");
    }

    public void getColor() {
        System.out.println("黑人就是黑色的！");
    }
}
