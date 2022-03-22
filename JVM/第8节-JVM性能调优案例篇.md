# 第8节-JVM性能调优案例篇

## 1、调优概述

### 1.1 生产中的问题

1、生产环境发生了内存溢出该如何处理？

2、生产环境应该给服务器分配多少内存合适？

3、如何对垃圾回收器的性能进行调优？

4、生产环境CPU负载飙高该如何处理？

5、生产环境应该给应用分配多少线程合适？

6、不加log，如何确定请求是否执行了某一行代码？

7、不加log，如何实时查看某个方法的入参与返回值？

### 1.2 调优基本问题

#### 1、为什么要调优

- 防止出现OOM，进行JVM规划和预调优
- 解决程序运行中各种OOM
- 减少Full GC出现的频率，解决运行慢，卡顿问题

#### 2、调优的大方向

- 合理编些代码
- 充分并合理的使用硬件资源
- 合理的进行JVM调优

#### 3、不同阶段的考虑

- 上线前，做Jemeter压测
- 项目运行，开启GC日志，以及内存dump
- 线上出现OOM，迅速定位问题以及解决问题

#### 4、两句话

- 调优，从业务场景开始，没有业务场景的调优都是耍流氓
- ==无监控，不调优==

### 1.3 调优监控依据

- 运行日志
- 异常堆栈
- GC日志
- 线程快照
- 堆转储快照dump

### 1.4 性能优化步骤

#### 1、熟悉业务场景

#### 2、发现问题：性能监控

- GC频繁
- CPU load过高
- OOM
- 内存泄漏
- 死锁
- 程序响应时间长

#### 3、排查问题：性能分析

- 打印GC日志，通过GCViewer或者http://gceasy.io来分析日志信息
- 灵活使用工具，命令行 jstat,jsp,jinfo,jstack,jmap等
- dump出堆文件，使用内存分析工具分析文件
- 使用阿里Arthas，或jconsole，JVisualVM来实时查看JVM状态
- jstack查看堆栈信息

#### 4、解决问题：性能调优

- 适当增加内存，根据业务背景选择垃圾回收器
- 优化代码，控制内存使用
- 增加机器，分散节点压力
- 合理设置线程池线程数量
- 使用中间件提高程序效率，比如缓存，消息队列等

### 1.5 性能评价/测试指标

1、停顿时间（响应时间）

2、吞吐量

3、并发数

4、内存占用

5、相互间的关系

## 2、OOM案例

### 2.1 案例1：堆溢出

报错信息：`java.lang.OutOfMemoryError: Java heap space`

案例模拟代码

```java
/**
     * 案例1：模拟线上环境OOM
     */
@RequestMapping("/add")
public void addObject(){
    System.err.println("add"+peopleSevice);
    ArrayList<People> people = new ArrayList<>();
    while (true){
        people.add(new People());
    }
}
```

JVM参数配置

```
-XX:+PrintGCDetails -XX:MetaspaceSize=64m -XX:+HeapDumpOnOutOfMemoryError  -XX:HeapDumpPath=heap/heapdump.hprof -XX:+PrintGCDateStamps -Xms50M  -Xmx50M  -Xloggc:log/gc-oomHeap.log
```

运行程序，调用接口

```
http://localhost:8080/add
```

运行结果：

```
运行结果：
java.lang.OutOfMemoryError: Java heap space
  at java.util.Arrays.copyOf(Arrays.java:3210) ~[na:1.8.0_131]
  at java.util.Arrays.copyOf(Arrays.java:3181) ~[na:1.8.0_131]
  at java.util.ArrayList.grow(ArrayList.java:261) ~[na:1.8.0_131]
  at java.util.ArrayList.ensureExplicitCapacity(ArrayList.java:235) ~[na:1.8.0_131]
  at java.util.ArrayList.ensureCapacityInternal(ArrayList.java:227) ~[na:1.8.0_131]
```

运行程序得到 heapdump.hprof 文件以及GC日志。如下图所示：

![image-20220318233715874](https://gitee.com/huangwei0123/image/raw/master/img/image-20220318233715874.png)

![image-20220318233744325](https://gitee.com/huangwei0123/image/raw/master/img/image-20220318233744325.png)

**出现原因：**

1、代码中可能存在大对象分配

2、可能存在内存泄漏，导致在多次GC之后，还是无法找到一块足够大的内存容纳当前对象。



**解决办法：**

==1、检查是否存在大对象的分配，最有可能的拾大数组分配。==

2、通过jmap命令，把堆内存dump下来，使用jvisualvm等工具分析一下，检查是否存在内存泄漏的问题

3、如果没有找到明显的内存泄漏，使用-Xmx 调整堆内存最大参数（加大堆内存）

4、还有一点容易被忽略，检查是否有大量的自定义的Finalizable对象，也有可能拾框架内部提供的，考虑其存在的必要性。（Finalizable对象，这个对象不可达了，会放到Finalizalier的队列中去执行，调用Finaliza()方法）==存在大量的明明不可达，但是还未被回收的对象，可能会造成堆内存溢出。==

**dump文件分析**

jvisualvm分析

- 接下来我们使用工具打开该文件，由于我们当前设置的内存比较小，所以该文件比较小，但是正常在线上环境，该文件是比较大的，通常是以G为单位。
- jvisualvm工具分析堆内存文件heapdump.hprof：

![image-20220318234535771](https://gitee.com/huangwei0123/image/raw/master/img/image-20220318234535771.png)

![image-20220318234555789](https://gitee.com/huangwei0123/image/raw/master/img/image-20220318234555789.png)

- 通过jvisualvm工具查看，占用最多实例的类是哪个，这样就可以定位到我们的问题所在了。

![image-20220318234655175](https://gitee.com/huangwei0123/image/raw/master/img/image-20220318234655175.png)

gc日志分析

![image-20220318234737695](https://gitee.com/huangwei0123/image/raw/master/img/image-20220318234737695.png)

### 2.2 案例2：元空间溢出

方法区（Method Area）与 Java 堆一样，是各个线程共享的内存区域，它用于存储已被虚拟机加载的类信息、常量、即时编译器编译后的代码等数据。虽然Java 虚拟机规范把方法区描述为堆的一个逻辑部分，但是它却有一个别名叫做 Non-Heap（非堆），目的应该是与 Java 堆区分开来。

Java 虚拟机规范对方法区的限制非常宽松，除了和 Java 堆一样不需要连续的内存和可以选择固定大小或者可扩展外，还可以选择不实现垃圾收集。垃圾收集行为在这个区域是比较少出现的，==其内存回收目标主要是针对常量池的回收和对类型的卸载==。当方法区无法满足内存分配需求时，将抛出 OutOfMemoryError 异常。

报错信息

`java.lang.OutOfMemoryError: Metaspace`

**案例模拟**

案例代码

```java
/**
     * 案例2:模拟元空间OOM溢出
     */
    @RequestMapping("/metaSpaceOom")
    public void metaSpaceOom(){
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        while (true){
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(People.class);
            enhancer.setUseCache(false);
//            enhancer.setUseCache(true);
            enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
                System.out.println("我是加强类，输出print之前的加强方法");
                return methodProxy.invokeSuper(o,objects);
            });
            People people = (People)enhancer.create();
            people.print();
            System.out.println(people.getClass());
            System.out.println("totalClass:" + classLoadingMXBean.getTotalLoadedClassCount());
            System.out.println("activeClass:" + classLoadingMXBean.getLoadedClassCount());
            System.out.println("unloadedClass:" + classLoadingMXBean.getUnloadedClassCount());
        }
    }
```

JVM参数设置

```
-XX:+PrintGCDetails -XX:MetaspaceSize=60m -XX:MaxMetaspaceSize=60m -Xss512K -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=heap/heapdumpMeta.hprof  -XX:SurvivorRatio=8 -XX:+TraceClassLoading -XX:+TraceClassUnloading -XX:+PrintGCDateStamps  -Xms60M  -Xmx60M -Xloggc:log/gc-oomMeta.log
```

发送请求

```
http://localhost:8080/metaSpaceOom
```

**原因及解决方案**

JDK8后，元空间替换了永久代，元空间使用的是本地内存

**原因：**

**1、运行期间生成了大量的代理类，导致方法区被撑爆，无法卸载**

**2、应用长时间运行，没有重启**

**3、元空间内存设置过小**

解决办法：

因为该OOM原因比较简单，解决办法如下几种：

==1、检查是否永久代或者元空间设置的过小==

==2、检查代码中是否存在大量反射操作==

==3、dump之后通过jvisualvm检查是否存在大量由于反射生成的代理类==

**分析及其及解决**

1、查看监控

![image-20220319001407503](https://gitee.com/huangwei0123/image/raw/master/img/image-20220319001407503.png)

2、查看GC状态

```
jstat -gc 线程id 1000 10 (1s一次，打印10次)

S0C：第一个幸存区的大小
S1C：第二个幸存区的大小
S0U：第一个幸存区的使用大小
S1U：第二个幸存区的使用大小
EC：伊甸园区的大小
EU：伊甸园区的使用大小
OC：老年代大小
OU：老年代使用大小
MC：方法区大小
MU：方法区使用大小
CCSC:压缩类空间大小
CCSU:压缩类空间使用大小
YGC：年轻代垃圾回收次数
YGCT：年轻代垃圾回收消耗时间
FGC：老年代垃圾回收次数
FGCT：老年代垃圾回收消耗时间
GCT：垃圾回收消耗总时间
```

![image-20220319001711436](https://gitee.com/huangwei0123/image/raw/master/img/image-20220319001711436.png)

可以看到，==FullGC 非常频繁==，而且我们的方法区，占用了59190KB/1024 = 57.8M空间，==几乎把整个方法区空间占用，所以得出的结论是方法区空间设置过小，或者存在大量由于反射生成的代理类==。

3、查看GC日志

可以看到FullGC是由于方法区空间不足引起的，那么我们接下来分析到底是什么数据占用了大量的方法区。

![image-20220319002053360](https://gitee.com/huangwei0123/image/raw/master/img/image-20220319002053360.png)

4、分析dump文件

导出dump文件，使用jvisualvm工具分析之：

![image-20220319002119059](https://gitee.com/huangwei0123/image/raw/master/img/image-20220319002119059.png)

对应的：

![image-20220319002151315](https://gitee.com/huangwei0123/image/raw/master/img/image-20220319002151315.png)

5、解决方法

那么我们可以想一下解决方案，每次是不是可以只加载一个代理类即可，

==因为我们的需求其实是没必要如此加载的，当然如果业务上确实需要加载很多类的话，那么我们就要考虑增大方法区的大小了==

所以我们在这里修改代码如下

```java
enhancer.setUseCache(true);
```

enhancer.setUseCache(false)

选择为true的话，==使用和更新一类具有相同属性生成的类的静态缓存，**而不会在同一个类文件还继续被动态加载并视为不同的类**，这个其实跟类的equals()和hashCode()有关，它们是与cglib内部的class cache的key相关的==。再看程序运行结果如下：

```
我是加强类哦，输出print之前的加强方法
我是print本人
class com.atguiigu.jvmdemo.bean.People$$EnhancerByCGLIB$$6ef22046
totalClass:6901
activeClass:6901
我是加强类哦，输出print之前的加强方法
我是print本人
class com.atguiigu.jvmdemo.bean.People$$EnhancerByCGLIB$$6ef22046
totalClass:6901
activeClass:6901+
```

可以看到，几乎不变了，方法区也没有溢出。到此，问题基本解决，再就是把while循环去掉。

### 2.3 案例3：GC overhead limit exceeded（超出 GC 开销限制）

实例代码：

```java
public class OOMTest {
    public static void main(String[] args) {
        test1();

//        test2();
    }

    // 因为String 对象一直被添加到list结合里面，有引用指向，一直回收不掉。 报GC overhead limit exceeded
    public static void test1() {
        int i = 0;
        List<String> list = new ArrayList<>();
        try {
            while (true) {
                list.add(UUID.randomUUID().toString().intern());
                i++;
            }
        } catch (Throwable e) {
            System.out.println("************i: " + i);
            e.printStackTrace();
            throw e;
        }
    }

    // String对象一直被创建，但是重新赋值后，老的对象没有引用指向它了，所以会被GC回收。最后报 Java heap space 堆溢出
    public static void test2() {
        String str = "";
        Integer i = 1;
        try {
            while (true) {
                i++;
                str += UUID.randomUUID();
            }
        } catch (Throwable e) {
            System.out.println("************i: " + i);
            e.printStackTrace();
            throw e;
        }
    }

}
```

JVM参数设置

```
-XX:+PrintGCDetails  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=heap/dumpExceeded.hprof -XX:+PrintGCDateStamps  -Xms10M  -Xmx10M -Xloggc:log/gc-oomExceeded.log
```

报错信息

```
[Full GC (Ergonomics) [PSYoungGen: 2047K->2047K(2560K)] [ParOldGen: 7110K->7095K(7168K)] 9158K->9143K(9728K), [Metaspace: 3177K->3177K(1056768K)], 0.0479640 secs] [Times: user=0.23 sys=0.01, real=0.05 secs] 

java.lang.OutOfMemoryError: GC overhead limit exceeded

[Full GC (Ergonomics) [PSYoungGen: 2047K->2047K(2560K)] [ParOldGen: 7114K->7096K(7168K)] 9162K->9144K(9728K), [Metaspace: 3198K->3198K(1056768K)], 0.0408506 secs] [Times: user=0.22 sys=0.01, real=0.04 secs] 
```

通过查看GC日志可以发现，==系统在频繁性的做FULL GC，但是却没有回收掉多少空间，那么引起的原因可能是因为内存不足，也可能是存在内存泄漏的情况，接下来我们要根据堆dump文件来具体分析==。

代码解析：

**第一段代码：运行期间将 内容放入常量池的典型案例**

intern()方法

- 如果字符串常量池里面已经包含了等于字符串X的字符串，那么就返回常量池中这个对象的引用
- 如果常量池中不存在，那么就会把当前字符串加入到常量池，并返回这个对象的引用



**第二段代码：不停的追加String**

你可能会疑惑，看似demo也没有差太多，为什么第二个没有报GC overhead limit exceeded呢？以上两个demo的区别在于：

- ==Java heap space 的demo每次都能回收大部分的对象（中间产生的UUID），只有一个对象是无法回收的，慢慢长大，知道内存溢出==
- ==GC overhead limit exceeded的demo由于每个字符串都在被list引用，所以无法回收，很快就用完内存，触发不断回收的机制。==



**分析和解决**

1、定位问题以及代码块

jvisualvm分析：

![image-20220319203330926](https://gitee.com/huangwei0123/image/raw/master/img/image-20220319203330926.png)

![image-20220319203344010](https://gitee.com/huangwei0123/image/raw/master/img/image-20220319203344010.png)

**这里就定位到了具体的线程中具体出现问题的代码的位置，进而进行优化即可。**

2、分析dump文件

如上图

看到发生OOM是因为进行了死循环，不停的往 ArrayList 存放字符串常量，==JDK1.8以后，字符串常量池移到了堆中存储==，所以最终导致内存不足发生了OOM。

打开Histogram，可以看到，==String类型的字符串占用了大概8M的空间，几乎把堆占满，但是还没有占满，所以这也符合Sun 官方对此的定义==：

**超过98%的时间用来做GC并且回收了不到2%的堆内存时会抛出此异常，本质是一个预判性的异常，抛出该异常时系统没有真正的内存溢出。**

3、代码修改

根据业务来修改是否需要死循环。（最好任何业务代码都不要出现死循环操作）

原因：==这个是JDK6新加的错误类型，一般都是堆太小导致的。==

Sun 官方对此的定义：超过98%的时间用来做GC并且回收了不到2%的堆内存时会抛出此异常。本质是一个预判性的异常，抛出该异常时系统没有真正的内存溢出

解决方法：

1. 检查项目中是否有大量的死循环或有使用大内存的代码，优化代码。
2. 添加参数 `-XX:-UseGCOverheadLimit` 禁用这个检查，其实这个参数解决不了内存问题，只是把错误的信息延后，最终出现 java.lang.OutOfMemoryError: Java heap space。
3. dump内存，检查是否存在内存泄漏，如果没有，加大内存。



### 2.4 案例4：线程溢出

1、报错信息

`java.lang.OutOfMemoryError : unable to create new native Thread`

2、问题原因

出现这种异常，基本上都是创建了大量的线程导致的

3、案例模拟

```java
/**
 * 测试4：线程溢出
 * @author shkstart
 * @create 17:45
 */
public class TestNativeOutOfMemoryError {
    public static void main(String[] args) {
        for (int i = 0; ; i++) {
            System.out.println("i = " + i);
            new Thread(new HoldThread()).start();
        }
    }
}

class HoldThread extends Thread {
    CountDownLatch cdl = new CountDownLatch(1);

    @Override
    public void run() {
        try {
            cdl.await();
        } catch (InterruptedException e) {
        }
    }
}
```

运行结果

```
i = 15241
Exception in thread "main" java.lang.OutOfMemoryError: unable to create new native thread
        at java.lang.Thread.start0(Native Method)
        at java.lang.Thread.start(Thread.java:717)
        at TestNativeOutOfMemoryError.main(TestNativeOutOfMemoryError.java:9)
```

![image-20220319210121126](https://gitee.com/huangwei0123/image/raw/master/img/image-20220319210121126.png)

4、分析解决

- 通过 -Xss 设置每个线程栈大小的容量
- JDK5.0以后每个线程堆栈大小为1M,以前每个线程堆栈大小为256K。
- 正常情况下，在相同物理内存下，减小这个值能生成更多的线程。但是操作系统对一个进程内的线程数还是有限制的,不能无限生成,经验值在3000~5000左右。
- 能创建的线程数的具体计算公式如下： (MaxProcessMemory - JVMMemory - ReservedOsMemory) / (ThreadStackSize) = Number of threads

```
MaxProcessMemory 指的是进程可寻址的最大空间
JVMMemory     JVM内存
ReservedOsMemory 保留的操作系统内存
ThreadStackSize   线程栈的大小
```

- 在Java语言里， 当你创建一个线程的时候，虚拟机会在JVM内存创建一个Thread对象同时创建一个操作系统线程，而这个系统线程的内存用的不是JVMMemory，而是系统中剩下的内存(MaxProcessMemory - JVMMemory - ReservedOsMemory)。

- 由公式得出结论：你给JVM内存越多，那么你能创建的线程越少，越容易发生`java.lang.OutOfMemoryError: unable to create new native thread`

==综上，在生产环境下如果需要更多的线程数量，建议使用64位操作系统，如果必须使用32位操作系统，可以通过调整Xss的大小来控制线程数量。==



线程总数也受到系统空闲内存和操作系统的限制，检查是否该系统下有此限制：

- /proc/sys/kernel/pid_max          系统最大pid值，在大型系统里可适当调大
- /proc/sys/kernel/threads-max     系统允许的最大线程数
- maxuserprocess（ulimit -u）   系统限制某用户下最多可以运行多少进程或线程
- /proc/sys/vm/max_map_count   

```
max_map_count文件包含限制一个进程可以拥有的VMA(虚拟内存区域)的数量。虚拟内存区域是一个连续的虚拟地址空间区域。在进程的生命周期中，每当程序尝试在内存中映射文件，链接到共享内存段，或者分配堆空间的时候，这些区域将被创建。调优这个值将限制进程可拥有VMA的数量。限制一个进程拥有VMA的总数可能导致应用程序出错，因为当进程达到了VMA上线但又只能释放少量的内存给其他的内核进程使用时，操作系统会抛出内存不足的错误。如果你的操作系统在NORMAL区域仅占用少量的内存，那么调低这个值可以帮助释放内存给内核用。
```



# 3、调优案例

### 3.1 性能优化案例1：调整堆大小提高服务的吞吐量

生产环境下，Tomcat并不建议直接在catalina.sh里配置变量，而是写在与catalina同级目录（bin目录）下的setenv.sh里。

![image-20220320000318322](https://gitee.com/huangwei0123/image/raw/master/img/image-20220320000318322.png)

所以如果我们想要修改jvm的内存配置，那么我们就需要修改setenv.sh文件（默认没有，需新建一个setenv.sh）。

初始化配置

```
setenv.sh文件中写入（大小根据自己情况修改）：setenv.sh内容如下：
 
export CATALINA_OPTS="$CATALINA_OPTS -Xms30m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:SurvivorRatio=8"
export CATALINA_OPTS="$CATALINA_OPTS -Xmx30m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseParallelGC"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDetails"
export CATALINA_OPTS="$CATALINA_OPTS -XX:MetaspaceSize=64m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDateStamps"
export CATALINA_OPTS="$CATALINA_OPTS -Xloggc:/opt/tomcat8.5/logs/gc.log"

```

我们查看日志信息：

![image-20220320000413966](https://gitee.com/huangwei0123/image/raw/master/img/image-20220320000413966.png)

其中存在大量的**Full GC日志**，查看一下我们Jmeter汇总报告

![image-20220320000430072](https://gitee.com/huangwei0123/image/raw/master/img/image-20220320000430072.png)

吞吐量是866.9/sec

优化配置

```
接下来我们测试另外一组数据，增加初始化和最大内存：
export CATALINA_OPTS="$CATALINA_OPTS -Xms120m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:SurvivorRatio=8"
export CATALINA_OPTS="$CATALINA_OPTS -Xmx120m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseParallelGC"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDetails"
export CATALINA_OPTS="$CATALINA_OPTS -XX:MetaspaceSize=64m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDateStamps"
export CATALINA_OPTS="$CATALINA_OPTS -Xloggc:/opt/tomcat8.5/logs/gc.log"
```

重新启动tomcat，查看gc.log
vi gc.log

==查找Full关键字，发现只有一处FullGC，如下图所示，我们可以看到，增大了初始化内存和最大内存之后，我们的Full次数有一个明显的减少。==

![image-20220320000529626](https://gitee.com/huangwei0123/image/raw/master/img/image-20220320000529626.png)

查看Jmeter汇总报告，如下图所示：吞吐量变成了1142.1/sec，基本上是有一个明显的提升，这就说明，我们增加内存之后，服务器的性能有一个明显的提升，这就是我们本次案例的的演示

![image-20220320000543576](https://gitee.com/huangwei0123/image/raw/master/img/image-20220320000543576.png)

### 3.2 性能优化案例2：JVM优化之JIT优化

在《深入理解Java虚拟机中》关于Java堆内存有这样一段描述：
随着==JIT编译期的发展与**逃逸分析技术**逐渐成熟，**栈上分配**、**标量替换优化技术**将会导致一些微妙的变化，所有的对象都分配到堆上也渐渐变得不那么“绝对”了==。

在Java虚拟机中，对象是在Java堆中分配内存的，这是一个普遍的常识。但是，有一种特殊情况，那就是如果经过逃逸分析(Escape Analysis)后发现，一个对象并没有逃逸出方法的话，那么就可能被优化成栈上分配。这样就无需在堆上分配内存，也无须进行垃圾回收了。这也是==最常见的堆外存储技术==。

此外，前面提到的基于OpenJDK深度定制的TaoBaoVM，其中创新的GCIH（GC invisible heap）技术实现off-heap，将生命周期较长的Java对象从heap中移至heap外，并且GC不能管理GCIH内部的Java对象，以此达到降低GC的回收频率和提升GC的回收效率的目的。


马克思：否定之否定！

是 ---> 不是  ---> 是

#### 编译开销

> 编译的时间开销：

解释器的执行，抽象的看是这样的:
输入的代码 -> [ 解释器 解释执行 ] -> 执行结果

JIT编译然后再执行的话，抽象的看则是:
输入的代码 -> [ 编译器 编译 ] -> 编译后的代码 -> [ 执行 ] -> 执行结果

注意：
说JIT比解释快，其实说的是“执行编译后的代码”比“解释器解释执行”要快，并不是说“编译”这个动作比“解释”这个动作快。JIT编译再怎么快，至少也比解释执行一次略慢一些，而要得到最后的执行结果还得再经过一个“执行编译后的代码”的过程。所以，==对“只执行一次”的代码而言，解释执行其实总是比JIT编译执行要快==。怎么算是`只执行一次的代码`呢？粗略说，下面条件同时满足时就是严格的`只执行一次。

- 只被调用一次，例如类的构造器（class initializer，()）
- 没有循环，对只执行一次的代码做JIT编译再执行，可以说是得不偿失。
- 对只执行少量次数的代码，JIT编译带来的执行速度的提升也未必能抵消掉最初编译带来的开销。

==只有对频繁执行的代码（热点代码），JIT编译才能保证有正面的收益==。

> 空间开销

对一般的Java方法而言，编译后代码的大小相对于字节码的大小，膨胀比达到10+是很正常的。同上面说的时间开销一样，这里的空间开销也是，只有对执行频繁的代码才值得编译，如果把所有代码都编译则会显著增加代码所占空间，导致代码爆炸。这也就解释了为什么有些JVM会选择不总是做JIT编译，而是选择用解释器+JIT编译器的混合执行引擎。

#### 即时编译对代码的优化

##### 1、逃逸分析

如果一个子程序分配一个对象并返回一个该对象的指针，该对象可能在程序中被访问到的地方无法确定——这样指针就成功“逃逸”了。如果指针存储在全局变量或者其它数据结构中，因为全局变量是可以在当前子程序之外访问的，此时指针也发生了逃逸。

- 如何将堆上的对象分配到栈，需要使用逃逸分析手段。
- 逃逸分析(Escape Analysis)是目前Java虚拟机中比较前沿的优化技术。这是一种可以==有效减少Java 程序中同步负载和内存堆分配压力的跨函数全局数据流分析算法==。
- ==通过逃逸分析，Java Hotspot编译器能够分析出一个新的对象的引用的使用范围，从而决定是否要将这个对象分配到堆上。==
- 逃逸分析的基本行为就是分析对象动态作用域：
  - ==当一个对象在方法中被定义后，对象只在方法内部使用，则认为没有发生逃逸==。
  - ==当一个对象在方法中被定义后，它被外部方法所引用，则认为发生逃逸。例如作为调用参数传递到其他地方中==。

```java
public void my_method() {
    V v = new V();
    //use v
    //......
    v = null;
}
```

**没有发生逃逸的对象，则可以分配到栈上，随着方法执行的结束，栈空间就被移除。**

逃逸分析包括：

-  全局变量赋值逃逸
- 方法返回值逃逸
- 实例引用发生逃逸
- 线程逃逸:赋值给类变量或可以在其他线程中访问的实例变量

代码例子：

```java
/**
 * 逃逸分析
 *
 *  如何快速的判断是否发生了逃逸分析，大家就看new的对象实体是否有可能在方法外被调用。
 *
 * @author shkstart
 * @create 2021 下午 4:00
 */
public class EscapeAnalysis {

    public EscapeAnalysis obj;

    /*
    方法返回EscapeAnalysis对象，发生逃逸
     */
    public EscapeAnalysis getInstance(){
        return obj == null? new EscapeAnalysis() : obj;
    }
    /*
    为成员属性赋值，发生逃逸
     */
    public void setObj(){
        this.obj = new EscapeAnalysis();
    }
    //思考：如果当前的obj引用声明为static的，会发生逃逸吗？会！

    /*
    对象的作用域仅在当前方法中有效，没有发生逃逸
     */
    public void useEscapeAnalysis(){
        EscapeAnalysis e = new EscapeAnalysis();
    }
    /*
    引用成员变量的值，发生逃逸
     */
    public void useEscapeAnalysis1(){
        EscapeAnalysis e = getInstance();
        //getInstance().xxx()同样会发生逃逸
    }
    /*
    * 也发生了逃逸
    * */
    public void operate(EscapeAnalysis e){
        // e
    }
}

```

**参数设置：**

- 在JDK 6u23版本之后，HotSpot中默认就已经开启了逃逸分析。
- 如果使用的是较早的版本，开发人员则可以通过：
- ==通过选项“-XX:+DoEscapeAnalysis”显式开启逃逸分析==
- 通过选项“-XX：+PrintEscapeAnalysis”查看逃逸分析的筛选结果。

==结论：开发中能使用局部变量的，就不要使用在方法外定义。==

##### **代码优化一：**栈上分配

使用逃逸分析，编译器可以对代码做如下优化：

**栈上分配。将堆分配转化为栈分配**。

如果经过逃逸分析后发现，一个对象并没有逃逸出方法的话，那么就可能被优化成栈上分配。这样就**无需在堆上分配内存**，**也无须进行垃圾回收了**。可以减少垃圾回收时间和次数。

好处：1、栈上分配速度快，大约在30倍 2、不用进行GC，程序响应更快

**JIT编译器在编译期间根据逃逸分析的结果，发现如果一个对象并没有逃逸出方法的话，==就可能被优化成栈上分配。分配完成后，继续在调用栈内执行，最后线程结束，栈空间被回收，局部变量对象也被回收。这样就无须进行垃圾回收了==。**

代码演示：

```java
/**
 * 栈上分配测试
 * -Xmx1G -Xms1G -XX:-DoEscapeAnalysis -XX:+PrintGCDetails
 *
 * 只要开启了逃逸分析，就会判断方法中的变量是否发生了逃逸。如果没有发生了逃逸，则会使用栈上分配
 *
 * @author shkstart  shkstart@126.com
 * @create 2021  10:31
 */
public class StackAllocation {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        for (int i = 0; i < 10000000; i++) {
            alloc();
        }
        // 查看执行时间
        long end = System.currentTimeMillis();
        System.out.println("花费的时间为： " + (end - start) + " ms");
        // 为了方便查看堆内存中对象个数，线程sleep
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private static void alloc() {
        User user = new User();//是否发生逃逸？ 没有！
    }

    static class User {

    }
}
```

##### **代码优化二：**同步消除

==同步省略。如果一个对象被发现只能从一个线程被访问到，那么对于这个对象的操作可以不考虑同步。==

- 线程同步的代价是相当高的，同步的后果是降低并发性和性能。
- 在动态编译同步块的时候，JIT编译器可以==借助逃逸分析来判断同步块所使用的锁对象是否只能够被一个线程访问而没有被发布到其他线程。==如果没有，那么JIT编译器在编译这个同步块的时候就会取消对这部分代码的同步。这样就能大大提高并发性和性能。这个取消同步的过程就叫同步省略，也叫锁消除。

```java
/**
 * 同步省略说明
 * @author shkstart  shkstart@126.com
 * @create 2021  11:07
 */
public class SynchronizedTest {
    public void f() {
        /*
        * 代码中对hollis这个对象进行加锁，但是hollis对象的生命周期只在f()方法中，
        * 并不会被其他线程所访问到，所以在JIT编译阶段就会被优化掉。
        *
        * 问题：字节码文件中会去掉hollis吗？ 
        * */
        Object hollis = new Object();
        synchronized(hollis) {
            System.out.println(hollis);
        }

        /*
        * 优化后； 仅仅是JIT编译器在运行的时候进行的优化，编译成字节码还是会有synchronized修饰的
        * Object hollis = new Object();
        * System.out.println(hollis);
        * */
    }
}
```

##### 代码优化三：标量替换

标量（Scalar）是指一个无法再分解成更小的数据的数据。Java中的原始数据类型就是标量。

相对的，那些还可以分解的数据叫做聚合量（Aggregate），Java中的对象就是聚合量，因为他可以分解成其他聚合量和标量。

==在JIT阶段，如果经过逃逸分析，发现一个对象不会被外界访问的话，那么经过JIT优化，就会把这个对象拆解成若干个其中包含的若干个成员变量来代替。这个过程就是标量替换。==

```java
public static void main(String[] args) {
   alloc();
}
private static void alloc() {
   Point point = new Point（1,2）;
   System.out.println("point.x="+point.x+"; point.y="+point.y);
}
class Point{
    private int x;
    private int y;
}
 
// 以上代码，经过标量替换后，就会变成：
private static void alloc() {
   int x = 1;
   int y = 2;
   System.out.println("point.x="+x+"; point.y="+y);
}
```

可以看到，Point这个聚合量经过逃逸分析后，发现他并没有逃逸，就被替换成两个标量了。那么标量替换有什么好处呢？

==就是可以大大减少堆内存的占用。因为一旦不需要创建对象了，那么就不再需要分配堆内存了。==标量替换为栈上分配提供了很好的基础。

标量替换参数设置：

参数-XX:+EliminateAllocations：开启了标量替换(默认打开)，允许将对象打散分配在栈上。

```java
package com.atguigu.escape;

/**
 * 标量替换测试
 *  -Xmx100m -Xms100m -XX:+DoEscapeAnalysis -XX:+PrintGCDetails -XX:-EliminateAllocations
 *
 *  结论：Java中的逃逸分析，其实优化的点就在于对栈上分配的对象进行标量替换。
 *
 * @author shkstart  shkstart@126.com
 * @create 2021  12:01
 */
public class ScalarReplace {
    public static class User {
        public int id;
        public String name;
    }

    public static void alloc() {
        User u = new User();//未发生逃逸
        u.id = 5;
        u.name = "www.atguigu.com";
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            alloc();
        }
        long end = System.currentTimeMillis();
        System.out.println("花费的时间为： " + (end - start) + " ms");

    }
}

/*
class Customer{
    String name;
    int id;
    Account acct;

}

class Account{
    double balance;
}
 */

```

##### 逃逸分析小结：

- 关于逃逸分析的论文在1999年就已经发表了，但直到JDK 1.6才有实现，而且这项技术到如今也并不是十分成熟的。
- 其根本原因就是无法保证非逃逸分析的性能消耗一定能高于他的消耗。虽然经过逃逸分析可以做标量替换、栈上分配、和锁消除。但是逃逸分析自身也是需要进行一系列复杂的分析的，这其实也是一个相对耗时的过程。
- 一个极端的例子，就是经过逃逸分析之后，发现没有一个对象是不逃逸的。那这个逃逸分析的过程就白白浪费掉了。
- 虽然这项技术并不十分成熟，但是它也是即时编译器优化技术中一个十分重要的手段。
- 注意到有一些观点，认为通过逃逸分析，JVM会在栈上分配那些不会逃逸的对象，这在理论上是可行的，但是取决于JVM设计者的选择。
- 目前很多书籍还是基于JDK 7以前的版本，JDK已经发生了很大变化，intern字符串的缓存和静态变量曾经都被分配在永久代上，而永久代已经被元数据区取代。但是，intern字符串缓存和静态变量并不是被转移到元数据区，而是直接在堆上分配，**所以这一点同样符合前面一点的结论：对象实例都是分配在堆上。**

### 3.3 性能优化案例3：合理配置堆内存

#### 1、推荐配置

在案例1中我们讲到了增加内存可以提高系统的性能而且效果显著，那么随之带来的一个问题就是，我们增加多少内存比较合适？如果内存过大，那么如果产生FullGC的时候，GC时间会相对比较长，如果内存较小，那么就会频繁的触发GC，在这种情况下，我们该如何合理的适配堆内存大小呢？

分析：
==依据的原则是根据Java Performance里面的推荐公式来进行设置==。

![image-20220321202200424](https://gitee.com/huangwei0123/image/raw/master/img/image-20220321202200424.png)



==Java整个堆大小设置，Xmx 和 Xms设置为老年代存活对象的3-4倍，即FullGC之后的老年代内存占用的3-4倍。==
==方法区（永久代 PermSize和MaxPermSize 或 元空间 MetaspaceSize 和 MaxMetaspaceSize）设置为老年代存活对象的1.2-1.5倍==。
==年轻代Xmn的设置为老年代存活对象的1-1.5倍==。
==老年代的内存大小设置为老年代存活对象的2-3倍==。

但是，上面的说法也不是绝对的，也就是说这给的是一个参考值，根据多种调优之后得出的一个结论，大家可以根据这个值来设置一下我们的初始化内存，在保证程序正常运行的情况下，我们还要去查看GC的回收率，GC停顿耗时，内存里的实际数据来判断，Full GC是基本上不能有的，如果有就要做内存Dump分析，然后再去做一个合理的内存分配。

我们还要注意到一点就是，上面说的老年代存活对象怎么去判定。

方式1:查看日志

推荐/比较稳妥！

JVM参数中添加GC日志，GC日志中会记录每次FullGC之后各代的内存大小，观察老年代GC之后的空间大小。可观察一段时间内（比如2天）的FullGC之后的内存情况，==根据多次的FullGC之后的老年代的空间大小数据来预估FullGC之后老年代的存活对象大小（可根据多次FullGC之后的内存大小取平均值）==。

方式2：强制触发Full GC

会影响线上服务，慎用！

方式1的方式比较可行，但需要更改JVM参数，并分析日志。同时，在使用CMS回收器的时候，有可能不能触发FullGC，所以日志中并没有记录FullGC的日志。在分析的时候就比较难处理。 所以，有时候需要强制触发一次FullGC，来观察FullGC之后的老年代存活对象大小。

注：强制触发FullGC，会造成线上服务停顿（STW），要谨慎！建议的操作方式为，**在强制FullGC前先把服务节点摘除，FullGC之后再将服务挂回可用节点，对外提供服务，在不同时间段触发FullGC，根据多次FullGC之后的老年代内存情况来预估FullGC之后的老年代存活对象大小**

如何强制触发Full GC?

1、jmap -dump:live,format=b,file=heap.bin <pid> 将当前的存活对象dump到文件，此时会触发FullGC
2、jmap -histo:live <pid> 打印每个class的实例数目,内存占用,类全名信息.live子参数加上后,只统计活的对象数量. 此时会触发FullGC
3、在性能测试环境，可以通过Java监控工具来触发FullGC，比如使用VisualVM和JConsole，VisualVM集成了JConsole，VisualVM或者JConsole上面有一个触发GC的按钮。

#### 2、案例演示

现在我们通过idea启动springboot工程，我们将内存初始化为1024M。我们这里就从1024M的内存开始分析我们的GC日志，根据我们上面的一些知识来进行一个合理的内存设置。

```
-XX:+PrintGCDetails -XX:MetaspaceSize=64m -Xss512K -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=heap/heapdump3.hprof  -XX:SurvivorRatio=8  -XX:+PrintGCDateStamps  -Xms1024M  -Xmx1024M -Xloggc:log/gc-oom3.log
```

```java
/**
     * 性能优化案例3：合理配置堆内存
     */
@RequestMapping("/getData")
public List<People> getProduct(){
    List<People> peopleList = peopleSevice.getPeopleList();
    return peopleList;
}
```

项目启动，通过jmeter访问10000次（主要是看项目是否可以正常运行）之后，查看gc状态

jstat -gc pid 1000 1

![image-20220321203847960](https://gitee.com/huangwei0123/image/raw/master/img/image-20220321203847960.png)

YGC平均耗时： 0.12s * 10000/7 = 17.14ms
FGC未产生

看起来似乎不错，YGC触发的频率不高，FGC也没有产生，但这样的内存设置是否还可以继续优化呢？是不是有一些空间是浪费的呢。

为了快速看数据，我们使用了方式2，通过命令 jmap -histo:live pid 产生几次FullGC，FullGC之后，使用的jmap -heap 来看的当前的堆内存情况。

观察老年代存活对象大小：

```
jmap -heap pid
或者直接查看GC日志
查看一次FullGC之后剩余的空间大小
```

![image-20220321204050981](https://gitee.com/huangwei0123/image/raw/master/img/image-20220321204050981.png)

可以看到存活对象占用内存空间大概13.36M，老年代的内存占用为683M左右。 按照整个堆大小是老年代（FullGC）之后的3-4倍计算的话，设置堆内存情况如下：

Xmx=14 * 3 = 42M  至  14 * 4 = 56M 之间

我们修改堆内存状态如下：

```
-XX:+PrintGCDetails -XX:MetaspaceSize=64m -Xss512K -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=heap/heapdump.hprof  -XX:SurvivorRatio=8  -XX:+PrintGCDateStamps  -Xms60M  -Xmx60M -Xloggc:log/gc-oom.log
```

修改完之后，我们查看一下GC状态

![image-20220321204336187](https://gitee.com/huangwei0123/image/raw/master/img/image-20220321204336187.png)

请求之后
YGC平均耗时： 0.195s * 1000/68 = 2.87ms
FGC未产生
整体的GC耗时减少。但GC频率比之前的1024M时要多了一些。依然未产生FullGC，所以我们内存设置为60M 也是比较合理的，相对之前节省了很大一块内存空间,所以本次内存调整是比较合理的

依然手动触发Full ，查看堆内存结构

![image-20220321204429124](https://gitee.com/huangwei0123/image/raw/master/img/image-20220321204429124.png)

#### 3、结论

在内存相对紧张的情况下，可以按照上述的方式来进行内存的调优， 找到一个在GC频率和GC耗时上都可接受的一个内存设置，可以用较小的内存满足当前的服务需要。

==但当内存相对宽裕的时候，可以相对给服务多增加一点内存，可以减少GC的频率，GC的耗时相应会增加一些。==一般要求低延时的可以考虑多设置一点内存， 对延时要求不高的，可以按照上述方式设置较小内存。 

==如果在垃圾回收日志中观察到OutOfMemoryError,尝试把Java堆的大小扩大到物理内存的80%~90%。==尤其需要注意的是堆空间导致的OutOfMemoryError以及一定要增加空间。

- 比如说，增加-Xms和-Xmx的值来解决old代的OutOfMemoryError
- 增加-XX:PermSize和-XX:MaxPermSize来解决permanent代引起的OutOfMemoryError（jdk7之前）；增加-XX:MetaspaceSize和-XX:MaxMetaspaceSize来解决Metaspace引起的OutOfMemoryError（jdk8之后）

记住一点Java堆能够使用的容量受限于硬件以及是否使用64位的JVM。在扩大了Java堆的大小之后，再检查垃圾回收日志，直到没有OutOfMemoryError为止。==如果应用运行在稳定状态下没有OutOfMemoryError就可以进入下一步了，计算活动对象的大小==。

> 怎么估算GC频率？

正常情况我们应该根据我们的系统来进行一个内存的估算，这个我们可以在测试环境进行测试，最开始可以将内存设置的大一些，比如4G这样，当然这也可以根据业务系统估算来的。

举例：

比如从数据库获取一条数据占用128个字节，需要获取1000条数据，那么一次读取到内存的大小就是（128 B/1024 Kb/1024M）* 1000 = 0.122M ，那么我们程序可能需要并发读取，比如每秒读取100次，那么内存占用就是0.122*100 = 12.2M  ，如果堆内存设置1个G，那么年轻代大小大约就是333M，那么333M*80% / 12.2M  =21.84s ，也就是说我们的程序几乎每分钟进行两到三次youngGC。这样可以让我们对系统有一个大致的估算。

0.122M * 100 = 12.2M /秒  ---Eden区

1024M * 1/3 * 80% = 273M 

273 / 12.2M = 22.38s ---> YGC  每分钟2-3次YGC 

> 特殊问题：新生代和老年代的比例

JVM参数设置为

```
# 打印日志详情          打印日志打印日期     初始化内存200M  最大内存200M   日志路径
-XX:+PrintGCDetails   -XX:+PrintGCDateStamps  -Xms300M  -Xmx300M -Xloggc:log/gc.log
```

新生代 ( Young ) 与老年代 ( Old ) 的比例为 1:2，所以，内存分配应该是新生代100M，老年代 200M

我们可以先用命令查看一下堆内存分配是怎么样的：

```
# 查看进程ID
jps -l
# 查看对应的进程ID的堆内存分配
jmap -heap 3725
```

结果大家可以看到：我们的SurvivorRatio= 8 但是内存分配却不是8:1:1，这是为什么呢？

这是因为JDK 1.8 默认使用 ==UseParallelGC 垃圾回收器==，该垃圾回收器默认启动了 AdaptiveSizePolicy，==会根据GC的情况自动计算计算 Eden、From 和 To 区的大小；所以这是由于JDK1.8的自适应大小策略导致的==，除此之外，我们下面观察GC日志发现有很多类似这样的FULLGC（Ergonomics），也是一样的原因。

我们可以在jvm参数中配置开启和关闭该配置：

```
  # 开启：
  -XX:+UseAdaptiveSizePolicy
  # 关闭
  -XX:-UseAdaptiveSizePolicy
```

注意事项：

1、在 JDK 1.8 中，如果使用 CMS，无论 UseAdaptiveSizePolicy 如何设置，都会将 UseAdaptiveSizePolicy 设置为 false；不过不同版本的JDK存在差异；

2、UseAdaptiveSizePolicy不要和SurvivorRatio参数显示设置搭配使用，一起使用会导致参数失效；(前者动态计算，后者直接定死)

3、由于UseAdaptiveSizePolicy会动态调整 Eden、Survivor 的大小，有些情况存在Survivor 被自动调为很小，比如十几MB甚至几MB的可能，==这个时候YGC回收掉 Eden区后，还存活的对象进入Survivor 装不下，就会直接晋升到老年代，导致老年代占用空间逐渐增加，从而触发FULL GC==，如果一次FULL GC的耗时很长（比如到达几百毫秒），那么在要求高响应的系统就是不可取的。

附：对于面向外部的大流量、低延迟系统，不建议启用此参数，建议关闭该参数。

如果不想动态调整内存大小，以下是解决方案：

1、保持使用 UseParallelGC，显式设置 -XX:SurvivorRatio=8。

2、使用 CMS 垃圾回收器。CMS 默认关闭 AdaptiveSizePolicy。配置参数 -XX:+UseConcMarkSweepGC

### 3.4 性能优化案例4：CPU占用很高排查方案

案例代码

```java
package com.atguigu.cpu;

/**
 * <pre>
 *    @author  : shkstart
 *    desc    : jstack 死锁案例
 *    version : v1.0
 * </pre>
 */
public class JstackDeadLockDemo {
    /**
     * 必须有两个可以被加锁的对象才能产生死锁，只有一个不会产生死锁问题
     */
    private final Object obj1 = new Object();
    private final Object obj2 = new Object();

    public static void main(String[] args) {
        new JstackDeadLockDemo().testDeadlock();
    }

    private void testDeadlock() {
        Thread t1 = new Thread(() -> calLock_Obj1_First());
        Thread t2 = new Thread(() -> calLock_Obj2_First());
        t1.start();
        t2.start();
    }

    /**
     * 先synchronized  obj1，再synchronized  obj2
     */
    private void calLock_Obj1_First() {
        synchronized (obj1) {
            sleep();
            System.out.println("已经拿到obj1的对象锁，接下来等待obj2的对象锁");
            synchronized (obj2) {
                sleep();
            }
        }
    }

    /**
     * 先synchronized  obj2，再synchronized  obj1
     */
    private void calLock_Obj2_First() {
        synchronized (obj2) {
            sleep();
            System.out.println("已经拿到obj2的对象锁，接下来等待obj1的对象锁");
            synchronized (obj1) {
                sleep();
            }
        }
    }

    /**
     * 为了便于让两个线程分别锁住其中一个对象，
     * 一个线程锁住obj1，然后一直等待obj2，
     * 另一个线程锁住obj2，然后一直等待obj1，
     * 然后就是一直等待，死锁产生
     */
    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

按照国际惯例，我们把代码上传到linux系统进行执行，如果linux报错如下：

错误: 找不到或无法加载主类 JstackDeadLockDemo

解决方案：

原先的etc/profile中的classpath配置

export CLASSPATH=$JAVA_HOME/lib/tools.jar

改为：export CLASSPATH=.:$JAVA_HOME/lib/tools.jar

加了个 .: 当前目录的意思

运行结果如下：

![image-20220321221320770](https://gitee.com/huangwei0123/image/raw/master/img/image-20220321221320770.png)

可以看到，程序依然处于运行状态。现在我们知道是线程死锁造成的问题。

> 问题分析

![image-20220321221339031](https://gitee.com/huangwei0123/image/raw/master/img/image-20220321221339031.png)

从上图可以看出来，当前占用cpu比较高的线程 ID 是1465

接下来把 线程 PID 转换为16进制为

10 进制线程PId 转换为 16 进制

1465   ------->    5b9

5b9 在计算机中显示为   

0x5b9

最后我们把线程信息打印出来：

jstack  1456 > jstack.log

结果如下：

![image-20220321221432431](https://gitee.com/huangwei0123/image/raw/master/img/image-20220321221432431.png)

所有的准备工作已经完成，我们接下来分析日志中的信息，来定位问题出在哪里。

打开jstack.log文件  查找一下刚刚我们转换完的16进制ID是否存在

![image-20220321221456057](https://gitee.com/huangwei0123/image/raw/master/img/image-20220321221456057.png)

jstack命令生成的thread dump信息包含了JVM中所有存活的线程，里面确实是存在我们定位到的线程 ID ，在thread dump中每个线程都有一个nid，在nid=0x5b9的线程调用栈中，我们发现两个线程在互相等待对方释放资源

![image-20220321221524728](https://gitee.com/huangwei0123/image/raw/master/img/image-20220321221524728.png)

到此就可以检查对应的代码是否有问题，也就定位到我们的死锁问题。

```
xxx大厂问题排查过程：
 
...省略;
4、ps aux | grep java  查看到当前java进程使用cpu、内存、磁盘的情况获取使用量异常的进程
5、top -Hp 进程pid  检查当前使用异常线程的pid
6、把线程pid变为16进制如 31695 - 》 7bcf  然后得到0x7bcf
7、jstack 进程的pid | grep -A20  0x7bcf  得到相关进程的代码   -A20 之后得20行
```

觉得方案（针对当前情况 死锁得）：

（1）调整锁的顺序，保持一致

（2）或者采用定时锁，一段时间后，如果还不能获取到锁就释放自身持有的所有锁。

### 3.5 性能优化案例5：G1并发执行的线程数对性能的影响

在8核心Linux配置下，使用如下JVM参数

```
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseG1GC"
export CATALINA_OPTS="$CATALINA_OPTS -Xms30m"
export CATALINA_OPTS="$CATALINA_OPTS -Xmx30m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDetails"
export CATALINA_OPTS="$CATALINA_OPTS -XX:MetaspaceSize=64m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDateStamps"
export CATALINA_OPTS="$CATALINA_OPTS -Xloggc:/opt/tomcat8.5/logs/gc.log"
export CATALINA_OPTS="$CATALINA_OPTS -XX:ConcGCThreads=1"
```

说明：最后一个参数可以在使用G1GC测试初始并发GCThreads之后再加上。

初始化内存和最大内存调整小一些，目的发生 FullGC，关注GC时间

==关注点是：GC次数，GC时间，以及 Jmeter的平均响应时间==

> 初始化状态：

启动tomcat

查看进程默认的并发线程数：

```
jinfo -flag ConcGCThreads pid -XX:ConcGCThreads=1
```

没有配置的情况下：并发线程数是1

查看线程状态：

```
jstat -gc pid
```

![image-20220322221701675](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322221701675.png)

得出信息：

```
YGC：youngGC次数是1259次
FGC：Full GC次数是6次
GCT：GC总时间是5.556s
```

Jmeter压测之后的GC状态：

![image-20220322221743001](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322221743001.png)

得出信息：

```
YGC：youngGC次数是1600次
FGC：Full GC次数是18次
GCT：GC总时间是7.919s
```

由此我们可以计算出来压测过程中，发生的GC次数和GC时间差

压测过程GC状态：

```
YGC：youngGC次数是 1600 - 1259 = 341次
FGC：Full GC次数是 18 - 6 = 12次
GCT：GC总时间是 7.919 - 5.556 = 2.363s
```

Jmeter压测结果如下：

压测结果如下：
主要关注响应时间：
95%的请求响应时间为：16ms
99%的请求响应时间为：28ms

![image-20220322221829595](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322221829595.png)

> 优化之后状态

增加线程配置:

```
export CATALINA_OPTS="$CATALINA_OPTS -XX:ConcGCThreads=8"
```

观察GC状态

```
jstat -gc pid
```

tomcat启动之后的初始化GC状态：

![image-20220322221943595](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322221943595.png)

总结：
YGC：youngGC次数是 1134 次
FGC：Full GC次数是 5 次
GCT：GC总时间是 5.234s

Jmeter压测之后的GC状态：

![image-20220322222016544](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322222016544.png)

总结：
YGC：youngGC次数是 1347 次
FGC：Full GC次数是 16 次
GCT：GC总时间是 7.149s

```
#########
由此我们可以计算出来压测过程中，发生的GC次数和GC时间差
 
压测过程GC状态：
YGC：youngGC次数是 1347 - 1134 = 213次
FGC：Full GC次数是 16 - 5 = 13次
GCT：GC总时间是 7.149 - 5.234 = 1.915s   提供了线程数，使得用户响应时间降低了。
```

压测结果如下：
主要关注响应时间：
95%的请求响应时间为：15ms
99%的请求响应时间为：22ms

![image-20220322222126474](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322222126474.png)



总结：

==配置完线程数之后，我们的请求的平均响应时间和GC时间都有一个明显的减少了，仅从效果上来看，我们这次的优化是有一定效果的。大家在工作中对于线上项目进行优化的时候，可以考虑到这方面的优化==（前提是G1垃圾回收器）。

### 3.6 性能优化案例6：调整垃圾回收器提高服务的吞吐量

系统配置是单核，我们看到日志，显示DefNew，说明我们用的是串行收集器，SerialGC

那么就考虑切换一下并行收集器是否可以提高性能，增加配置如下：

```
export CATALINA_OPTS="$CATALINA_OPTS -Xms60m"
export CATALINA_OPTS="$CATALINA_OPTS -Xmx60m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseParallelGC"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDetails"
export CATALINA_OPTS="$CATALINA_OPTS -XX:MetaspaceSize=64m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDateStamps"
export CATALINA_OPTS="$CATALINA_OPTS -Xloggc:/opt/tomcat8.5/logs/gc6.log"
```

查看GC状态：

![image-20220322222246990](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322222246990.png)

发生3次FullGC，可以接受

![image-20220322222313339](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322222313339.png)

查看吞吐量，997.6/sec，吞吐量并没有明显变化，我们究其原因，本身UseParallelGC是并行收集器，但是我们的服务器是单核。

接下来我们把服务器改为8核。

![image-20220322222347131](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322222347131.png)

8核状态下的性能表现如下，吞吐量大幅提升，甚至翻了一倍，这说明我们在多核机器上面采用并行收集器对于系统的吞吐量有一个显著的效果。

```
接下来我们改为G1收集器看看效果
 
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseG1GC"
export CATALINA_OPTS="$CATALINA_OPTS -Xms60m"
export CATALINA_OPTS="$CATALINA_OPTS -Xmx60m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDetails"
export CATALINA_OPTS="$CATALINA_OPTS -XX:MetaspaceSize=64m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+PrintGCDateStamps"
export CATALINA_OPTS="$CATALINA_OPTS -Xloggc:/opt/tomcat8.5/logs/gc6.log"
```

查看GC状态：

![image-20220322222416883](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322222416883.png)

没有产生FullGC，效果较之前有提升。

查看压测效果，吞吐量也是比串行收集器效果更佳，而且没有了FullGC。此次优化较为成功。

![image-20220322222439260](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322222439260.png)



### 3.7 性能优化案例7：日均百万级订单交易系统如何设置JVM参数

![image-20220322222557686](https://gitee.com/huangwei0123/image/raw/master/img/image-20220322222557686.png)

第二个层面问题：如果要求响应时间控制在100ms如何实现？

配置GC参数：-XX:MaxGCPauseMillis 、 -XX:ConcGCThreads 

根据log日志、dump文件分析，优化内存空间的比例



### 3.8 面试小结

1、分而治之

12306遭遇春节大规模抢票如何支撑？

12306号称是国内并发量最大的秒杀网站，并发量达到百万级别。

普通电商订单-->  下单 --> 订单系统（IO）减库存 ---> 等待用户付款

12306一种可能的模型：下单 --> 减库存和订单（redis、kafka）同时异步进行 --> 等付款
但减库存最后还会把压力压到一台服务器上。如何？

分布式本地库存+单独服务器做库存均衡！



问题二：==系统CPU经常100%，如何调优？==（面试高频）
CPU100%的话，一定是有线程占用系统资源。具体步骤前面已经讲过。

注意： 工作中有时候是工作线程100%占用了CPU，还有可能是垃圾回收线程占用了100%


问题三：==系统内存飙高，如何查找问题？==（面试高频）

一方面：jmap -heap 、jstat 、... ; gc日志情况
另一方面：dump文件分析


问题四：如何监控JVM
 > 命令行工具

> 图形化界面工具  visule VM 

