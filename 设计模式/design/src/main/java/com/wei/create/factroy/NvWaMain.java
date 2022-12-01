package com.wei.create.factroy;

/**
 * 工程方法模式 的测试
 *
 * @author huangw
 * @since 2022-12-01 14:52:12
 */
public class NvWaMain {
    public static void main(String[] args) {
        // 阴阳炉
        AbstractHumanFactory yinyanglu = new HumanFactory();

        // 造白人
        WhiteHuman whiteHuman = yinyanglu.createHuman(WhiteHuman.class);
        whiteHuman.getColor();
        // 造黑人
        BlackHuman blackHuman = yinyanglu.createHuman(BlackHuman.class);
        blackHuman.getColor();
        // 造黄人
        YellowHuman yellowHuman = yinyanglu.createHuman(YellowHuman.class);
        yellowHuman.getColor();
    }

    /**
     * 工厂方法模式定义：定义一个用于创建对象的接口，让子类决定实例化哪一个类。工厂方法使一个类的实例化延迟到其子类
     *
     *
     *
     */
}
