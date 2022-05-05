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

## 3、公平锁和非公平锁

#### 3.1 从ReentrantLock卖票编码演示公平和非公平现象

```java
class Ticket
{
    private int number = 30;
    ReentrantLock lock = new ReentrantLock();

    public void sale()
    {
        lock.lock();
        try
        {
            if(number > 0)
            {
                System.out.println(Thread.currentThread().getName()+"卖出第：\t"+(number--)+"\t 还剩下:"+number);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}

public class SaleTicketDemo
{
    public static void main(String[] args)
    {
        Ticket ticket = new Ticket();

        new Thread(() -> { for (int i = 0; i <35; i++)  ticket.sale(); },"a").start();
        new Thread(() -> { for (int i = 0; i <35; i++)  ticket.sale(); },"b").start();
        new Thread(() -> { for (int i = 0; i <35; i++)  ticket.sale(); },"c").start();
    }
}
```

#### 3.2 何为公平锁/非公平锁?

⽣活中，排队讲求先来后到视为公平。程序中的公平性也是符合请求锁的绝对时间的，其实就是 FIFO，否则视为不公平

==按序排队公平锁，就是判断同步队列是否还有先驱节点的存在(我前面还有人吗?)，==

==如果没有先驱节点才能获取锁；先占先得非公平锁，是不管这个事的，只要能抢获到同步状态就可以==

![image-20220410225524448](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220410225524448.png)

#### 3.3 面试题

> 为什么会有公平锁/非公平锁的设计为什么默认非公平？

1、恢复挂起的线程到真正锁获取还是有时间差的，从开发人员来看这个时间微乎其微，但是从cpu的角度来看，这个时间差还是非常明显的。所以==非公平锁能更充分的利用CPU 的时间片，尽量减少 CPU 空闲状态时间==。

2、使用多线程很重要的考量点是线程切换的开销，当采用非公平锁时，==当1个线程请求锁获取同步状态，然后释放同步状态，因为不需要考虑是否还有前驱节点，所以刚释放锁的线程在此刻再次获取同步状态的概率就变得非常大，所以就减少了线程的开销==

> 使用非公平锁有什么问题？

公平锁保证了排队的公平性，非公平锁霸气的忽视这个规则，所以就有可能导致排队的长时间在排队，也没有机会获取到锁，这就是传说中的 “锁饥饿”

> 什么时候用公平？什么时候用非公平？

如果为了更高的吞吐量，很显然非公平锁是比较合适的，因为节省很多线程切换时间，吞吐量自然就上去了；否则那就用公平锁，大家公平使用。

> （可重入锁）公平锁/非公平锁的基石AQS

![image-20220410230049140](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220410230049140.png)

![image-20220410230101169](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220410230101169.png)

## 4、可重入锁(又名递归锁)

==是指在同一个线程在外层方法获取锁的时候，再进入该线程的内层方法会自动获取锁(前提，锁对象得是同一个对象)，不会因为之前已经获取过还没释放而阻塞。==

如果是1个有 synchronized 修饰的递归调用方法，程序第2次进入被自己阻塞了岂不是天大的笑话，出现了作茧自缚。

所以Java中**ReentrantLock和synchronized都是可重入锁，可重入锁的一个优点是可一定程度避免死锁**。

#### 4.1 “可重入锁”这四个字分开来解释：

可：可以。

重：再次。

入：进入。 进入同步域（即同步代码块/方法或显式锁锁定的代码）

锁：同步锁。

==一个线程中的多个流程可以获取同一把锁，持有这把同步锁可以再次进入。自己可以获取自己的内部锁==

#### 4.2 可重入锁种类

隐式锁（即synchronized关键字使用的锁）默认是可重入锁

```JAVA
public class ReEntryLockDemo
{
    public static void main(String[] args)
    {
        final Object objectLockA = new Object();

        new Thread(() -> {
            synchronized (objectLockA)
            {
                System.out.println("-----外层调用");
                synchronized (objectLockA)
                {
                    System.out.println("-----中层调用");
                    synchronized (objectLockA)
                    {
                        System.out.println("-----内层调用");
                    }
                }
            }
        },"a").start();
    }
}

// 在一个Synchronized修饰的方法或代码块的内部调用本类的其他Synchronized修饰的方法或代码块时，是永远可以得到锁的
public class ReEntryLockDemo
{
    public synchronized void m1()
    {
        System.out.println("-----m1");
        m2();
    }
    public synchronized void m2()
    {
        System.out.println("-----m2");
        m3();
    }
    public synchronized void m3()
    {
        System.out.println("-----m3");
    }

    public static void main(String[] args)
    {
        ReEntryLockDemo reEntryLockDemo = new ReEntryLockDemo();

        reEntryLockDemo.m1();
    }
}
```

Synchronized的重入的实现机理（管程机制） and 为什么任何一个对象都可以成为一个锁

**每个锁对象拥有一个锁计数器和一个指向持有该锁的线程的指针。**

当执行monitorenter时，如果目标锁对象的计数器为零，那么说明它没有被其他线程所持有，Java虚拟机会将该锁对象的持有线程设置为当前线程，并且将其计数器加1。

在目标锁对象的计数器不为零的情况下，如果锁对象的持有线程是当前线程，那么 Java 虚拟机可以将其计数器加1，否则需要等待，直至持有线程释放该锁。

当执行monitorexit时，Java虚拟机则需将锁对象的计数器减1。计数器为零代表锁已被释放。



显式锁（即Lock）也有ReentrantLock这样的可重入锁。

```java
public class ReEntryLockDemo
{
    static Lock lock = new ReentrantLock();

    public static void main(String[] args)
    {
        new Thread(() -> {
            lock.lock();
            try
            {
                System.out.println("----外层调用lock");
                lock.lock();
                try
                {
                    System.out.println("----内层调用lock");
                }finally {
                    // 这里故意注释，实现加锁次数和释放次数不一样
                    // 由于加锁次数和释放次数不一样，第二个线程始终无法获取到锁，导致一直在等待。
                    lock.unlock(); // 正常情况，加锁几次就要解锁几次
                }
            }finally {
                lock.unlock();
            }
        },"a").start();

        new Thread(() -> {
            lock.lock();
            try
            {
                System.out.println("b thread----外层调用lock");
            }finally {
                lock.unlock();
            }
        },"b").start();

    }
}

```

## 5、死锁及排查

==死锁是指两个或两个以上的线程在执行过程中,因争夺资源而造成的一种互相等待的现象==,若无外力干涉那它们都将无法推进下去，如果系统资源充足，进程的资源请求都能够得到满足，死锁出现的可能性就很低，否则就会因争夺有限的资源而陷入死锁。

![image-20220410230637437](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220410230637437.png)

产生死锁主要原因：

- 系统资源不足
- 进程允许推进的顺序不合理
- 资源分配不当

请写一个死锁代码case

```java
public class DeadLockDemo
{
    public static void main(String[] args)
    {
        final Object objectLockA = new Object();
        final Object objectLockB = new Object();

        new Thread(() -> {
            synchronized (objectLockA)
            {
                System.out.println(Thread.currentThread().getName()+"\t"+"自己持有A，希望获得B");
                //暂停几秒钟线程
                try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
                synchronized (objectLockB)
                {
                    System.out.println(Thread.currentThread().getName()+"\t"+"A-------已经获得B");
                }
            }
        },"A").start();

        new Thread(() -> {
            synchronized (objectLockB)
            {
                System.out.println(Thread.currentThread().getName()+"\t"+"自己持有B，希望获得A");
                //暂停几秒钟线程
                try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
                synchronized (objectLockA)
                {
                    System.out.println(Thread.currentThread().getName()+"\t"+"B-------已经获得A");
                }
            }
        },"B").start();

    }
}
```

如何排查死锁

纯命令:

1、jps -l    2、jstack pid   

图形化：

jconsole 选择检测进程，左下角，检测死锁即可

## 6、写锁(独占锁)/读锁(共享锁)

## 7、自旋锁SpinLock

## 8、无锁→独占锁→读写锁→邮戳锁

#### 8.1 简单聊聊ReetrantReadWriteLock

> 什么是读写锁？

**读写锁定义为一个资源能够被多个读线程访问，或者被一个写线程访问，但是不能同时存在读写线程。**

![image-20220505230728450](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505230728450.png)

> 读写锁的意义和特点

『读写锁ReentrantReadWriteLock』**并不是真正意义上的读写分离，它只允许读读共存，而读写和写写依然是互斥的，**

大多实际场景是“读/读”线程间并不存在互斥关系，只有"读/写"线程或"写/写"线程间的操作需要互斥的。因此引入ReentrantReadWriteLock。

一个ReentrantReadWriteLock同时只能存在一个写锁但是可以存在多个读锁，但不能同时存在写锁和读锁(切菜还是拍蒜选一个)。也即一个资源可以被多个读操作访问或一个写操作访问，但两者不能同时进行。

只有在读多写少情境之下，读写锁才具有较高的性能体现。

#### 8.2 特点

- 可重入

- 读写分离

- 无锁无序--> 加锁---> 读写锁演变复习   

  code演示

  ```java
  class MyResource
  {
      Map<String,String> map = new HashMap<>();
      //=====ReentrantLock 等价于 =====synchronized
      Lock lock = new ReentrantLock();
      //=====ReentrantReadWriteLock 一体两面，读写互斥，读读共享
      ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
  
      public void write(String key,String value)
      {
          rwLock.writeLock().lock();
          try
          {
              System.out.println(Thread.currentThread().getName()+"\t"+"---正在写入");
              map.put(key,value);
              //暂停毫秒
              try { TimeUnit.MILLISECONDS.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
              System.out.println(Thread.currentThread().getName()+"\t"+"---完成写入");
          }finally {
              rwLock.writeLock().unlock();
          }
      }
      public void read(String key)
      {
          rwLock.readLock().lock();
          try
          {
              System.out.println(Thread.currentThread().getName()+"\t"+"---正在读取");
              String result = map.get(key);
              //后续开启注释修改为2000，演示一体两面，读写互斥，读读共享，读没有完成时候写锁无法获得
              //try { TimeUnit.MILLISECONDS.sleep(200); } catch (InterruptedException e) { e.printStackTrace(); }
              System.out.println(Thread.currentThread().getName()+"\t"+"---完成读取result："+result);
          }finally {
              rwLock.readLock().unlock();
          }
      }
  }
  
  
  public class ReentrantReadWriteLockDemo
  {
      public static void main(String[] args)
      {
          MyResource myResource = new MyResource();
  
          for (int i = 1; i <=10; i++) {
              int finalI = i;
              new Thread(() -> {
                  myResource.write(finalI +"", finalI +"");
              },String.valueOf(i)).start();
          }
  
          for (int i = 1; i <=10; i++) {
              int finalI = i;
              new Thread(() -> {
                  myResource.read(finalI +"");
              },String.valueOf(i)).start();
          }
  
          //暂停几秒钟线程
          try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
  
          //读全部over才可以继续写
          for (int i = 1; i <=3; i++) {
              int finalI = i;
              new Thread(() -> {
                  myResource.write(finalI +"", finalI +"");
              },"newWriteThread==="+String.valueOf(i)).start();
          }
      }
  }
  
  ```

  

- **从写锁--->读锁，ReetrantReadWriteLock可降级**

《java并发编程的艺术》中关于锁降级的说明：

![image-20220505231910792](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505231910792.png)

说人话：

锁降级：将写锁降级为读锁（类似Linux文件读写权限，写权限高于读权限一样）

读写锁降级演示

![image-20220505232203484](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505232203484.png)

锁降级是为了让当前线程感知到数据的变化，目的是保证数据可见性

```java
/**
 * 锁降级：遵循获取写锁→再获取读锁→再释放写锁的次序，写锁能够降级成为读锁。
 *
 * 如果一个线程占有了写锁，在不释放写锁的情况下，它还能占有读锁，即写锁降级为读锁。
 */
public class LockDownGradingDemo
{
    public static void main(String[] args)
    {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();


        writeLock.lock();
        System.out.println("-------正在写入");


        readLock.lock();
        System.out.println("-------正在读取");

        writeLock.unlock();

    }
}
 
```

**结论：如果有线程在读，那么写线程是无法获取写锁的，是悲观锁策略**

不可锁升级：

**线程获取读锁是不能直接升级为写入锁的。**

![image-20220505232608274](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505232608274.png)

写锁和读锁是互斥的

（这里的互斥是指线程间的互斥，当前线程可以获取到写锁又获取到读锁，但是获取到了读锁不能继续获取写锁）。**这是因为读写锁要保持写操作的可见性**。

因为，如果允许读锁在被获取的情况下对写锁的获取，那么正在运行的其他读线程无法感知到当前写线程的操作。

因此，

分析读写锁ReentrantReadWriteLock，会发现它有个潜在的问题：

==**读锁全完，写锁有望；写锁独占，读写全堵；**==

如果有线程正在读，写线程需要等待读线程释放锁后才能获取写锁。

**即ReadWriteLock读的过程中不允许写，只有等待线程都释放了读锁，当前线程才能获取写锁，**

**也就是写入必须等待，这是一种悲观的读锁，o(╥﹏╥)o，人家还在读着那，你先别去写，省的数据乱。**



分析StampedLock(后面详细讲解)，会发现它改进之处在于：

**读的过程中也允许获取写锁介入(相当牛B，读和写两个操作也让你“共享”(注意引号))，这样会导致我们读的数据就可能不一致！**

所以，需要额外的方法来判断**读的过程中是否有写入**，这是一种乐观的读锁，O(∩_∩)O哈哈~。 

显然乐观锁的并发效率更高，但一旦有小概率的写入导致读取的数据不一致，需要能检测出来，再读一遍就行。



读写锁之读写规矩，再说降级

ReetrantReadWriteLock源码总结：

锁降级  下面的示例代码摘自ReentrantWriteReadLock源码中：
ReentrantWriteReadLock支持锁降级，遵循按照获取写锁，获取读锁再释放写锁的次序，写锁能够降级成为读锁，不支持锁升级。
解读在最下面:

![image-20220505233502577](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505233502577.png)

1 、代码中声明了一个volatile类型的cacheValid变量，保证其可见性。

2、首先获取读锁，如果cache不可用，则释放读锁，获取写锁，在更改数据之前，再检查一次cacheValid的值，然后修改数据，将cacheValid置为true，然后在释放写锁前获取读锁；此时，cache中数据可用，处理cache中数据，最后释放读锁。这个过程就是一个完整的锁降级的过程，目的是保证数据可见性。

如果违背锁降级的步骤 ：

​	如果当前的线程C在修改完cache中的数据后，**没有获取读锁而是直接释放了写锁**，那么假设此时另一个线程D获取了写锁并修改了数据，那么C线程无法感知到	数据已被修改，则数据出现错误。

如果遵循锁降级的步骤 ：

​	线程C在释放写锁之前获取读锁，**那么线程D在获取写锁时将被阻塞，直到线程C完成数据处理过程，释放读锁。**这样可以保证返回的数据是这次更新的数据，该	机制是专门为了缓存设计的。



#### 8.3 邮戳锁

面试题：有没有比读写锁更快的锁

> 什么是邮戳锁？

StampedLock是JDK1.8中新增的一个读写锁，也是对JDK1.5中的读写锁ReetrantReadWriteLock的优化

stamp（戳记，long类型）代表了锁的状态。当Stamp返回零时，表示线程获取锁失败，并且，当释放锁或者转化锁的时候，都要传入最初获取的stamp值

> 它由锁饥饿的问题引出

锁饥饿问题：

ReentrantReadWriteLock实现了读写分离，但是一旦读操作比较多的时候，想要获取写锁就变得比较困难了，

假如当前1000个线程，999个读，1个写，有可能999个读取线程长时间抢到了锁，那1个写线程就悲剧了 

因为当前有可能会一直存在读锁，而无法获得写锁，根本没机会写

如何环境锁饥饿问题？

使用公平锁在一定程度上可以缓解这个问题，`new ReetrantReadWriteLock（true）`，但是公平是以牺牲吞吐量为代价的

StampedLock类的乐观读写锁登场

ReentrantReadWriteLock，允许多个线程同时读，但是只允许一个线程写，在线程获取到写锁的时候，其他写操作和读操作都会处于阻塞状态， 

==读锁和写锁也是互斥的==，所以在读的时候是不允许写的，读写锁比传统的synchronized速度要快很多，**原因就是在于ReentrantReadWriteLock支持读并发**



StampedLock横空出世

ReentrantReadWriteLock的读锁被占用的时候，其他线程尝试获取写锁的时候会被阻塞。

但是，StampedLock采取乐观获取锁后，**其他线程尝试获取写锁时不会被阻塞**，这其实是对读锁的优化，

所以，在获取乐观读锁后，还需要对结果进行校验。

> 邮戳锁的特点

![image-20220505234501520](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220505234501520.png)



```java
public class StampedLockDemo
{
    static int number = 37;
    static StampedLock stampedLock = new StampedLock();

    public void write()
    {
        long stamp = stampedLock.writeLock();
        System.out.println(Thread.currentThread().getName()+"\t"+"=====写线程准备修改");
        try
        {
            number = number + 13;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            stampedLock.unlockWrite(stamp);
        }
        System.out.println(Thread.currentThread().getName()+"\t"+"=====写线程结束修改");
    }

    //悲观读
    public void read()
    {
        long stamp = stampedLock.readLock();
        System.out.println(Thread.currentThread().getName()+"\t come in readlock block,4 seconds continue...");
        //暂停几秒钟线程
        for (int i = 0; i <4 ; i++) {
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println(Thread.currentThread().getName()+"\t 正在读取中......");
        }
        try
        {
            int result = number;
            System.out.println(Thread.currentThread().getName()+"\t"+" 获得成员变量值result：" + result);
            System.out.println("写线程没有修改值，因为 stampedLock.readLock()读的时候，不可以写，读写互斥");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            stampedLock.unlockRead(stamp);
        }
    }

    //乐观读
    public void tryOptimisticRead()
    {
        long stamp = stampedLock.tryOptimisticRead();
        int result = number;
        //间隔4秒钟，我们很乐观的认为没有其他线程修改过number值，实际靠判断。
        System.out.println("4秒前stampedLock.validate值(true无修改，false有修改)"+"\t"+stampedLock.validate(stamp));
        for (int i = 1; i <=4 ; i++) {
            try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println(Thread.currentThread().getName()+"\t 正在读取中......"+i+
                    "秒后stampedLock.validate值(true无修改，false有修改)"+"\t"
                    +stampedLock.validate(stamp));
        }
        if(!stampedLock.validate(stamp)) {
            System.out.println("有人动过--------存在写操作！");
            stamp = stampedLock.readLock();
            try {
                System.out.println("从乐观读 升级为 悲观读");
                result = number;
                System.out.println("重新悲观读锁通过获取到的成员变量值result：" + result);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                stampedLock.unlockRead(stamp);
            }
        }
        System.out.println(Thread.currentThread().getName()+"\t finally value: "+result);
    }

    public static void main(String[] args)
    {
        StampedLockDemo resource = new StampedLockDemo();

        new Thread(() -> {
            resource.read();
            //resource.tryOptimisticRead();
        },"readThread").start();

        // 2秒钟时乐观读失败，6秒钟乐观读取成功resource.tryOptimisticRead();，修改切换演示
        //try { TimeUnit.SECONDS.sleep(6); } catch (InterruptedException e) { e.printStackTrace(); }

        new Thread(() -> {
            resource.write();
        },"writeThread").start();
    }
}
```

> stampedLock的缺点

- stampedLock不支持重入，没有Re开头，容易造成死锁
- stampedLock的悲观读锁和写锁都不支持条件变量Condition
- 使用stampedLock一定不要调用中断操作，即不要调用Interrupt()方法
- 如果要支持中断功能，一定使用可中断的悲观读锁readLockInterruptibly()和写锁writeLockInterruptibly()



## 9、无锁→偏向锁→轻量锁→重量锁

锁升级后面有讲述

其它细节 --》 不可以String同一把锁 --》严禁这么做 

```java
String a = "adc";
String b = "adc";

synchronized(a)
synchronized(b)    

    // 这样是严禁这么做的，因为String默认是个常量，存放到方法区中的常量池，a b 只是指向了同一个引用地址，加锁以后会锁的是同一个东西，很容易死锁。
```

