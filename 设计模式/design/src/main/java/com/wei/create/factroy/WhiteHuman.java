package com.wei.create.factroy;

/**
 * 工程方法模式接口
 *
 * @author huangw
 * @since 2022-12-01 14:52:12
 */
public class WhiteHuman implements Human{
    @Override
    public void talk() {
        System.out.println("白种人说话");
    }

    @Override
    public void getColor() {
        System.out.println("我就问你白不白？");
    }
}
