package com.juc.thread;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class MyThead2 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("come in callable");
        return 1024;
    }
}

public class CallableDemo {
    public static void main(String[] args) {
        FutureTask<Integer> futureTask1 = new FutureTask<>(new MyThead2());
        //FutureTask<Integer> futureTask2 = new FutureTask<>(new MyThead2());
        Thread t1 = new Thread(futureTask1,"aaaa");
        Thread t2 = new Thread(futureTask1,"bbbb");
        t1.start();
        t2.start();

        try {
            System.out.println(futureTask1.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
