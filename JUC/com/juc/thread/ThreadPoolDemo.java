package com.juc.thread;

import java.util.concurrent.*;

public class ThreadPoolDemo {
    public static void main(String[] args) {
        // 开启一个5个线程的 线程池
//        ExecutorService executorService = Executors.newFixedThreadPool(5);

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                2,
                5,
                2L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        //模拟10个用户来办理业务，每一个用户就是一个来自外部的请求线程
        try {
            for (int i = 0; i < 10; i++) {
                final int temp = i;
                poolExecutor.execute(() ->{
                    System.out.println(Thread.currentThread().getName() + "\t 给用户：" +temp +"办理业务");
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            poolExecutor.shutdown();
        }
    }
}
