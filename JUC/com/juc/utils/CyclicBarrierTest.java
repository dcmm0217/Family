package com.juc.utils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * 可循环使用的屏障
 * 集齐7颗龙珠 召唤神龙
 * 当所有达到计数器之后要执行的操作
 *
 * 使用场景：
 * 1、数据的采用多线程的组装
 */
public class CyclicBarrierTest {
    public static void main(String[] args) {
//        MyThread myThread = new MyThread();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7, ()->{
            System.out.println("召唤神龙成功！");
        });
        /**
         * public CyclicBarrier(int parties, Runnable barrierAction)
         * parties: 计数要达到的值
         * barrierAction : 达到后要执行的线程
         */

        for (int i = 1; i <= 7; i++) {
            final int temp = i;
            new Thread(() ->{
                System.out.println("收集了第"+ temp + "颗龙珠！");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();
        }

        /**
         * 1、为什么要临时变量来存储值呢?
         * 在大量线程下，启动线程的时间很短，但是启动了不代表就执行完结束了，线程本身有自己的生命周期没有结束，
         * 但是for循环定义的临时变量用完生命周期就结束了，当线程里面再去使用这个变量时就会出错。
         *
         * 2、for循环里面直接用临时变量存储就行了，拿为什么还要加final来修饰呢？
         * 多核CPUn并行处理的访问的时候，可能会导致2个线程访问的是同一个变量，但是如果第一个线程访问时这个变量的值为本应该为1，因为某些原因
         * 延迟执行了。被第二个线程给修改了，导致程序出现数据问题。
         * （说人话：并发情况，会存在数据安全问题。）
         */

    }
}

class MyThread implements Runnable {

    @Override
    public void run() {
        System.out.println("召唤神龙成功！");
    }
}
