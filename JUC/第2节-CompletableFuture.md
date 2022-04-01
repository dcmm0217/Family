# 第2节-CompletableFuture

## 1、Future和Callable接口

Future接口定义了操作异步任务执行一些方法，如获取异步任务的执行结果、取消任务的执行、判断任务是否被取消、判断任务执行是否完毕等。

Callable接口中定义了需要有返回的任务需要实现的方法

比如主线程让一个子线程去执行任务，子线程可能比较耗时，启动子线程开始执行任务后，主线程就去做其他事情了，过了一会才去获取子任务的执行结果。（==注意返回值阻塞主线程的情况==）

#### 1.1 FutureTask

本源的Future接口相关架构

![image-20220401224130071](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220401224130071.png)

FutureTask的Demo

```java
public class CompletableFutureDemo
{
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException
    {
        FutureTask<String> futureTask = new FutureTask<>(() -> {
            System.out.println("-----come in FutureTask");
            try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
            return ""+ThreadLocalRandom.current().nextInt(100);
        });

        Thread t1 = new Thread(futureTask,"t1");
        t1.start();

        //3秒钟后才出来结果，还没有计算你提前来拿(只要一调用get方法，对于结果就是不见不散，会导致阻塞)
        //System.out.println(Thread.currentThread().getName()+"\t"+futureTask.get());

        //3秒钟后才出来结果，我只想等待1秒钟，过时不候
        System.out.println(Thread.currentThread().getName()+"\t"+futureTask.get(1L,TimeUnit.SECONDS));

        System.out.println(Thread.currentThread().getName()+"\t"+" run... here");

    }
}
```

get()会导致线程阻塞

一旦调用get()方法，不管是否计算完成都会导致阻塞，o(╥﹏╥)o

==是否能有办法消除阻塞，采用轮询（类似自旋锁）==

```java
public class CompletableFutureDemo2
{
    public static void main(String[] args) throws ExecutionException, InterruptedException
    {
        FutureTask<String> futureTask = new FutureTask<>(() -> {
            System.out.println("-----come in FutureTask");
            try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
            return ""+ThreadLocalRandom.current().nextInt(100);
        });

        new Thread(futureTask,"t1").start();

        System.out.println(Thread.currentThread().getName()+"\t"+"线程完成任务");

        /**
         * 用于阻塞式获取结果,如果想要异步获取结果,通常都会以轮询的方式去获取结果
         */
        while(true)
        {
            if (futureTask.isDone())
            {
                System.out.println(futureTask.get());
                break;
            }
        }
    }
}

```

isDone()轮询

轮询的缺点：

- 轮询的方式会耗费无谓的CPU资源，而且也不见得能及时地得到计算结果
- 如果想要异步获取结果,通常都会以轮询的方式去获取结果尽量不要阻塞

小总结：

futureTask.get()不带时间-----不见不散（一直阻塞，知道计算完内容）

futureTask.get(time) 带时间------过时不候（超时异常）

while(true) -----轮询  还是阻塞，只是没有线程上下文切换

如果想完成一些复杂的任务？

- 应对Future的完成时间，完成了可以告诉我，也就是我们的回调通知
- 将两个异步计算合成一个异步计算，这两个异步计算互相独立，同时第二个又依赖第一个的结果。
- 当Future集合中某个任务最快结束时，返回结果。
- 等待Future集合中的所有任务都完成。

这时候就需要我们的`CompletableFuture`

## 2、对Future的改进--CompletableFuture

#### 2.1 CompletableFuture和CompletionStage源码分别介绍

系统框架说明

![image-20220401225257123](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220401225257123.png)

接口CompletionStage：

![image-20220401225316943](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220401225316943.png)

**代表异步计算过程中的某一个阶段，一个阶段完成以后可能会触发另外一个阶段，有些类似Linux系统的管道分隔符传参数。**

类CompletableFuture：

![image-20220401225417272](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220401225417272.png)

#### 2.2 核心的四个静态方法，来创建一个异步操作

- runAsync 无 返回值

```java
public static CompletableFuture<Void> runAsync(Runnable runnable);

public static CompletableFuture<Void> runAsync(Runnable runnable,Executor executor)
```

```java
public class CompletableFutureDemo2
{
    public static void main(String[] args) throws ExecutionException, InterruptedException
    {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName()+"\t"+"-----come in");
            //暂停几秒钟线程
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println("-----task is over");
        });
        System.out.println(future.get());
        // null
    }
}
```

- supplyAsync 有 返回值

```java
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
    
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier,Executor executor)
```

```java
public class CompletableFutureDemo2
{
    public static void main(String[] args) throws ExecutionException, InterruptedException
    {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "-----come in");
            //暂停几秒钟线程
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ThreadLocalRandom.current().nextInt(100);
        });

        System.out.println(completableFuture.get());
    }
}
```



上述Executor executor参数说明

- 没有指定Executor的方法，直接使用**默认的ForkJoinPool.commonPool() 作为它的线程池执行异步代码。**

- 如果指定线程池，则使用我们自定义的或者特别指定的线程池执行异步代码

Code之通用演示，**减少阻塞和轮询**

从Java8开始引入了CompletableFuture，它是Future的功能增强版，可以传入回调对象，当异步任务完成或者发生异常时，自动调用回调对象的回调方法

```java
public class cfuture4
{
    public static void main(String[] args) throws Exception
    {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "-----come in");
            int result = ThreadLocalRandom.current().nextInt(10);
            //暂停几秒钟线程
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println("-----计算结束耗时1秒钟，result： "+result);
            if(result > 6)
            {
                int age = 10/0;
            }
            return result;
        }).whenComplete((v,e) ->{
            if(e == null)
            {
                System.out.println("-----result: "+v);
            }
        }).exceptionally(e -> {
            System.out.println("-----exception: "+e.getCause()+"\t"+e.getMessage());
            return -44;
        });

        //主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭:暂停3秒钟线程
        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
```

**CompletableFuture的优点**

- 异步任务结束时，会自动回调某个对象的方法；
- 异步任务出错时，会自动回调某个对象的方法；
- 主线程设置好回调后，不再关心异步任务的执行，异步任务之间可以顺序执行

