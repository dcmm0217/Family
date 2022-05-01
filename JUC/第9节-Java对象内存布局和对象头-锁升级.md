# 第9节-Java对象内存布局和对象头-锁升级

面试题：

![image-20220428223256924](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428223256924.png)

Object obj = new Object()这句话的理解？

位置：JVM堆开辟内存---->  新生区 -----> 伊甸园区

## 1、对象在堆内存中的布局

![image-20220428223535688](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428223535688.png)

> 对象在堆内存中的存储布局

#### 1、对象头

- **对象标记Mark word**

![image-20220428223750845](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428223750845.png)



![image-20220428223836896](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428223836896.png)

**64为Host pot虚拟机的Mark word的存储结构**

![image-20220428223844537](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428223844537.png)

默认存储对象的HashCode，分代年龄和锁标志位等信息

这些信息都是与对象自身定义无关的数据，所以MarkWord被设计成一个非固定的数据结构以便在极小的空间内存储尽量多的数据。

它还会根据对象的状态复用自己的存储空间，也就是说在运行期间**Mark Word里存储的数据会随着锁标志位的变化而变化**

- **类型指针**

![image-20220428224238944](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428224238944.png)

**对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例。**

- 对象头有多大

在64为系统中，Mark Word占了8个字节，类型指针占了8个字节，一共是16个字节。（未开启类压缩的情况），开启只有12个字节

#### 2、实例数据

存放类的属性（Field）数据信息，包括父类的属性信息，如果是数组的实例部分还包括数组的长度，这部分内存按4字节对齐。

#### 3、对齐填充

虚拟机要求对象起始地址必须是8字节的整数倍，填充数不是必须存在的，仅仅是为了字节对齐这部分内存按8字节补充对齐。

## 2、再聊对象头MarkWord

前提在64位虚拟机下

![image-20220428224743417](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428224743417.png)

![image-20220428224805463](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428224805463.png)



锁升级就是对象标记Mark Word里面标志位的变化

![image-20220428224814554](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428224814554.png)



![image-20220428224830575](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428224830575.png)

````
hash： 保存对象的哈希码
age： 保存对象的分代年龄
biased_lock： 偏向锁标识位
lock： 锁状态标识位
JavaThread* ：保存持有偏向锁的线程ID
epoch： 保存偏向时间戳
````

![image-20220428224855774](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428224855774.png)

## 3、代码说话对象头

Object obj = new Object()这句话的理解？

```xml
<!--
官网：http://openjdk.java.net/projects/code-tools/jol/
定位：分析对象在JVM的大小和分布
-->
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.9</version>
</dependency>
```

```java
public class MyObject
{
    public static void main(String[] args){
        //VM的细节详细情况
        System.out.println(VM.current().details());
        //所有的对象分配的字节都是8的整数倍。
        System.out.println(VM.current().objectAlignment());
        
        
        Object o = new Object();
        // 打印Object o 对象头的信息
        System.out.println( ClassLayout.parseInstance(o).toPrintable());
		
        
    }
}
```

![image-20220428225110743](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428225110743.png)

![image-20220428225149278](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428225149278.png)

尾巴参数说明：

```
java -XX:+PrintCommandLineFlags -version
```

![image-20220428225345267](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428225345267.png)

默认开启对象压缩。

![image-20220428225401209](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428225401209.png)

如果不开启压缩    -XX:-UserCompressedClassPointers

![image-20220428225442416](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428225442416.png)

![image-20220428225554844](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428225554844.png)

## 4、Synchronized与锁升级

面试题：

- 请你谈谈对Synchronized的理解
- 请你聊聊Synchronized的锁升级
- Synchronized的性能是不是一定弱于Lock

![image-20220428225730522](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428225730522.png)

#### 4.1 总纲

![image-20220428225815805](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428225815805.png)

Synchronized锁：由于对象头中的Mark Word根据锁标志位的不同而被复用及锁升级策略。

#### 4.2 Synchronized的性能变化

1.5以前，只有Synchronized，直接是操作系统级别的重量级锁。

会产生用户态和内核态的切换。性能极具下降

![image-20220428230031545](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428230031545.png)

Java的线程是映射到操作系统原生线程之上的，如果要阻塞或唤醒一个线程就需要操作系统介入，需要在用户态与核心态之间切换，这种切换会消耗大量的系统资源，因为用户态与内核态都有**各自专用的内存空间，专用的寄存器等**，用户态切换至内核态需要传递给许多变量、参数给内核，内核也需要保护好用户态在切换时的一些寄存器值、变量等，以便内核态调用结束后切换回用户态继续工作。

在Java早期版本中，synchronized属于重量级锁，效率低下，因为监视器锁（monitor）是**依赖于底层的操作系统的Mutex Lock来实现**的，==挂起线程和恢复线程都需要转入内核态去完成==。

阻塞或唤醒一个Java线程需要操作系统切换CPU状态来完成，这种状态切换需要耗费处理器时间，

如果同步代码块中内容过于简单，这种切换的时间可能比用户代码执行的时间还长”，时间成本相对较高，这也是为什么早期的synchronized效率低的原因

Java 6之后，为了减少获得锁和释放锁所带来的性能消耗，引入了轻量级锁和偏向锁

> 为什么每个对象都可以成为一个锁？？？

markOop.hpp

![image-20220428230302447](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428230302447.png)

Monitor可以理解为一种同步工具，也可理解为一种同步机制，常常被描述为一个Java对象。==Java对象是天生的Monitor，每一个Java对象都有成为Monitor的潜质，因为在Java的设计中 ，每一个Java对象自打娘胎里出来就带了一把看不见的锁，它叫做内部锁或者Monitor锁==。

![image-20220428230322230](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428230322230.png)

**Monitor的本质是依赖于底层操作系统的Mutex Lock实现，操作系统实现线程之间的切换需要从用户态到内核态的转换，成本非常高。**

> 监视器锁

Mutex Lock 

Monitor是在jvm底层实现的，底层代码是c++。本质是依赖于底层操作系统的Mutex Lock实现，操作系统实现线程之间的切换需要从用户态到内核态的转换，状态转换需要耗费很多的处理器时间成本非常高。所以synchronized是Java语言中的一个重量级操作。 

Monitor与java对象以及线程是如何关联 ？

1.如果一个java对象被某个线程锁住，则该java对象的Mark Word字段中LockWord指向monitor的起始地址

2.Monitor的Owner字段会存放拥有相关联对象锁的线程id

Mutex Lock 的切换需要从用户态转换到核心态中，因此状态转换需要耗费很多的处理器时间。

Java 6之后，为了减少获得锁和释放锁所带来的性能消耗，引入了轻量级锁和偏向锁，有一个逐步升级的过程，别一开始就直接使用重量级。

#### 4.3 synchronized锁种类及升级步骤

> 多线程访问情况

- 只有一个线程来访问，有且唯一Only One
- 有2个线程A，B交替访问
- 竞争激烈，多个线程来访问

> 升级流程

Synchronized用的锁是存在Java对象头里Mark Word中锁升级功能主要依赖Mark Word中锁标志位和释放偏向锁标志位

==64位标记图==

![image-20220428224814554](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220428224814554.png)

> 无锁状态

```java
public class MyObject
{
    public static void main(String[] args)
    {
        Object o = new Object();

        System.out.println("10进制hash码："+o.hashCode());
        System.out.println("16进制hash码："+Integer.toHexString(o.hashCode()));
        System.out.println("2进制hash码："+Integer.toBinaryString(o.hashCode()));

        System.out.println( ClassLayout.parseInstance(o).toPrintable());
    }
}
```

程序不会有锁的竞争

![image-20220501232658334](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220501232658334.png)

> 偏向锁

作用：

当一段同步代码一直被同一个线程多次访问，由于只有一个线程那么该线程在后续访问时便会自动获得锁

eg：

同一个老顾客来访，直接老规矩行方便

看看多线程卖票，同一个线程获得体会感觉一下

小结论：

Hotspot 的作者经过研究发现，大多数情况下：**多线程的情况下，锁不仅不存在多线程竞争，还存在锁由同一线程多次获得的情况，偏向锁就是在这种情况下出现的，它的出现是为了解决只有在一个线程执行同步时提高性能。**

![image-20220501233428533](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220501233428533.png)

通过CAS的方式修改Mark Word对象头里面的线程ID

> 偏向锁的持有

理论落地：

​	在实际应用运行过程中发现，“锁总是同一个线程持有，很少发生竞争”，也就是说锁总是被第一个占用他的线程拥有，这个线程就是锁的偏向线程。

​	那么只需要在锁第一次被拥有的时候，记录下偏向线程ID。这样偏向线程就一直持有着锁(后续这个线程进入和退出这段加了同步锁的代码块时，==不需要再次加锁和释放锁。**而是直接比较对象头里面是否存储了指向当前线程的偏向锁)**。==

​	如果相等表示偏向锁是偏向于当前线程的，就不需要再尝试获得锁了，直到竞争发生才释放锁。以后每次同步，检查锁的偏向线程ID与当前线程ID是否一致，如果一致直接进入同步。无需每次加锁解锁都去CAS更新对象头。如果自始至终使用锁的线程只有一个，很明显偏向锁几乎没有额外开销，性能极高。

​	如不一致意味着发生了竞争，锁已经不是总是偏向于同一个线程了，这时候可能需要升级变为轻量级锁，才能保证线程间公平竞争锁。偏向锁只有遇到其他线程尝试竞争偏向锁时，持有偏向锁的线程才会释放锁，线程是不会主动释放偏向锁的。

技术实现:

​	一个synchronized方法被一个线程抢到了锁时，那这个方法所在的对象就会在其所在的Mark Word中将偏向锁修改状态位，同时还会占有前54位来存储线程指针作为标识。若线程再次访问同一个synchronized方法时，该线程只需去对象头的Mark Word中去判断一下是否有偏向锁指向本身的ID，无需再进入Monitor去竞争对象了。

**偏向锁的案例**

偏向锁的操作不用直接捅到操作系统，不涉及用户到内核转换，不必要直接升级为最高级，我们以一个account对象的“对象头”为例，

![image-20220501234010083](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220501234010083.png)

假如有一个线程执行到synchronized代码块的时候，JVM使用CAS操作把线程指针ID记录到Mark Word当中，并修改标偏向标示，标示当前线程就获得该锁。锁对象变成偏向锁（通过CAS修改对象头里的锁标志位），字面意思是“偏向于第一个获得它的线程”的锁。**执行完同步代码块后，线程并不会主动释放偏向锁。**

![image-20220501234106399](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220501234106399.png)

这时线程获得了锁，可以执行同步代码块。当该线程第二次到达同步代码块时会判断此时持有锁的线程是否还是自己（持有锁的线程ID也在对象头里），JVM通过account对象的Mark Word判断：当前线程ID还在，说明还持有着这个对象的锁，就可以继续进入临界区工作。由于之前没有释放锁，这里也就不需要重新加锁。 如果自始至终使用锁的线程只有一个，很明显偏向锁几乎没有额外开销，性能极高。

结论：JVM不用和操作系统协商设置Mutex(争取内核)，它只需要记录下线程ID就标示自己获得了当前锁，不用操作系统接入。

上述就是偏向锁：在没有其他线程竞争的时候，一直偏向偏心当前线程，当前线程可以一直执行。

> 偏向锁JVM命令

```
java -XX:+PrintFlagsInitial | grep BiasedLock*
```

![image-20220501234233748](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220501234233748.png)

![image-20220501234245422](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220501234245422.png)

```
* 实际上偏向锁在JDK1.6之后是默认开启的，但是启动时间有延迟，
* 所以需要添加参数-XX:BiasedLockingStartupDelay=0，让其在程序启动时立刻启动。
*
* 开启偏向锁：
* -XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0
*
* 关闭偏向锁：关闭之后程序默认会直接进入------------------------------------------>>>>>>>>   轻量级锁状态。
* -XX:-UseBiasedLocking
```

![image-20220501234340335](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220501234340335.png)





一切默认情况

```java
public class MyObject
{
    public static void main(String[] args)
    {
        Object o = new Object();

        new Thread(() -> {
            synchronized (o){
                System.out.println(ClassLayout.parseInstance(o).toPrintable());
            }
        },"t1").start();
    }
}
```

![image-20220501234404986](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220501234404986.png)

```
-XX:+UseBiasedLocking                       开启偏向锁(默认)           
-XX:-UseBiasedLocking                       关闭偏向锁
-XX:BiasedLockingStartupDelay=0             关闭延迟(演示偏向锁时需要开启)

参数说明：
偏向锁在JDK1.6以上默认开启，开启后程序启动几秒后才会被激活，可以使用JVM参数来关闭延迟 -XX:BiasedLockingStartupDelay=0
 
如果确定锁通常处于竞争状态则可通过JVM参数 -XX:-UseBiasedLocking 关闭偏向锁，那么默认会进入轻量级锁
```

关闭延时参数，启用立马开启偏向功能

````
-XX:BiasedLockingStartupDelay=0 
````

![image-20220501234522398](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220501234522398.png)

！！！ 如果 有第二个线程来抢夺了

> 偏向锁的撤销

当有另外线程来逐步竞争锁的时候，就不能再使用偏向锁了，要升级为轻量级锁

竞争线程尝试CAS更新对象头失败，会等待全局安全点（此时不会执行任何字节码） 才会撤销偏向锁

偏向锁的撤销

​	偏向锁使用一种等到竞争出现才释放锁的机制，只有当其他线程竞争锁时，持有偏向锁的原来线程才会被撤销。

​	撤销需要等待全局安全点(该时间点上没有字节码正在执行)，同时检查持有偏向锁的线程是否还在执行：

```
①  第一个线程正在执行synchronized方法(处于同步块)，它还没有执行完，其它线程来抢夺，该偏向锁会被取消掉并出现锁升级。
此时轻量级锁由原持有偏向锁的线程持有，继续执行其同步代码，而正在竞争的线程会进入自旋等待获得该轻量级锁。

②  第一个线程执行完成synchronized方法(退出同步块)，则将对象头设置成无锁状态并撤销偏向锁，重新偏向 。
```

![image-20220501234749486](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220501234749486.png)

