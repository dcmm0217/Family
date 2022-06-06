package com.juc.lock;

import java.util.concurrent.TimeUnit;

class MyLock implements Runnable {
    private String firstLock;
    private String lastLock;

    public MyLock(String firstLock, String lastLock) {
        this.firstLock = firstLock;
        this.lastLock = lastLock;
    }

    @Override
    public void run() {
        synchronized (firstLock) {
            System.out.println(Thread.currentThread().getName() + "持有\t" + firstLock + "\t ， 想要 " + lastLock);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lastLock) {
                System.out.println(Thread.currentThread().getName() + "持有\t" + lastLock + "\t ， 想要 " + firstLock);
            }
        }

    }
}

/**
 * 2个或者多个线程互相持有锁 并且想要获取对方的锁
 * （吃的碗里的，想着锅里的）
 * 为什么死锁呢？ synchronized 不是可重入锁吗？
 * 是可重入锁，可重入的意思是 可以对同一锁进行多次的加锁解锁操作，但是这里是2把锁了，想拿到对方的锁，就会陷入循环等待，所以是偷换概念的一种情况。
 */
public class DeadLockDemo {
    public static void main(String[] args) {
        Thread thread1 = new Thread(new MyLock("lockA","lockB"),"AAAAAAAAAA");
        Thread thread2 = new Thread(new MyLock("lockB","lockA"),"BBBBBBBBBB");
        thread1.start();
        thread2.start();
    }
}
