package com.wei.create.factroy;
/**
 * 工程方法模式接口
 *
 * @author huangw
 * @since 2022-12-01 14:52:12
 */
public class YellowHuman implements Human{
    @Override
    public void talk() {
        System.out.println("黄人说话！");
    }

    @Override
    public void getColor() {
        System.out.println("黄种人难道不是是黄色的？");
    }
}
