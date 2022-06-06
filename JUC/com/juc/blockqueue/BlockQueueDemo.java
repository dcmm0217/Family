package com.juc.blockqueue;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(3);

//        System.out.println(blockingQueue.add("a"));
//        System.out.println(blockingQueue.add("b"));
//        System.out.println(blockingQueue.add("C"));
//        System.out.println(blockingQueue.remove());
//        System.out.println(blockingQueue.remove());
//        System.out.println(blockingQueue.remove());
//        System.out.println(blockingQueue.remove());

//        System.out.println(blockingQueue.offer("a"));
//        System.out.println(blockingQueue.offer("b"));
//        System.out.println(blockingQueue.offer("c"));
//        System.out.println(blockingQueue.poll());
//        System.out.println(blockingQueue.poll());
//        System.out.println(blockingQueue.poll());

//        try {
//            blockingQueue.put("a");
//            blockingQueue.put("b");
//            blockingQueue.put("c");
//            System.out.println("==================");
//            System.out.println(blockingQueue.take());
//            System.out.println(blockingQueue.take());
//            System.out.println(blockingQueue.take());
//            System.out.println(blockingQueue.take());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        System.out.println(blockingQueue.offer("a", 2L, TimeUnit.SECONDS));
//        System.out.println(blockingQueue.offer("b", 2L, TimeUnit.SECONDS));
//        System.out.println(blockingQueue.offer("c", 2L, TimeUnit.SECONDS));
//        System.out.println(blockingQueue.offer("d", 2L, TimeUnit.SECONDS));
//        System.out.println("===========================");
//        System.out.println(blockingQueue.poll(2L, TimeUnit.SECONDS));
//        System.out.println(blockingQueue.poll(2L, TimeUnit.SECONDS));
//        System.out.println(blockingQueue.poll(2L, TimeUnit.SECONDS));
//        System.out.println(blockingQueue.poll(2L, TimeUnit.SECONDS));


        Iterator<String> iterator = blockingQueue.iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}
