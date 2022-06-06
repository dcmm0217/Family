package com.juc.utils;

import java.util.concurrent.CountDownLatch;

/**
 * 计数器，倒计时闩锁
 * 倒计时，只有到新年的12点，才会敲钟
 *
 * 使用场景：
 * 1、业务中出现需要压缩A到B天的数据每天生成一个excel(不考虑数据量巨大的情况),
 * 并且打包成zip返回给用户,打包数据的时候使用多线程性能会提高很多,但是这里也会有一个问题,
 * 比如打包5天的数据,那么我们必须让所有的线程都完成了工作才能进行打包否则会出现数据损失的情况
 *
 * 2、云产品进行一个大用户量的导入校验，使用了多线程，必须要让所有线程全部完成校验后，才把正确、错误数据添加到对应的导入对象中
 */
public class CountDownLatchTest {
    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(7);

        for (int i = 1; i <= 7 ; i++) {
            new Thread(() ->{
                System.out.println(Thread.currentThread().getName() + "\t go! go! go!");
                countDownLatch.countDown();
            },String.valueOf(i)).start();
        }
        try {
            //只有计数器归0后才能向下执行主线程
            countDownLatch.await();
            System.out.println("main执行!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
