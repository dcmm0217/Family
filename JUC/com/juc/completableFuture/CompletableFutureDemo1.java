package com.juc.completableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author：huangw
 * @date: 2022/4/4
 */
public class CompletableFutureDemo1 {
    public static void main(String[] args) {
        //demo1();
        // 谁先完成就返回谁
        System.out.println(CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1;
        }).applyToEither(CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 3;
        }), r -> {
            return r;
        }).join());
    }

    private static void demo1() {
        CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "-----come in");
            int result = ThreadLocalRandom.current().nextInt(10);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (result > 6) {
                int age = 1 / 0;
            }
            return result;
        }).whenComplete((v, e) -> {
            if (e == null) {
                System.out.println("-----result: " + v);
            }
        }).exceptionally((e) -> {
            e.printStackTrace();
            return null;
        });

        System.out.println("main thread over !");

        // 主线程不能立马结束，否则completableFuture的线程池会立马关闭
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
