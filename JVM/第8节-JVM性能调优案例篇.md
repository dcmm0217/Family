# 第8节-JVM性能调优案例篇

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

