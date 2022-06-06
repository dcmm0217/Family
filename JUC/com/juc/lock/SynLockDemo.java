package com.juc.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A：打印5次  B打印10次  C 打印15次
 * 使用reetrantLock 多condition进行精确唤醒
 * 核心： 标志位number的判断  加上 多condition的唤醒
 */
class shareData{
    //A1 B2 C3
    private int number = 1;
    private Lock lock  = new ReentrantLock();
    private Condition c1 = lock.newCondition();
    private Condition c2 = lock.newCondition();
    private Condition c3 = lock.newCondition();

    public void printA(){
        lock.lock();
        try {
            while (number != 1){
                c1.await();
            }
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + "\t A1");
            }
            number = 2;
            c2.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void printB(){
        lock.lock();
        try {
            while (number != 2){
                c2.await();
            }
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName() + "\t B2");
            }
            number = 3;
            c3.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void printC(){
        lock.lock();
        try {
            while (number != 3){
                c3.await();
            }
            for (int i = 0; i < 15; i++) {
                System.out.println(Thread.currentThread().getName() + "\t C3");
            }
            number = 1;
            c1.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

}

/**
 * synchronized 和 Lock 的区别?
 * 1、底层分析
 * 2、使用方法
 * 3、是否能中断
 * 4、性能
 */
public class SynLockDemo {
    public static void main(String[] args) {
        shareData shareData = new shareData();
        new Thread(() ->{
            for (int i = 1; i <= 10; i++) {
                shareData.printA();
            }
        },"AAA").start();

        new Thread(() ->{
            for (int i = 1; i <= 10; i++) {
                shareData.printB();
            }
        },"BBB").start();

        new Thread(() ->{
            for (int i = 1; i <= 10; i++) {
                shareData.printC();
            }
        },"CCC").start();
    }
}
