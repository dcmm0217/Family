# 第3节-Java中的锁

## 1、乐观锁和悲观锁

#### 1.1 悲观锁

悲观锁认为自己在使用数据的时候一定有别的线程来修改数据，因此在获取数据的时候会先加锁，确保数据不会被别的线程修改。

**synchronized关键字和Lock的实现类都是悲观锁**

使用场景：适合写多读少的场景，先加锁确保写操作时数据正确，显式锁定后再操作同步资源。

#### 1.2 乐观锁

乐观锁认为自己在使用数据时==不会有别的线程修改数据==，所以不会加锁，只是在更新数据的时候去判断之前有没有别的线程更新了这个数据。

如果这个数据没有被更新，当前线程将自己的修改的数据成功写入。如果数据已经被其他线程修改，则根据不同实现方式去执行不同的操作。

乐观锁在Java中是通过无锁编程来实现，**最常采用的是CAS算法，Java原子类中的递增操作就是通过CAS自旋实现的。**

使用场景：适合读操作多的场景，不加锁的特点能够使其读操作的性能大幅提升。乐观锁则直接去操作同步资源，是一种无锁算法，得之我幸不得我命，再抢

乐观锁一般有两种实现方式：

1、采用版本号机制

2、CAS（Compare-and-Swap，即比较并替换）算法实现

## 2、8种锁现象案例演示

#### 2.1 看看JVM中对应的锁在哪里？

![image-20220407201335867](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220407201335867.png)

#### 2.2 synchronized有三种应用方式

8种锁的案例实际体现在3个地方

- 作用于实例方法，当前实例加锁，进入同步代码前要获得当前实例的锁；
- 作用于代码块，对括号里配置的对象加锁。
- 作用于静态方法，当前类加锁，进去同步代码前要获得当前类对象的锁；

```java
public class SyncDemo1{
    public static void main(String[] args){
        Phone phone1 = new Phone();
        Phone phone2 = new Phone();
        
        new Thread(()->{
            phone1.sendSMS();
        },"A").start()
            
        new Thread(()->{
            phone1.sendEmail();
        },"B").start()    
            
    }
}

class phone {
    public synchronized void sendSMS(){
       System.out.println(" snedSMS ");
    }
    
    public synchronized void sendEmail(){
        System.out.println(" sendEmail ");
    }
}
```

#### 2.3 从字节码角度分析synchronized实现

javap -c ***.class文件反编译 

-  -c      对代码进行反汇编
- javap -v ***.class文件反编译     -v  -verbose             输出附加信息（包括行号、本地变量表，反汇编等详细信息）

> synchronized同步代码块

javap -c ***.class文件反编译

![image-20220407224039773](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220407224039773.png)

22行那个monitorexit 是为了有异常的时候 保证锁能释放，16行那个是正常释放锁的字节码指令。

> synchronized同步代码块

实现使用的是monitorenter和monitorexit指令

一定是一个enter两个exit吗？

m1方法里面自己添加一个异常试试

![image-20220407224201942](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220407224201942.png)

> synchronized普通同步方法

javap -v ***.class文件反编译

![image-20220407224229712](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220407224229712.png)

synchronized普通同步方法

调用指令将会检查方法的ACC_SYNCHRONIZED访问标志是否被设置。

如果设置了，执行线程会将先持有monitor然后再执行方法，最后在方法完成(无论是正常完成还是非正常完成)时释放 monitor

> synchronized静态同步方法

javap -v ***.class文件反编译

![image-20220407224304597](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220407224304597.png)

synchronized静态同步方法

ACC_STATIC, ACC_SYNCHRONIZED访问标志区分该方法是否静态同步方法

#### 2.4 反编译synchronized锁的是什么

面试题：synchronized实现原理，monitor对象是什么时候生成的？知道monitor的monitorenter和moniterexit这两个是如何保证同步的吗？或者说，这两个操作计算机底层的时候是如何执行的。

**什么是管程monitor?**

为什么任何一个对象都可以成为一个锁

管程 (英语：Monitors，也称为监视器) 是一种程序结构，结构内的多个子程序（对象或模块）形成的多个工作线程互斥访问共享资源。
这些共享资源一般是硬件设备或一群变量。对共享变量能够进行的所有操作集中在一个模块中。（把信号量及其操作原语“封装”在一个对象内部）管程实现了在一个时间点，最多只有一个线程在执行管程的某个子程序。管程提供了一种机制，管程可以看做一个软件模块，它是将共享的变量和对于这些共享变量的操作封装起来，形成一个具有一定接口的功能模块，进程可以调用管程来实现进程级别的并发控制。

![image-20220407224525806](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220407224525806.png)

在HotSpot虚拟机中，monitor采用ObjectMonitor实现

上述C++源码解读

ObjectMonitor.java→ObjectMonitor.cpp→objectMonitor.hpp

objectMonitor.hpp

![image-20220407224611759](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220407224611759.png)

**每个对象天生都带着一个对象监视器**

对于synchronized关键字，我们在《Synchronized与锁升级》章节还会再深度讲解

synchronized必须作用于某个对象中，所以**Java在对象的头文件存储了锁的相关信息**。

锁升级功能主要依赖于==MarkWord 中的锁标志位和释放偏向锁标志位==，后续讲解锁升级时候我们再加深，目前为了承前启后的学习，对下图先混个眼熟即可

![image-20220407224705627](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220407224705627.png)

