# 第10节-JUC之基石AQS

面试题：

- ReetrantLock实现原理，简单说下AQS吧

AQS的前置知识：

- 公平锁和非公平锁
- 可重入锁
- 自旋锁
- LockSupport
- 数据结构---链表
- 设计模式之模板方法

## 1、什么是AQS

全称为：AbstractQueueSynchronizer  翻译为：抽象的队列同步器

![image-20220505200115115](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505200115115.png)

技术解释：

是通过构建锁或者其他同步器组件的**重量级基础框架及整个JUC体系的基石**，通过内置的FIFO队列，来完成资源获取线程的排队工作，并通过一个int类型变量。表示持有锁的状态。（state）

![image-20220505200325795](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505200325795.png)

## 2、AQS为什么重要

![image-20220505200459202](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505200459202.png)

ReetrantLock、CountDownLatch、Semaphore等底层都是AQS

![image-20220505200530352](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505200530352.png)

> 进一步理解 锁和同步器的关系

锁，面向锁的使用者，定义了程序员和锁交互的API，隐藏了锁实现的细节，你调用即可。

同步器，面向锁的实现者，比如DougLee，提出了统一规范并简化了锁的实现，屏蔽了同步状态管理、阻塞线程排队通知、唤醒机制等。

## 3、AQS能干嘛？

**加锁会导致阻塞，有阻塞就需要排队，实现排队必然需要队列** 

1、抢到资源的线程直接使用处理业务，抢不到资源的必然涉及一种**排队等候机制**。

2、抢占资源失败的线程继续去等待(类似银行业务办理窗口都满了，暂时没有受理窗口的顾客只能去候客区排队等候)，

3、但等候线程仍然保留获取锁的可能且获取锁流程仍在继续(候客区的顾客也在等着叫号，轮到了再去受理窗口办理业务)。

既然说到了排队等候机制，那么就一定会有某种队列形成，这样的队列是什么数据结构呢？

如果共享资源被占用，==**就需要一定的阻塞等待唤醒机制来保证锁分配。**==这个机制主要用的是CLH队列的变体实现的，将暂时获取不到锁的线程加入到队列中，这个队列就是AQS的抽象表现。

**它将请求共享资源的线程封装成队列的结点（Node），通过CAS、自旋以及LockSupport.park()的方式，维护state变量的状态，使并发达到同步的效果。**



## 4、AQS初步认识

官网解释：

![image-20220505201427413](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505201427413.png)

有阻塞就必然需要排队，要排队就必然需要队列

**==AQS使用一个volatile的int类型的成员变量来表示同步状态，通过内置的FIFO队列来完成资源获取的排队工作，每条要去抢占资源的线程封装成一个Node节点来实现锁的分配，通过CAS完成对state值得修改==**

![image-20220505202030323](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505202030323.png)

> AQS的内部体系架构

![image-20220505202151984](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505202151984.png)

#### 4.1 AQS自身

**AQS的 volatile int型变量**  ----> AQS同步状态state成员变量    ----->  private volatile int state;

类比成  银行业务办理窗口 ------->  0 ：就是没人，自由状态，可以去办理            >=1：有人占用窗口，等待

**AQS的CLH队列**

CLH队列（由三个大牛的名字组成），成为一个双向队列

![image-20220505211155384](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505211155384.png)

类比成：在银行候客区的等待顾客

**小总结：**

**有阻塞就需要排队，实现排队必然需要队列**

AQS的本质就是 =  state变量  +  CLH的双端队列



#### 4.2 内部类Node（Node类在AQS内部）

Node的int 变量：

​	Node的等待状态waitState成员变量   volatile int waitStatus;

​	说人话：等候区其他顾客（其他线程）的等待状态，队列中每一个排队的个体就是一个Node

Node类讲解：

​	内部结构：

```java
static final class Node {
        /** Marker to indicate a node is waiting in shared mode */
        static final Node SHARED = new Node();
        /** Marker to indicate a node is waiting in exclusive mode */
        static final Node EXCLUSIVE = null;

        /** waitStatus value to indicate thread has cancelled */
        static final int CANCELLED =  1;
        /** waitStatus value to indicate successor's thread needs unparking */
        static final int SIGNAL    = -1;
        /** waitStatus value to indicate thread is waiting on condition */
        static final int CONDITION = -2;
        /**
         * waitStatus value to indicate the next acquireShared should
         * unconditionally propagate
         */
        static final int PROPAGATE = -3;

        
        volatile int waitStatus;

       
        volatile Node prev;

        
        volatile Node next;

        /**
         * The thread that enqueued this node.  Initialized on
         * construction and nulled out after use.
         */
        volatile Thread thread;

       
        Node nextWaiter;

        final boolean isShared() {
            return nextWaiter == SHARED;
        }

      
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {    // Used to establish initial head or SHARED marker
        }

        Node(Thread thread, Node mode) {     // Used by addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // Used by Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }
```

![image-20220505212327768](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505212327768.png)

属性说明：

![image-20220505212346040](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505212346040.png)

#### 4.3 AQS同步队列的基本结构

![image-20220505213333443](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505213333443.png)



## 5、从ReetrantLock解读AQS源码

1、Lock接口的实现类，基本都是通过【聚合】了一个【队列同步器】的子类完成线程访问控制的

2、ReetrantLock原理

![image-20220505213643035](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505213643035.png)

3、从最简单的Lock方法开始看公平锁和非公平锁

![image-20220505213758429](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505213758429.png)

![image-20220505213845057](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505213845057.png)

**可以明显看出公平锁与非公平锁的lock()方法唯一的区别就在于公平锁在获取同步状态时多了一个限制条件：**

**hasQueuedPredecessors()**

**hasQueuedPredecessors是公平锁加锁时判断等待队列中是否存在有效节点的方法**

4、细看非公平的lock()方法

对比公平锁和非公平锁的 tryAcquire()方法的实现代码，其实差别就在于非公平锁获取锁时比公平锁中少了一个判断 !hasQueuedPredecessors()

hasQueuedPredecessors() 中判断了是否需要排队，导致公平锁和非公平锁的差异如下：

​	公平锁：公平锁讲究先来先到，线程在获取锁时，如果这个锁的等待队列中已经有线程在等待，那么当前线程就会进入等待队列中；

​	非公平锁：不管是否有等待队列，如果可以获取锁，则立刻占有锁对象。也就是说队列的第一个排队线程在unpark()，之后还是需要竞争锁（存在线程竞争的情况下）

![image-20220505214419923](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505214419923.png)

源码解读：

lock（）：

![image-20220505214649275](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505214649275.png)

acquire（）：

源码和3大流程走向

![image-20220505221839137](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505221839137.png) 

tryAcquire()：

本次走非公平锁

![image-20220505221948711](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505221948711.png)

return false ： 继续推进，走下一个方法

return true：结束



addWaiter（Node.EXCLUSIVE）：

​	addWaiter(Node mode)

![image-20220505223357558](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505223357558.png)

​	enq（node）：

​		![image-20220505223522805](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505223522805.png)

双向链表中，**第一个节点为虚节点（也叫哨兵节点），其实并不存储任何信息，只是占位**。真正的第一个有数据的节点，是从第二个节点开始的。

假如3号ThreadC线程进来：

​	prev、compareAndSetTail、next

acquireQueued（addWaiter(Node.EXCLUDSIVE),args）

​	acquireQueued

![image-20220505224337982](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505224337982.png)

假设再抢抢失败就会进入

shouldParkAfterFailedAcquire和parkAndCheckInterrupt方法中

![image-20220505224536135](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505224536135.png)

shouldParkAfterFaildedAcquire ：如果前驱节点的waitStatus是Single状态，即shouldParkAfterFailedAcquire方法会返回true，程序会继续向下执行parkAndCheckInterrupt方法，用于将当前线程挂起

![image-20220505224605525](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505224605525.png)

parkAndCheckInterrupt：

![image-20220505224824599](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505224824599.png)

unlock（）：

sync.release(1)

tryRelease(arg)

unparkSuccessor

