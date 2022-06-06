package com.juc.cas;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

@Data
@AllArgsConstructor
class User {
    String name;
    int age;
}

public class ABATest {
    public static void main(String[] args) {
        User z3 = new User("z3", 22);
        User li4 = new User("li4", 25);
        //原子引用（用来判断实体类的CAS操作）
        AtomicReference<Integer> atomicReference = new AtomicReference<Integer>(100);
        AtomicStampedReference atomicStampedReference = new AtomicStampedReference(100, 1);
        boolean bz3 = atomicReference.compareAndSet(100, 101);
        System.out.println(Thread.currentThread().getName() + "\t " + bz3 +":" + atomicReference.get());

        boolean bli4 = atomicReference.compareAndSet(101, 100);
        System.out.println(Thread.currentThread().getName() + "\t " + bli4 +":" + atomicReference.get());
        System.out.println("ABA问题的产生.............");
        new Thread(() -> {
            boolean b11 = atomicReference.compareAndSet(100, 101);
            System.out.println(Thread.currentThread().getName() + "\t " + b11 + "\t value:" + atomicReference.get());

            boolean b22 = atomicReference.compareAndSet(101, 100);
            System.out.println(Thread.currentThread().getName() + "\t " + b22 + "\t value:" + atomicReference.get());

        }, "T1").start();

        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean b12 = atomicReference.compareAndSet(100, 2019);
            System.out.println(Thread.currentThread().getName() + "\t " + b12 + "\t value:" + atomicReference.get());

        }, "T2").start();


        System.out.println("ABA问题的解决..........带时间戳的原子引用.......");
        new Thread(() -> {
            int stamp = atomicStampedReference.getStamp();

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean b1 = atomicStampedReference.compareAndSet(100, 101, stamp, stamp + 1);
            System.out.println(Thread.currentThread().getName() + "\t " + b1 + "\t value:" + atomicStampedReference.getReference().toString() + "\t version:" + atomicStampedReference.getStamp());

            int stamp2 = atomicStampedReference.getStamp();
            boolean b2 = atomicStampedReference.compareAndSet(101, 100, stamp2, stamp2 + 1);
            System.out.println(Thread.currentThread().getName() + "\t " + b2 + "\t value:" + atomicStampedReference.getReference().toString() + "\t version:" + atomicStampedReference.getStamp());

        }, "T3").start();

        new Thread(() -> {
            int stamp = atomicStampedReference.getStamp();
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean b1 = atomicStampedReference.compareAndSet(100, 2019, stamp, stamp + 1);
            System.out.println(Thread.currentThread().getName() + "\t " + b1 + "\t value:" + atomicStampedReference.getReference().toString() + "\t version:" + atomicStampedReference.getStamp());
        },"T4").start();

    }


}
