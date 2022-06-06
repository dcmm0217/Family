package com.juc.blockqueue;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class SynchronousQueueDemo {
    public static void main(String[] args) {
        SynchronousQueue<String> synchronousQueue = new SynchronousQueue<>();

        new Thread(() ->{
            try {
                System.out.println(Thread.currentThread().getName() + "\t put 123 ");
                synchronousQueue.put("123");
                System.out.println(Thread.currentThread().getName() + "\t put 456 ");
                synchronousQueue.put("456");
                System.out.println(Thread.currentThread().getName() + "\t put 789 ");
                synchronousQueue.put("789");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"AAA").start();

        new Thread(() ->{
            try {

                TimeUnit.SECONDS.sleep(5);
                synchronousQueue.take();
                System.out.println(Thread.currentThread().getName() + "\t take 123 ");

                TimeUnit.SECONDS.sleep(5);
                synchronousQueue.take();
                System.out.println(Thread.currentThread().getName() + "\t take 456  ");

                TimeUnit.SECONDS.sleep(5);
                synchronousQueue.take();
                System.out.println(Thread.currentThread().getName() + "\t take 789  ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"BBB").start();
    }
}
