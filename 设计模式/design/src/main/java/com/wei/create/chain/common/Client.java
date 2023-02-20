package com.wei.create.chain.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 责任链模式场景类
 *
 * @author huangwei
 * @since 2023-02-20
 */
public class Client {

    public static void main(String[] args) {
        Random random = new Random();
        List<IWomen> womenList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            womenList.add(new Women(random.nextInt(4), "我要出去逛街！"));
        }
        // 定义3个请求对象
        IHandler father = new Father();
        IHandler son = new Son();
        IHandler husband = new Husband();

        /**
         * 通过一个int类型的参数来描述妇女的个人状况
         * 1--未出嫁
         * 2--出嫁
         * 3--夫死
         */
        for (IWomen women : womenList) {
            if (women.getType() == 1) {
                System.out.println("\n --------------女儿向父亲请示-----------------");
                father.handleMessage(women);
            } else if (women.getType() == 2) {
                System.out.println("\n --------------女儿向丈夫请示-----------------");
                husband.handleMessage(women);
            } else if (women.getType() == 3) {
                System.out.println("\n --------------女儿向儿子请示-----------------");
                son.handleMessage(women);
            } else {
                System.out.println("\n --------------女儿请示-----------------");
                System.out.println("没有处理人能处理此种情况！！！");
            }
        }
    }
}
