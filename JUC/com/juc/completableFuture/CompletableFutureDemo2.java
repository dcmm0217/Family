package com.juc.completableFuture;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description: 互联网电商比价需求
 * @author：huangw
 * @date: 2022/4/5
 */
public class CompletableFutureDemo2 {
    // 模拟商城数据集
    static List<NetMall> list = Arrays.asList(
            new NetMall("jd"),
            new NetMall("taobao"),
            new NetMall("dingding"),
            new NetMall("pdd"),
            new NetMall("fuliplus")
    );

    public static List<String> findPriceSync(List<NetMall> list, String productName) {
        List<String> stringList = list.stream()
                .map(mall -> String.format(productName + " %s price is %.2f", mall.getMallName(), mall.getPrice(productName)))
                .collect(Collectors.toList());
        return stringList;
    }

    public static List<String> findPriceAsync(List<NetMall> list, String productName) {

        List<String> stringList = list.stream()
                .map(netmall -> CompletableFuture.supplyAsync(() -> String.format(productName + " %s price is %.2f", netmall.getMallName(), netmall.getPrice(productName))))
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return stringList;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        List<String> list1 = findPriceSync(list, "thinking in java");
        for (String element : list1) {
            System.out.println(element);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("----costTime: " + (endTime - startTime) + " 毫秒");

        long startTime2 = System.currentTimeMillis();
        List<String> list2 = findPriceAsync(list, "thinking in java");
        for (String element : list2) {
            System.out.println(element);
        }
        long endTime2 = System.currentTimeMillis();
        System.out.println("----costTime: " + (endTime2 - startTime2) + " 毫秒");
    }

}


class NetMall {
    @Getter
    private String mallName;

    public NetMall(String mallName) {
        this.mallName = mallName;
    }

    public double getPrice(String productName) {
        return calcPrice(productName);
    }

    public double calcPrice(String productName) {
        // 模拟计算价格的时间大概再1s
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 返回估计的产品的价格
        return ThreadLocalRandom.current().nextDouble() + productName.charAt(0);
    }
}