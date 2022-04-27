# 第8节-聊聊ThreadLocal

大厂面试题

- ThreadLocal中ThreadLocalMap的数据结构和关系
- ThreadLocal的key是弱引用，这是为什么？
- ThreadLocal内存泄漏问题你知道吗？
- ThreadLocal中最后为什么要加remove()方法

## 1、ThreadLocal简介

> ThreadLocal是什么

ThreadLocal提供线程局部变量。这些变量与正常的变量不同，因为每一个线程在访问ThreadLocal实例的时候（通过其get和set方法）都有自己的、独立初始化的变量副本。ThreadLocal实例通常是类中的私有静态字段，使用它的目的是希望将状态（例如，用户ID或事物ID）与线程关联起来。

> ThreadLocal能干嘛

实现**每一个线程都有自己专属的本地变量副本**(自己用自己的不麻烦别人，不和其他人共享，人人有份，人各一份)，主要解决了让每个线程绑定自己的值，通过使用get()和set()方法，获取默认值将其值更改为当前线程所存副本的值从而避免了线程安全问题。

![image-20220425230740414](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220425230740414.png)

> ThreadLocalAPI介绍

![image-20220425230936346](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220425230936346.png)

> Demo案例 

按照总销售额统计，方便公司做计划统计（三个线程卖50张票）

```java
class MovieTicket
{
    int number = 50;

    public synchronized void saleTicket()
    {
        if(number > 0)
        {
            System.out.println(Thread.currentThread().getName()+"\t"+"号售票员卖出第： "+(number--));
        }else{
            System.out.println("--------卖完了");
        }
    }
}

/**
 * 三个售票员卖完50张票务，总量完成即可，吃大锅饭，售票员每个月固定月薪
 */
public class ThreadLocalDemo
{
    public static void main(String[] args)
    {
        MovieTicket movieTicket = new MovieTicket();

        for (int i = 1; i <=3; i++) {
            new Thread(() -> {
                for (int j = 0; j <20; j++) {
                    movieTicket.saleTicket();
                    try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
                }
            },String.valueOf(i)).start();
        }
    }
}
```

不参加总和计算，希望各自分灶吃饭，各凭本事提成，按照出单数各自统计（ThreadLocal统计当前线程卖了多少）

```java
class MovieTicket
{
    int number = 50;

    public synchronized void saleTicket()
    {
        if(number > 0)
        {
            System.out.println(Thread.currentThread().getName()+"\t"+"号售票员卖出第： "+(number--));
        }else{
            System.out.println("--------卖完了");
        }
    }
}

class House
{
    // 定义ThreadLocal变量 默认初始化为0
    public static ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(() -> 0);

    public void saleHouse()
    {
        Integer value = threadLocal.get();
        value++;
        threadLocal.set(value);
    }
}

/**
 * 1  三个售票员卖完50张票务，总量完成即可，吃大锅饭，售票员每个月固定月薪
 *
 * 2  分灶吃饭，各个销售自己动手，丰衣足食
 */
public class ThreadLocalDemo
{
    public static void main(String[] args)
    {
        /*MovieTicket movieTicket = new MovieTicket();

        for (int i = 1; i <=3; i++) {
            new Thread(() -> {
                for (int j = 0; j <20; j++) {
                    movieTicket.saleTicket();
                    try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
                }
            },String.valueOf(i)).start();
        }*/

        //===========================================
        House house = new House();

        new Thread(() -> {
            try {
                for (int i = 1; i <=3; i++) {
                    house.saleHouse();
                }
                System.out.println(Thread.currentThread().getName()+"\t"+"---"+house.threadLocal.get());
            }finally {
                house.threadLocal.remove();//如果不清理自定义的 ThreadLocal 变量，可能会影响后续业务逻辑和造成内存泄露等问题
            }
        },"t1").start();

        new Thread(() -> {
            try {
                for (int i = 1; i <=2; i++) {
                    house.saleHouse();
                }
                System.out.println(Thread.currentThread().getName()+"\t"+"---"+house.threadLocal.get());
            }finally {
                house.threadLocal.remove();
            }
        },"t2").start();

        new Thread(() -> {
            try {
                for (int i = 1; i <=5; i++) {
                    house.saleHouse();
                }
                System.out.println(Thread.currentThread().getName()+"\t"+"---"+house.threadLocal.get());
            }finally {
                house.threadLocal.remove();
            }
        },"t3").start();


        System.out.println(Thread.currentThread().getName()+"\t"+"---"+house.threadLocal.get());
    }
}
```

总结：

- 因为每个Thread内有自己的**实例副本**且该副本只有当前线程自己使用
- 既然其他Thread不可访问，那就不存在多线程间共享的问题
- 统一设置初始值，但是每个线程对这个值的修改都是各自线程互相独立的。
- 如何才能保证不争抢： 1、加入synchronized锁或者lock锁控制资源的访问顺序      2、使用ThreadLocal人手一份各自玩各自的。

## 2、ThreadLocal规范

![image-20220426212517960](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426212517960.png)

![image-20220426212527592](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426212527592.png)

> 对于非线程安全的SimpleDateFormat处理方案

SimpleDateFormat中的日期格式不是同步的。推荐（建议）为每个线程创建独立的格式实例。如果多个线程同时访问一个格式，则它必须保持外部同步。

写时间工具类，一般写成静态的成员变量，不知，此种写法的多线程下的危险性！

```java
public class DateUtils
{
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 模拟并发环境下使用SimpleDateFormat的parse方法将字符串转换成Date对象
     * @param stringDate
     * @return
     * @throws Exception
     */
    public static Date parseDate(String stringDate)throws Exception
    {
        return sdf.parse(stringDate);
    }
    
    public static void main(String[] args) throws Exception
    {
        for (int i = 1; i <=30; i++) {
            new Thread(() -> {
                try {
                    System.out.println(DateUtils.parseDate("2020-11-11 11:11:11"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();
        }
    }
}
```

![image-20220426222647913](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426222647913.png)

SimpleDateFormat类内部有一个Calendar对象引用,用来储存和这个SimpleDateFormat相关的日期信息,

例如sdf.parse(dateStr),sdf.format(date) 。诸如此类的方法参数传入的日期相关String,Date等等，都是交由Calendar引用来储存的.这样就会**导致一个问题如果你的SimpleDateFormat是个static的, 那么多个thread 之间就会共享这个SimpleDateFormat, 同时也是共享这个Calendar引用。**

![image-20220426223046080](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426223046080.png)

![image-20220426223145083](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426223145083.png)

解决方案1：

将SimpleDateFormat定义成局部变量

缺点：每次调用会创建一个SimpleDateFormat对象，方法结束又要作为垃圾回收。

```java
public class DateUtils
{
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 模拟并发环境下使用SimpleDateFormat的parse方法将字符串转换成Date对象
     * @param stringDate
     * @return
     * @throws Exception
     */
    public static Date parseDate(String stringDate)throws Exception
    {
        return sdf.parse(stringDate);
    }

    public static void main(String[] args) throws Exception
    {
        for (int i = 1; i <=30; i++) {
            new Thread(() -> {
                try {
                    // 用在自己线程创建的，不用公共的，本质也是一种ThreadLocal的思想，但是多次调用会生成多各对象，及时是同一个线程
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    System.out.println(sdf.parse("2020-11-11 11:11:11"));
                    sdf = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();
        }
    }
}
```

解决2：ThreadLocal，也叫线程本地变量或者线程本地存储

```java
public class DateUtils
{
    private static final ThreadLocal<SimpleDateFormat>  sdf_threadLocal =
            ThreadLocal.withInitial(()-> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    /**
     * ThreadLocal可以确保每个线程都可以得到各自单独的一个SimpleDateFormat的对象，那么自然也就不存在竞争问题了。
     * @param stringDate
     * @return
     * @throws Exception
     */
    public static Date parseDateTL(String stringDate)throws Exception
    {
        return sdf_threadLocal.get().parse(stringDate);
    }

    public static void main(String[] args) throws Exception
    {
        for (int i = 1; i <=30; i++) {
            new Thread(() -> {
                try {
                    System.out.println(DateUtils.parseDateTL("2020-11-11 11:11:11"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },String.valueOf(i)).start();
        }
    }
}
```

其他方式：

​	在每次调用parse方法的时候加个锁

​	直接第三方时间库

```java
public class DateUtils
{
    /*
    1   SimpleDateFormat如果多线程共用是线程不安全的类
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String format(Date date)
    {
        return SIMPLE_DATE_FORMAT.format(date);
    }

    public static Date parse(String datetime) throws ParseException
    {
        return SIMPLE_DATE_FORMAT.parse(datetime);
    }*/

    //2   ThreadLocal可以确保每个线程都可以得到各自单独的一个SimpleDateFormat的对象，那么自然也就不存在竞争问题了。
    public static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_THREAD_LOCAL = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public static String format(Date date)
    {
        return SIMPLE_DATE_FORMAT_THREAD_LOCAL.get().format(date);
    }

    public static Date parse(String datetime) throws ParseException
    {
        return SIMPLE_DATE_FORMAT_THREAD_LOCAL.get().parse(datetime);
    }


    //3 DateTimeFormatter 代替 SimpleDateFormat
    /*public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String format(LocalDateTime localDateTime)
    {
        return DATE_TIME_FORMAT.format(localDateTime);
    }

    public static LocalDateTime parse(String dateString)
    {

        return LocalDateTime.parse(dateString,DATE_TIME_FORMAT);
    }*/
}
```

## 3、ThreadLocal源码分析

> ThreadLocal、Thread、ThreadLocalMap关系 

Thread和ThreadLocal

![image-20220426223800120](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426223800120.png)

每个线程，都有自己的一个ThreadLocal，人手一份

ThreadLocal和ThreadLocalMap

![image-20220426223849102](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426223849102.png)

ThreadLocal里面又有静态内部类ThreadLocalMap

> 三者关系概括

![image-20220426224144604](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426224144604.png)

**ThreadLocalMap实际上就是一个以ThreadLocal实例为key，任意对象为value的Entry对象**。

**当我们为ThreadLocal变量赋值，实际上就是以当前threadLocal实例为key，值为value的Entry往这个threadLocalMap存放**

> 小总结

近似的可以理解为:

ThreadLocalMap从字面上就可以看出这是一个保存ThreadLocal对象的map(其实是以ThreadLocal为Key)，不过是经过了两层包装的ThreadLocal对象：

![image-20220426224528202](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426224528202.png)

==JVM内部维护了一个线程版本的Map<Thread,T>（通过ThreadLocal对象的set方法，结果把ThreadLocal对象自己当做key，放进了ThreadLocalMap中）==，每个线程要用到这个T的时候，用当前线程去Map里面获取，**通过这样让每个线程都拥有了自己的独立的变量**。

人手一份，竞争条件被彻底消除，在并发模式下是绝对安全的变量。

## 4、ThreadLocal内存泄漏问题

阿里规范

![image-20220426224822338](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426224822338.png)

> 什么是内存泄漏？

**不会再被使用的对象或者变量占用的内存不能被回收，就是内存泄漏。**

> 究竟是谁造成的内存泄漏?为什么ThreadLocal要采用弱引用？

![image-20220426231629642](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426231629642.png)

> 再回首ThreadLocalMap

ThreadLocalMap与WeakReference 

ThreadLocalMap从字面上就可以就看出这个是一个保存ThreadLocal对象的Map（key 为 ThreadLocal对象），不过是经过了两层包装ThreadLocal对象

1、第一层包装是使用WeakReference<ThreadLocal<?>>将ThreadLocal对象变成一个**弱引用对象**

2、第二层包装是定义了一个专门的类Entry来扩展WeakReference<ThreadLocal<?>>

> 引用的整体架构

![image-20220426233108940](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426233108940.png)

Java 技术允许使用 finalize() 方法在垃圾收集器将对象从内存中清除出去之前做必要的清理工作。

> 强引用(默认引用模式)

当内存不足，JVM开始垃圾回收，对于强引用的对象，就算是**出现了OOM也不会对该对象进行回收，死都不收**。

强引用是我们最常见的普通对象引用，只要还有强引用指向一个对象，就能表明对象还“活着”，垃圾收集器不会碰这种对象。

在 Java 中最常见的就是强引用，把一个对象赋给一个引用变量，这个引用变量就是一个强引用。

一个对象被强引用变量引用时，它处于可达状态，它是不可能被垃圾回收机制回收的，即使该对象以后永远都不会被用到JVM也不会回收。因此强引用是造成Java内存泄漏的主要原因之一。

对于一个普通的对象，如果没有其他的引用关系，只要超过了引用的作用域或者显式地将相应（强）引用赋值为 null，

一般认为就是可以被垃圾收集的了(当然具体回收时机还是要看垃圾收集策略)

```java
public static void strongReference()
{
    MyObject myObject = new MyObject();
    System.out.println("-----gc before: "+myObject);

    myObject = null;
    System.gc();
    try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }

    System.out.println("-----gc after: "+myObject);
}
```

> 软引用

软引用是一种相对强引用弱化了一些的引用，需要用java.lang.ref.SoftReference类来实现，可以让对象豁免一些垃圾收集。

对于只有软引用的对象来说，

当系统内存充足时它      不会     被回收，

当系统内存不足时它         会     被回收

软引用通常用在对内存敏感的程序中，**比如高速缓存就有用到软引用，内存够用的时候就保留，不够用就回收！**

```java
public class ReferenceDemo
{
    public static void main(String[] args)
    {
        //当我们内存不够用的时候，soft会被回收的情况，设置我们的内存大小：-Xms10m -Xmx10m
        SoftReference<MyObject> softReference = new SoftReference<>(new MyObject());

        System.gc();
        try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println("-----gc after内存够用: "+softReference.get());

        try
        {
            byte[] bytes = new byte[9 * 1024 * 1024];
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println("-----gc after内存不够: "+softReference.get());
        }
    }
}
```

> 弱引用

弱引用需要用java.lang.ref.WeakReference类来实现，它比软引用的生存期更短，

对于只有弱引用的对象来说，只要垃圾回收机制一运行，不管JVM的内存空间是否足够，都会回收该对象占用的内存。 

```java
public class ReferenceDemo
{
    public static void main(String[] args)
    {
        WeakReference<MyObject> weakReference = new WeakReference<>(new MyObject());
        System.out.println("-----gc before内存够用: "+weakReference.get());

        System.gc();
        try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }

        System.out.println("-----gc after内存够用: "+weakReference.get());
    }
}
```

软/弱引用的适用场景

![image-20220426233838969](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426233838969.png)

> 虚引用

![image-20220426233931012](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426233931012.png)

![image-20220426233943243](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426233943243.png)

我被回收前需要被引用队列保存下。

```java
public static void main(String[] args)
    {
        ReferenceQueue<MyObject> referenceQueue = new ReferenceQueue();
        PhantomReference<MyObject> phantomReference = new PhantomReference<>(new MyObject(),referenceQueue);
        //System.out.println(phantomReference.get());

        List<byte[]> list = new ArrayList<>();

        new Thread(() -> {
            while (true)
            {
                list.add(new byte[1 * 1024 * 1024]);
                try { TimeUnit.MILLISECONDS.sleep(600); } catch (InterruptedException e) { e.printStackTrace(); }
                System.out.println(phantomReference.get());
            }
        },"t1").start();

        new Thread(() -> {
            while (true)
            {
                Reference<? extends MyObject> reference = referenceQueue.poll();
                if (reference != null) {
                    System.out.println("***********有虚对象加入队列了");
                }
            }
        },"t2").start();

        //暂停几秒钟线程
        try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
    }
```

> GCRoots和四大引用小总结

![image-20220426234156832](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426234156832.png)

![image-20220426234257990](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426234257990.png)

#### 4.1为什么要用弱引用？不用会怎么样？

```java
public void function01()
{
    ThreadLocal tl = new ThreadLocal<Integer>();    //line1
    tl.set(2021);                                   //line2
    tl.get();                                       //line3
}
```

line1新建了一个ThreadLocal对象，t1 是强引用指向这个对象；

line2调用set()方法后新建一个Entry，通过源码可知Entry对象里的k是弱引用指向这个对象。

![image-20220426234435978](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220426234435978.png)

为什么源码要用弱引用？

当function01方法执行完后，栈帧销毁强引用tl也就没有了。但此时线程的ThreadLocalMap里某个Entry的key还指向这个对象。

若这个key是强引用，就会导致key指向ThreadLocal对象及value指向的对象不能被GC回收，造成内存泄漏；

若这个key是弱引用，就大概率会减少内存泄漏的问题（**还有一个key为null的雷**）。===使用弱引用，就可以使用ThreadLocal对象在方法执行完毕后顺序被回收且Entry的key引用指向null==

**此后我们调用get,set或remove方法时，就会尝试删除key为null的entry，可以释放value对象所占用的内存。**

> 仅仅是弱引用就完全可以搞定了吗？

让我们从每一步来具体分析一下：

1、当我们为threadLocal变量赋值，实际上就是当前的Entry（threadLocal实例为key，值为value）往这个threadLocalMap中存放。Entry中的key是弱引用，当threadLocal外部强引用被置为null（tl=null），那么系统GC的时候，根据可达性分析，这个threadLocal实例就没有任何一条链路能够引用到它，这个ThreadLocal势必会被回收，这样一来，ThreadLocalMap中就会出现key为null的Entry，就没有办法访问这些key为null的Entry的value，如果当前线程再迟迟不结束的话，这些key为null的Entry的value就会一直存在一条强引用链：

**ThreadRef -> Thread -> ThreadLocalMap -> Entry -> value永远无法回收，造成内存泄漏**

2、当然，如果当前thread运行结束，threadLocal、threadLocalMap，Entry没有引用链可达，在垃圾回收的时候都会被系统进行回收。

3、但在实际使用的中，我们有时候会使用到**线程池**去维护我们的线程，比如在，Executos.newFixedThreadPool()时创建线程的时候，**为了复用线程是不会结束的，所以threadLocal内存泄漏就值得我们小心。**

一句话总结：

==ThreadLocalMap的Entry数组，key为弱引用的ThreadLocal对象，value为线程所携带的值，系统发生GC的时候，弱引用的key会被回收，而强引用的value还是会存在，这样ThreadLocal就出现了key=null的Entry，value是强引用的，如果线程一直存在的话，（采用线程池的情况）就会一直存在这个value，就会产生内存泄漏。==

> key=null的entry，原理解析

![image-20220427191943937](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220427191943937.png)

ThreadLocalMap使用ThreadLocal的弱引用作为key，如果一个ThreadLocal没有外部强引用引用他，那么GC的时候，这个ThreadLocal势必会被回收，这样一来，ThreadLocalMap中就会出现**Key为null的Entry**，就没有办法访问这些key为null的Entry的value，如果当前线程再迟迟不结束的话（比如正好用在线程池）这些key为null的Entry的value就会一直存在强引用链。

虽然是弱引用，保证了key指向的ThreadLocal对象能被及时回收，但是v指向的value对象是需要ThreadLocalMap调用get、set时发现key为null时才会去回收整个entry、value，**因此弱引用不能100%保证内存不泄漏。我们要在不使用某个ThreadLocal对象后，手动调用remove方法来删除它**，尤其时在线程池中，不仅仅时内存泄露的问题，因为线程池中的线程是重复使用的，意味着这个线程的ThreadLocalMap对象也是重复使用的，如果我们不手动调用remove方法，那么后面的线程就有可能获取到上个线程遗留下来的value值，造成Bug。

> set、get方法会去检查所有键值为null的Entry对象

set()

![image-20220427192915031](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220427192915031.png)

get()

![image-20220427193003964](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220427193003964.png)

remove()

![image-20220427193025989](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220427193025989.png)

从前面的set、getEntry、remove方法看出，在threadLocal生命周期里，针对threadLocal存在的内存泄漏问题，都会通过`expungeStaleEntry`，`cleanSomeSlots`,`replaceStaleEntry`这三个方法清理掉key为null的脏entry

结论:

![image-20220427193246527](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220427193246527.png)

## 5、ThreadLocal最佳实践

1、

![image-20220427193552802](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220427193552802.png)

2、用完记得擦屁股（手动调remove()）

## 6、小总结

- ThreadLocal并不解决线程间共享数据的问题
- ThreadLocal适用于**变量在线程间隔离且在方法间共享的场景**
- ThreadLocal通过隐式的在不同线程内创建独立实例副本避免了实例线程安全的问题。
- **每个线程持有一个属于自己的专属Map并维护了ThreadLocal对象与具体实例的映射，该Map由于只有被持有它的线程访问，故不存在线程安全以及锁的问题**
- ThreadLocalMap的Entry对ThreadLocal的引用为弱引用，避免了ThreadLocal对象无法被回收的问题， + 都会通过`expungeStaleEntry`，`cleanSomeSlots`,`replaceStaleEntry`这三个方法回收键值为null的Entry对象的值（即为具体实例）以及Entry对象本身从而防止内存泄漏，属于安全加固的方法。

