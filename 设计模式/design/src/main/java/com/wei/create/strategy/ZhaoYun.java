package com.wei.create.strategy;

/**
 * 策略模式使用类 - 赵云 策略使用者
 * 对应不同的情况使用不同的策略；
 *
 * @author huangw
 * @since 2022-12-06
 */
public class ZhaoYun {
    // 赵云出场了，他根据诸葛亮给他的交代，依次拆开妙计
    public static void main(String[] args) {
        Context context;
        // 刚刚到吴国的时候拆第一个
        System.out.println("---刚刚到吴国的时候拆第一个---");
        context = new Context(new BackDoorStrategy()); //拿到妙计
        context.operate(); //拆开执行
        System.out.println("\n\n");
        // 刘备乐不思蜀了，拆第二个了
        System.out.println("---刘备乐不思蜀了，拆第二个了---");
        context = new Context(new GivenGreenLightStrategy());
        context.operate(); //执行了第二个锦囊
        System.out.println("\n\n");
        // 孙权的小兵追来了，咋办？拆第三个
        System.out.println("---孙权的小兵追来了，咋办？拆第三个---");
        context = new Context(new BlockEnemyStrategy());
        context.operate(); //孙夫人退兵
        System.out.println("\n\n");
    }
    /*
    策略模式的定义：定义一组算法，将每个算法都封装起来，并且使它们之间可以互换。
     */
}
