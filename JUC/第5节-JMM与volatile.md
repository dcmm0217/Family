# 第5节-JMM与volatile

## 1、JMM面试题

- 你知道什么是Java内存模型JMM吗？
- JMM与volatile它们两个之间的关系？(下一章详细讲解)
- JMM有哪些特性or它的三大特性是什么？
- 为什么要有JMM，它为什么出现？作用和功能是什么？
- ==happens-before先行发生原则你有了解过吗？==

## 2、计算机硬件存储体系

计算机存储结构，从本地磁盘到主存到CPU缓存，也就是从硬盘到内存，到CPU。

一般对应的程序的操作就是从数据库查数据到内存然后到CPU进行计算

![image-20220412223456947](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412223456947.png)

问题？和推导出我们需要知道JMM

因为有这么多级的缓存(cpu和物理主内存的速度不一致的)，

**CPU的运行并不是直接操作内存而是先把内存里边的数据读到缓存，而内存的读和写操作的时候就会造成不一致的问题**

![image-20220412223555437](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412223555437.png)

==Java虚拟机规范中试图定义一种Java内存模型（java Memory Model，简称JMM) 来屏蔽掉各种硬件和操作系统的内存访问差异，以实现让Java程序在各种平台下都能达到一致的内存访问效果。推导出我们需要知道JMM==

## 3、Java内存模型Java Memory Model

JMM(Java内存模型Java Memory Model，简称JMM)

本身是一种`抽象的概念`并**不真实存在**它仅仅描述的是==一组约定或规范==，通过这组规范定义了程序中(尤其是多线程)

**各个变量的读写访问方式并决定一个线程对共享变量的写入何时以及如何变成对另一个线程可见，关键技术点都是围绕多线程的原子性、可见性和有序性展开的**。

原则：

JMM的关键技术点都是围绕`多线程的原子性`、`可见性`和`有序性`展开的

能干嘛？

1、通过JMM来实现线程和主内存之间的抽象关系。

2、屏蔽各个硬件平台和操作系统的内存访问差异以实现让Java程序在各种平台下都能达到一致的内存访问效果。



## 4、JMM规范下，三大特性

- 可见性

==是指当一个线程修改了某一个共享变量的值，其他线程是否能够立即知道该变更 ，JMM规定了所有的变量都存储在主内存中。==

![image-20220412225124922](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412225124922.png)

Java中==普通的共享变量不保证可见性==，因为数据修改被写入内存的时机是不确定的，多线程并发下很可能出现"脏读"，所以每个线程都有自己的**工作内存**，线程自己的工作内存中保存了该线程使用到的变量的**主内存副本拷贝**，线程对变量的所有操作（读取，赋值等 ）都必需在线程自己的工作内存中进行，而不能够直接读写主内存中的变量。不同线程之间也无法直接访问对方工作内存中的变量，线程间变量值的传递均需要通过主内存来完成。

![image-20220412225323509](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412225323509.png)

- 原子性

==指一个操作是不可中断的，即多线程环境下，操作不能被其他线程干扰==

- 有序性（指令重排序问题）

对于一个线程的执行代码而言，我们总是习惯性认为代码的执行总是从上到下，有序执行。但为了提供性能，编译器和处理器通常会对指令序列进行重新排序。

==指令重排可以保证串行语义一致，但没有义务保证多线程间的语义也一致，即可能产生"脏读"，简单说，==

**两行以上不相干的代码在执行的时候有可能先执行的不是第一条，不见得是从上到下顺序执行，执行顺序会被优化。**

![image-20220412225450470](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412225450470.png)

单线程环境里面确保程序最终执行结果和代码顺序执行的结果一致。

处理器在进行重排序时==必须要考虑指令之间的数据依赖性==（如果存在数据依赖性是不能被重排序的）

多线程环境中线程交替执行,由于编译器优化重排的存在，两个线程中使用的变量能否保证一致性是无法确定的,结果无法预测

简单案例

```java
public void mySort()
{
    int x = 11; //语句1
    int y = 12; //语句2
    x = x + 5;  //语句3
    y = x * x;  //语句4
}

  1234
  2134
  1324
  问题：请问语句4可以重排后变成第一个条吗？ NO 存在前后数据依赖
```

> JMM规范下，多线程对变量的读写过程

读取过程

由于JVM运行程序的实体是线程，而每个线程创建时JVM都会为其创建一个工作内存(有些地方称为栈空间)，工作内存是每个线程的私有数据区域，而Java内存模型中规定所有变量都存储在主内存，主内存是共享内存区域，所有线程都可以访问，

==但线程对变量的操作(读取赋值等)必须在工作内存中进行，首先要将变量从主内存拷贝到的线程自己的工作内存空间，然后对变量进行操作，操作完成后再将变量写回主内存==

不能直接操作主内存中的变量，各个线程中的工作内存中存储着主内存中的变量副本拷贝，因此不同的线程间无法访问对方的工作内存，线程间的通信(传值)必须通过主内存来完成，其简要访问过程如下图:

![image-20220412231346079](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412231346079.png)

JMM定义了线程和主内存之间的抽象关系

1 线程之间的共享变量存储在主内存中(从硬件角度来说就是内存条)
2 每个线程都有一个私有的本地工作内存，本地工作内存中存储了该线程用来读/写共享变量的副本(从硬件角度来说就是CPU的缓存，比如寄存器、L1、L2、L3缓存等)

> 小总结

- 我们定义的所有共享变量都储存在物理主内存中
- 每个线程都有自己独立的工作内存，里面保存该线程使用到的变量的副本(主内存中该变量的一份拷贝)
- 线程对共享变量所有的操作都必须先在线程自己的工作内存中进行后写回主内存，不能直接从主内存中读写(不能越级)
- 不同线程之间也无法直接访问其他线程的工作内存中的变量，线程间变量值的传递需要通过主内存来进行(同级不能相互访问)

## 5、JMM规范下，多线程先行发生原则之happens-before

在JMM中，==如果一个操作执行的结果需要对另一个操作可见性或者 代码重排序，那么这两个操作之间必须存在happens-before关系==。

![image-20220412231659535](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412231659535.png)

如果线程A的操作（x= 5）happens-before(先行发生)线程B的操作（y = x）,那么可以确定线程B执行后y = 5 一定成立;

如果他们不存在happens-before原则，那么y = 5 不一定成立。

这就是happens-before原则的威力。-------------------》包含可见性和有序性的约束

> 先行发生原则说明

如果Java内存模型中所有的有序性都仅靠volatile和synchronized来完成，那么有很多操作都将会变得非常啰嗦，但是我们在编写Java并发代码的时候并没有察觉到这一点。

我们==没有时时、处处、次次，添加volatile和synchronized来完成程序，这是因为Java语言中JMM原则下有一个“先行发生”(Happens-Before)的原则限制和规矩==

这个原则非常重要： 

==它是判断数据是否**存在竞争**，线程是否安全的非常有用的手段。依赖这个原则，我们可以通过几条简单规则一揽子解决并发环境下两个操
作之间是否可能存在冲突的所有问题，而不需要陷入Java内存模型苦涩难懂的底层编译原理之中==。

#### 5.1 happens-before总原则

- 如果一个操作happens-before另一个操作，那么第一个操作的执行结果将对第二个操作可见，而且第一个操作的执行顺序排在第二个操作之前。
- 两个操作之间存在happens-before关系，并不意味着一定要按照happens-before原则制定的顺序来执行。如果重排序之后的执行结果与按照happens-before关系来执行的**结果一致**，那么**这种重排序并不非法**。（类似值日，周一张三，周二李四，有事情可以调换，完成值日这个任务即可）

> happens-before之8条

- 次序规则：一个线程内，按照代码顺序，写在前面的操作先行发生于写在后面的操作；加深说明，前一个操作的结果可以被后续的操作获取。讲白点就是前面一个操作把变量X赋值为1，那后面一个操作肯定能知道X已经变成了1。

- 锁定规则：一个unLock操作先行发生于后面((这里的“后面”是指时间上的先后))对同一个锁的lock操作；

- volatile变量规则：对一个volatile变量的写操作先行发生于后面对这个变量的读操作，==**前面的写对后面的读是可见的**==，这里的“后面”同样是指时间上的先后。

- 传递规则：如果操作A先行发生于操作B，而操作B又先行发生于操作C，则可以得出操作A先行发生于操作C；

- 线程启动规则(Thread Start Rule)：Thread对象的start()方法先行发生于此线程的每一个动作

- 线程中断规则(Thread Interruption Rule)：对线程interrupt()方法的调用先行发生于被中断线程的代码检测到中断事件的发生；可以通过Thread.interrupted()检测到是否发生中断

- 线程终止规则(Thread Termination Rule)：线程中的所有操作都先行发生于对此线程的终止检测，我们可以通过Thread::join()方法是否结束。Thread::isAlive()的返回值等手段检测线程是否已经终止执行。

- 对象终结规则(Finalizer Rule)：一个对象的初始化完成（构造函数执行结束）先行发生于它的finalize()方法的开始；

  对象没有完成初始化之前，是不能调用finalized()方法的

![image-20220412233338439](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220412233338439.png)

把getter/setter方法都定义为synchronized方法

把value定义为volatile变量，由于setter方法对value的修改不依赖value的原值，满足volatile关键字使用场景



## 6、Volatile

#### 6.1 被volatile修改的变量有2大特点

特点：可见性、有序性

==volatile的内存语义：==

- 当写一个volatile变量时，JMM会把该线程对应的本地内存中的共享变量值`立即刷回主内存中`。
- 当读一个volatile变量时，JMM会把该线程对应的本地内存设置为无效，直接从主内存中读取共享变量
- 所以volatile的写内存语义是直接刷新到主内存，读的内存语义是直接从主内存读取。

#### 6.2 内存屏障（面试重点）

先说生活case

没有管控，顺序难保，设定规则，禁止乱序；上海南京路步行街武警“人墙”当红灯

![image-20220413223453593](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413223453593.png)

> 什么是内存屏障？

内存屏障（也称内存栅栏，内存栅障，屏障指令等，是一类同步屏障指令，是CPU或编译器在对内存随机访问的操作中的一个同步点，==使得此点之前的所有读写操作都执行后才可以开始执行此点之后的操作==），避免代码重排序。内存屏障其实就是一种JVM指令，Java内存模型的重排规则会要求Java编译器在生成JVM指令时插入特定的内存屏障指令，==通过这些内存屏障指令，volatile实现了Java内存模型中的可见性和有序性，但volatile无法保证原子性==。

**内存屏障之前的所有写操作都要回写到主内存，**

**内存屏障之后的所有读操作都能获得内存屏障之前的所有写操作的最新结果(实现了可见性)。**

![image-20220413223732469](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413223732469.png)

因此重排序时，不允许把内存屏障之后的指令重排序到内存屏障之前。

==一句话：对一个 volatile 域的写, happens-before 于任意后续对这个 volatile 域的读，也叫写后读。==

> volatile凭什么可以保证可见性和有序性？？？

底层拥有内存屏障 (Memory Barriers / Fences)

> JVM中提供了四类内存屏障指令

上一章讲解过happens-before先行发生原则，类似接口规范，落地？

落地靠什么？你凭什么可以保证？你管用吗？靠的就是底层的内屏屏障指令

C++源码分析

IDEA工具里面找Unsafe.class

![image-20220413224224001](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224224001.png)

Unsafe.java

![image-20220413224310089](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224310089.png)

Unsafe.cpp

![image-20220413224327288](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224327288.png)

OrderAccess.hpp

![image-20220413224346072](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224346072.png)

orderAccess_linux_x86.inline.hpp

![image-20220413224402017](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224402017.png)

四大屏障分别是什么意思？

![image-20220413224447912](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224447912.png)

![image-20220413224501688](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224501688.png)

orderAccess_linux_x86.inline.hpp

![image-20220413224534688](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224534688.png)

happens-before 之 volatile 变量规则

![image-20220413224634319](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224634319.png)

当第一个操作为volatile读时，不论第二个操作是什么，都不能重排序。这个操作保证了volatile读之后的操作不会被重排到volatile读之前。

当第二个操作为volatile写时，不论第一个操作是什么，都不能重排序。这个操作保证了volatile写之前的操作不会被重排到volatile写之后。

当第一个操作为volatile写时，第二个操作为volatile读时，不能重排。

> JMM 就将内存屏障插⼊策略分为 4 种

写

1、在每个 volatile 写操作的前⾯插⼊⼀个 StoreStore 屏障

2、在每个 volatile 写操作的后⾯插⼊⼀个 StoreLoad 屏障

![image-20220413224805130](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224805130.png)

![image-20220413224823719](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224823719.png)

对比图

![image-20220413224850216](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224850216.png)

读

3、 在每个 volatile 读操作的后⾯插⼊⼀个 LoadLoad 屏障

4、在每个 volatile 读操作的后⾯插⼊⼀个 LoadStore 屏障

![image-20220413224947616](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224947616.png)



![image-20220413224957713](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413224957713.png)

#### 6.3 volatile特性

##### **保证可见性**

==说明：保证不同线程对这个变量进行操作时的可见性，即变量一旦改变所有线程立即可见==

```java
public class VolatileSeeDemo
{
    static          boolean flag = true;       //不加volatile，没有可见性
    //static volatile boolean flag = true;       //加了volatile，保证可见性

    public static void main(String[] args)
    {
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName()+"\t come in");
            while (flag)
            {

            }
            System.out.println(Thread.currentThread().getName()+"\t flag被修改为false,退出.....");
        },"t1").start();

        //暂停2秒钟后让main线程修改flag值
        try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }

        flag = false;

        System.out.println("main线程修改完成");
    }
}

// 不加volatile，没有可见性，程序无法停止
// 加了volatile，保证可见性，程序可以停止
```

线程t1中为何看不到被主线程main修改为false的flag的值？

> 问题可能:

1、主线程修改了flag之后没有将其刷新到主内存，所以t1线程看不到。

2、主线程将flag刷新到了主内存，但是t1一直读取的是自己工作内存中flag的值，没有去主内存中更新获取flag最新的值。

> 我们的诉求：

1、线程中修改了工作内存中的副本之后，立即将其刷新到主内存；
2、工作内存中每次读取共享变量时，都去主内存中重新读取，然后拷贝到工作内存。

> 解决：

使用volatile修饰共享变量，就可以达到上面的效果，被volatile修改的变量有以下特点：

1、线程中读取的时候，每次读取都会去主内存中读取共享变量最新的值，然后将其复制到工作内存

2、线程中修改了工作内存中变量的副本，修改之后会立即刷新到主内存

**volatile变量的读写过程**

Java内存模型中定义的8种工作内存与主内存之间的原子操作

==read(读取)→load(加载)→use(使用)→assign(赋值)→store(存储)→write(写入)→lock(锁定)→unlock(解锁)==

![image-20220413225554622](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413225554622.png)

- read: 作用于主内存，将变量的值从主内存传输到工作内存，主内存到工作内存
- load: 作用于工作内存，将read从主内存传输的变量值放入工作内存变量副本中，即数据加载
- use: 作用于工作内存，将工作内存变量副本的值传递给执行引擎，每当JVM遇到需要该变量的字节码指令时会执行该操作
- assign: 作用于工作内存，将从执行引擎接收到的值赋值给工作内存变量，每当JVM遇到一个给变量赋值字节码指令时会执行该操作
- store: 作用于工作内存，将赋值完毕的工作变量的值写回给主内存
- write: 作用于主内存，将store传输过来的变量值赋值给主内存中的变量
- 由于上述只能保证单条指令的原子性，针对多条指令的组合性原子保证，没有大面积加锁，所以，JVM提供了另外两个原子指令：
- lock: 作用于主内存，将一个变量标记为一个线程独占的状态，只是写时候加锁，就只是锁了写变量的过程。
- unlock: 作用于主内存，把一个处于锁定状态的变量释放，然后才能被其他线程占用

##### 没有原子性

volatile变量的复合操作(如i++)不具有原子性

```java
class MyNumber
{
    volatile int number = 0;

    public void addPlusPlus()
    {
        number++;
    }
}

public class VolatileNoAtomicDemo
{
    public static void main(String[] args) throws InterruptedException
    {
        MyNumber myNumber = new MyNumber();

        for (int i = 1; i <=10; i++) {
            new Thread(() -> {
                for (int j = 1; j <= 1000; j++) {
                    myNumber.addPlusPlus();
                }
            },String.valueOf(i)).start();
        }
        
        //暂停几秒钟线程
        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println(Thread.currentThread().getName() + "\t" + myNumber.number);
    }
}
```

从i++的字节码角度说明

![image-20220413230050300](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413230050300.png)

原子性指的是一个操作是不可中断的，即使是在多线程环境下，一个操作一旦开始就不会被其他线程影响。

```java
public void add()
{
   i++; //不具备原子性，该操作是先读取值，然后写回一个新值，相当于原来的值加上1，分3步完成
 }
```

如果第二个线程在第一个线程读取旧值和写回新值期间读取i的域值，那么第二个线程就会与第一个线程一起看到同一个值，并执行相同值的加1操作，这也就造成了线程安全失败，因此对于add方法必须使用synchronized修饰，以便保证线程安全。

> 不保证原子性

![image-20220413230205926](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413230205926.png)

多线程环境下，"数据计算"和"数据赋值"操作可能多次出现，即操作非原子。若数据在加载之后，若主内存count变量发生修改之后，由于线程工作内存中的值在此前已经加载，从而不会对变更操作做出相应变化，即私有内存和公共内存中变量不同步，进而导致数据不一致。

对于volatile变量，JVM只是保证从主内存加载到线程工作内存的值是最新的，也就是数据加载时是最新的。

==由此可见volatile解决的是变量读时的可见性问题，但无法保证原子性，对于多线程修改共享变量的场景必须使用加锁同步==

> 读取赋值一个普通变量的情况

当线程1对主内存对象发起read操作到write操作第一套流程的时间里，线程2随时都有可能对这个主内存对象发起第二套操作

![image-20220413230834797](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413230834797.png)

> 既然一修改就是可见，为什么还不能保证原子性？

==volatile主要是对其中部分指令做了处理==

要use(使用)一个变量的时候必需load(载入），要载入的时候必需从主内存read(读取）这样就解决了读的可见性。 

写操作是把assign和store做了关联(在assign(赋值)后必需store(存储))。store(存储)后write(写入)。也就是做到了给一个变量赋值的时候一串关联指令直接把变量值写到主内存。

就这样通过用的时候直接从主内存取，在赋值到直接写回主内存做到了内存可见性。

![image-20220413231027959](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413231027959.png)

读取赋值一个volatile变量的情况

![image-20220413231100690](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413231100690.png)

read-load-use 和 assign-store-write 成为了两个不可分割的原子操作，==但是在use和assign之间依然有极小的一段真空期，有可能变量会被其他线程读取，导致写丢失一次==...o(╥﹏╥)o

但是无论在哪一个时间点主内存的变量和任一工作内存的变量的值都是相等的。这个特性就导致了volatile变量不适合参与到依赖当前值的运算，如i = i + 1; i++;之类的那么依靠可见性的特点volatile可以用在哪些地方呢？ ==通常volatile用做保存某个状态的boolean值or int值==。

《深入理解Java虚拟机》提到：

![image-20220413231315048](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413231315048.png)

> 面试回答

JVM的字节码，i++分成三步，间隙期不同步非原子操作(i++)

![image-20220413231407048](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220413231407048.png)

##### 指令禁重排



#### 6.4 如何正确使用volatile

#### 6.5 总结