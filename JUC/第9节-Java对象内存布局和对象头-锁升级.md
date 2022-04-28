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

