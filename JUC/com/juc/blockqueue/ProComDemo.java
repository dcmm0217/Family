package com.juc.blockqueue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生产者消费者模型
 * 步骤：1、判断  2、干活  3、通知
 * 1、使用synchronized锁 实现
 * 2、lock锁实现
 * 3、阻塞队列实现
 */

class ShareData {
    private int number = 0;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public synchronized void synIncrement() {
        System.out.println(this.getClass());
        while (number != 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        number++;
        System.out.println(Thread.currentThread().getName() + "\t 生产 " + number);
        this.notifyAll();
    }

    public synchronized void synDecrement() {
        while (number == 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        number--;
        System.out.println(Thread.currentThread().getName() + "\t 消费 " + number);
        this.notifyAll();
    }

    public void increment(){
        lock.lock();
        try {
            while (number != 0){
                condition.await();
            }
            number++;
            System.out.println(Thread.currentThread().getName() + "\t 生产 " + number);
            condition.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void decrement(){
        lock.lock();
        try {
            while (number == 0){
                condition.await();
            }
            number--;
            System.out.println(Thread.currentThread().getName() + "\t 消费 " + number);
            condition.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

}


public class ProComDemo {
    public static void main(String[] args) {
        ShareData shareData = new ShareData();

        for (int i = 1; i <= 5; i++) {
            new Thread(() -> {
                shareData.increment();
            }, "AAA").start();
        }

        for (int i = 1; i <= 5; i++) {
            new Thread(() -> {
                shareData.decrement();
            }, "BBB").start();
        }

    }

}
