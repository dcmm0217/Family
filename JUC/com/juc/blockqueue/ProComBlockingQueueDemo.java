package com.juc.blockqueue;

import jdk.nashorn.internal.ir.Flags;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生产者消费者模型
 * 使用阻塞队列实现
 * 使用：volatile、CAS、atomicInteger、BlockQueue、线程交互、原子引用
 */

class MyResource {
    private volatile Boolean FLAG  = true;

    private AtomicInteger atomicInteger = new AtomicInteger();

    BlockingQueue<String> blockingQueue = null;

    public MyResource(BlockingQueue<String> blockingQueue){
        this.blockingQueue = blockingQueue;
        System.out.println(blockingQueue.getClass());
    }

    /**
     * 生产方法
     */
    public void producer() throws InterruptedException {
        String data = null;
        boolean retValue;
        while (FLAG){
            data = atomicInteger.incrementAndGet() + "";

            //2s放入一个data
            retValue = blockingQueue.offer(data, 2L, TimeUnit.SECONDS);
            if (retValue) {
                System.out.println(Thread.currentThread().getName() + "\t 插入队列:" + data  + "成功" );
            }else {
                System.out.println(Thread.currentThread().getName() + "\t 插入队列:" + data  + "失败" );
            }

            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println(Thread.currentThread().getName() + "\t 停止生产，表示FLAG=false，生产介绍");
    }

    /**
     * 消费方法
     */
    public void consumer() throws InterruptedException {
        String retValue;
        while (FLAG){
            retValue = blockingQueue.poll(2L, TimeUnit.SECONDS);
            if (retValue != null && !retValue.equals("")){
                System.out.println(Thread.currentThread().getName() + "\t 消费队列:" + retValue  + "成功" );
            }else {
                FLAG = false;
                System.out.println(Thread.currentThread().getName() + "\t 消费失败，队列中已为空，退出" );
                //退出消费队列
                return;
            }
        }
    }
    /**
     * 停止生产的判断
     */
    public void stop(){
        this.FLAG = false;
    }
}

public class ProComBlockingQueueDemo {
    public static void main(String[] args) {
        MyResource myResource = new MyResource(new ArrayBlockingQueue<String>(10));

        new Thread(() ->{
            System.out.println(Thread.currentThread().getName() + "\t 生产线程启动");
            System.out.println("");
            System.out.println("");
            try {
                myResource.producer();
                System.out.println("");
                System.out.println("");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"生产者").start();

        new Thread(() ->{
            System.out.println(Thread.currentThread().getName() + "\t 消费线程启动");
            System.out.println("");
            System.out.println("");
            try {
                myResource.consumer();
                System.out.println("");
                System.out.println("");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"消费者").start();

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("");
        System.out.println("");
        System.out.println("5秒中后，生产和消费线程停止，线程结束");

        myResource.stop();


    }
}
