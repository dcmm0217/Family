package com.juc.utils;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 信号量
 * eg：抢车位，多个线程去处理多个共享资源
 * eg：6个车，3个车位，先暂3个，然后出去一个进来一个。停完走完。
 */
public class SemaphoreTest {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);

        for (int i = 1; i <= 6; i++) {
            new Thread(() ->{
                try {
                    //每个acquire都会阻塞， 得到资源
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName()+ "抢到车位!");

                    TimeUnit.SECONDS.sleep(3);

                    System.out.println(Thread.currentThread().getName() + "挺了3s，离开车位！");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    //每个release()添加许可证  释放资源
                    semaphore.release();
                }
            },String.valueOf(i)).start();

        }
    }
}
