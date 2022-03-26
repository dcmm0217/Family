# 第7节-JVM性能监控篇

## 1、JVM监控及诊断工具-命令行篇

#### 01-jps：查看正在运行的Java进程

jps(Java Process Status):

显示指定系统内所有的HotSpot虚拟机进程(查看虚拟机进程信息)，可用于查询正在运行的虚拟机进程。

说明：对于本地虚拟机进程来说，进程的本地虚拟机ID与操作系统的进程ID是一致的，是唯一的。

```
它的基本使用语法为：
jps [options] [hostid]
 
我们还可以通过追加参数，来打印额外的信息。

-l: 输出应用程序主类的全类名 或 如果进程执行的是jar包，则输出jar完整路径

-v: 列出虚拟机进程启动时的JVM参数。 比如：-Xms20m -Xmx50m是启动程序指定的jvm参数。
```

#### 02-jstat：查看JVM统计信息

jstat(JVM Statistics Monitoring Tool)：用于监视虚拟机各种运行状态信息的命令行工具。它可以显示本地或者远程虚拟机进程中的类装载、内存、垃圾收集、JIT编译等运行数据。

在没有GUI图形界面，只提供了纯文本控制台环境的服务器上，它将是运行期定位虚拟机性能问题的首选工具。==常用于检测垃圾回收问题以及内存泄漏问题==。

选项option可以由以下值构成。

```
类装载相关的：
-class：显示ClassLoader的相关信息：类的装载、卸载数量、总空间、类装载所消耗的时间等
 
垃圾回收相关的：

-gc：显示与GC相关的堆信息。包括Eden区、两个Survivor区、老年代、永久代等的容量、已用空间、GC时间合计等信息。

-gccapacity：显示内容与-gc基本相同，但输出主要关注Java堆各个区域使用到的最大、最小空间。
-gcutil：显示内容与-gc基本相同，但输出主要关注已使用空间占总空间的百分比。
-gccause：与-gcutil功能一样，但是会额外输出导致最后一次或当前正在发生的GC产生的原因。
-gcnew：显示新生代GC状况
-gcnewcapacity：显示内容与-gcnew基本相同，输出主要关注使用到的最大、最小空间
-geold：显示老年代GC状况
-gcoldcapacity：显示内容与-gcold基本相同，输出主要关注使用到的最大、最小空间
-gcpermcapacity:显示永久代使用到的最大、最小空间。
 
JIT相关的：
-compiler：显示JIT编译器编译过的方法、耗时等信息
-printcompilation：输出已经被JIT编译的方法 
```

#### 03-jinfo：实时查看和修改JVM配置参数

jinfo(Configuration Info for Java)
查看虚拟机配置参数信息，也可用于调整虚拟机的配置参数。

在很多情况下，Java应用程序不会指定所有的Java虚拟机参数。而此时，开发人员可能不知道某一个具体的Java虚拟机参数的默认值。在这种情况下，可能需要通过查找文档获取某个参数的默认值。这个査找过程可能是非常艰难的。但有了jinfo工具，开发人员可以很方便地找到Java虚拟机参数的当前值。

它的基本使用语法为：
jinfo  [ options ] pid

说明：java 进程ID 必须要加上

![image-20220322224149867](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220322224149867.png)

```
jinfo -flags PID  查看曾经赋过值的一些参数

jinfo -flag 具体参数 PID   查看某个java进程的具体参数的值
```

#### 04-jmap：导出内存映像文件&内存使用情况

jmap(JVM Memory Map)：作用一方面是获取dump文件（堆转储快照文件，二进制文件），它还可以获取目标Java进程的内存相关信息，包括Java堆各区域的使用情况、堆中对象的统计信息、类加载信息等。

```
它的基本使用语法为：
jmap [option] <pid>
jmap [option] <executable <core>
jmap [option] [server_id@]<remote server IP or hostname>
```

![image-20220322224310602](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220322224310602.png)

一般来说，使用jmap指令生成dump文件的操作算得上是最常用的jmap命令之一,将堆中所有存活对象导出至一个文件之中。

说明： 
1. 通常在写Heap Dump文件前会触发一次Full GC，所以heap dump文件里保存的都是FullGC后留下的对象信息。
2. 由于生成dump文件比较耗时，因此大家需要耐心等待，尤其是大内存镜像生成dump文件则需要耗费更长的时间来完成。

> 手动导出内存映像文件

jmap -dump:live,format=b,file=<filename.hprof> <pid>

jmap -dump:format=b,file=<filename.hprof> <pid>

> 自动导出内存映像文件

当程序发生OOM退出系统时，一些瞬时信息都随着程序的终止而消失，而重现OOM问题往往比较困难或者耗时。此时若能在OOM时，自动导出dump文件就显得非常迫切。

这里介绍一种比较常用的取得堆快照文件的方法，即使用:
-XX:+HeapDumpOnOutOfMemoryError：在程序发生OOM时，导出应用程序的当前堆快照。
-XX:HeapDumpPath：可以指定堆快照的保存位置。

比如：
-Xmx100m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=D:\m.hprof

> 使用2：显示堆内存相关信息

```
jmap -permstat pid    查看系统的ClassLoader信息
jmap -finalizerinfo   査看堆积在finalizer队列中的对象
```

#### 05-jstack：打印JVM中线程快照

jstack(JVM Stack Trace)：用于生成虚拟机指定进程当前时刻的线程快照(虚拟机堆栈跟踪)。 线程快照就是当前虚拟机内指定进程的每一条线程正在执行的方法堆栈的集合。

生成线程快照的作用：可用于==定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等问题==。这些都是导致线程长时间停顿的常见原因。当线程出现停顿时，就可以用jstack显示各个线程调用的堆栈情况。

在thread dump中，要留意下面几种状态 

- ==死锁，Deadlock（重点关注）== 
- ==等待资源，Waiting on condition（重点关注==）
- ==等待获取监视器，Waiting on monitor entry 重点关注== 
- ==阻塞，Blocked（重点关注）==
- 执行中，Runnable
- 暂停，Suspended
- 对象等待中，Object.wait() 或 TIMED_WAITING
- 停止，Parked

它的基本使用语法为：
jstack option pid



## 2、JVM监控及诊断工具-GUI篇

#### 01-Visual VM

Visual VM是一个功能强大的多合一故障诊断和性能监控的可视化工具。
它集成了多个JDK命令行工具，使用Visual VM可用于显示虚拟机进程及进程的配置和环境信息(jps,jinfo)，监视应用程序的CPU、GC、堆、方法区及线程的信息(jstat、jstack)等，甚至代替JConsole。
在JDK 6 Update 7以后，Visual VM便作为JDK的一部分发布（VisualVM 在JDK/bin目录下），即：它完全免费。
此外，Visual VM也可以作为独立的软件安装:

主要功能：

- 1-生成/读取堆内存快照
- 2-查看JVM参数和系统属性
- 3-查看运行中的虚拟机进程
- 4-生成/读取线程快照
- 5.程序资源的实时监控
- 6.CPU分析和内存分析

#### 02-JProfiler

在运行Java的时候有时候想测试运行时占用内存情况，这时候就需要使用测试工具查看了。在eclipse里面有 Eclipse Memory Analyzer tool(MAT)插件可以测试，而在IDEA中也有这么一个插件，就是JProfiler。

特点：

```
使用方便、界面操作友好 （简单且强大）
对被分析的应用影响小  （提供模板）
CPU,Thread,Memory分析功能尤其强大
支持对jdbc,noSql, jsp, servlet, socket等进行分析
支持多种模式(离线，在线)的分析
支持监控本地、远程的JVM
跨平台,拥有多种操作系统的安装版本
```

主要功能

- 1-方法调用
- 2-内存分配
- 3-线程和锁
- 4-高级子系统

#### 03-Arthas

见阿里官方文档好了

## 3、JVM运行时参数

#### 03-常用的JVM参数选项

打印设置的XX选项及值

```
-XX:+PrintCommandLineFlags   可以让在程序运行前打印出用户手动设置或者JVM自动设置的XX选项
-XX:+PrintFlagsInitial       表示打印出所有XX选项的默认值
-XX:+PrintFlagsFinal         表示打印出XX选项在运行程序时生效的值
-XX:+PrintVMOptions          打印JVM的参数
```

堆、栈、方法区等内存大小设置

```
栈:
-Xss128k   设置每个线程的栈大小为128k   等价于 -XX:ThreadStackSize=128k

堆内存:
-Xms3550m  等价于-XX:InitialHeapSize，设置JVM初始堆内存为3550M
-Xmx3550m	等价于-XX:MaxHeapSize，设置JVM最大堆内存为3550M
-Xmn2g	设置年轻代大小为2G  官方推荐配置为整个堆大小的3/8
-XX:NewSize=1024m	设置年轻代初始值为1024M
-XX:MaxNewSize=1024m	设置年轻代最大值为1024M
-XX:SurvivorRatio=8		设置年轻代中Eden区与一个Survivor区的比值，默认为8
-XX:+UseAdaptiveSizePolicy   自动选择各区大小比例
-XX:NewRatio=4  		设置老年代与年轻代（包括1个Eden和2个Survivor区）的比值


-XX:PretenureSizeThreadshold=1024		设置让大于此阈值的对象直接分配在老年代，单位为字节  只对Serial、ParNew收集器有效
-XX:MaxTenuringThreshold=15		默认值为15  新生代每次MinorGC后，还存活的对象年龄+1，当对象的年龄大于设置的这个值时就进入老年代
-XX:+PrintTenuringDistribution		让JVM在每次MinorGC后打印出当前使用的Survivor中对象的年龄分布	
-XX:TargetSurvivorRatio		表示MinorGC结束后Survivor区域中占用空间的期望比例	

方法区:
-XX:MetaspaceSize  初始空间大小
-XX:MaxMetaspaceSize	最大空间，默认没有限制
-XX:+UseCompressedOops	 压缩对象指针
-XX:+UseCompressedClassPointers	压缩类指针
-XX:CompressedClassSpaceSize	设置class Metaspace的大小，默认1G

直接内存:
-XX:MaxDirectMemorySize	   指定DirectMemory容量，若未指定，则默认与Java堆最大值一样
```

OutofMemory相关的选项

```
-XX:+HeapDumpOnOutOfMemoryError		表示在内存出现OOM的时候，把Heap转存(Dump)到文件以便后续分析

-XX:+HeapDumpBeforeFullGC		表示在出现FullGC之前，生成Heap转储文件

-XX:HeapDumpPath=<path>			指定heap转存文件的存储路径

-XX:OnOutOfMemoryError		指定一个可行性程序或者脚本的路径，当发生OOM的时候，去执行这个脚本
```

垃圾收集器相关选项

```
-XX:+PrintCommandLineFlags：查看命令行相关参数（包含使用的垃圾收集器）

使用命令行指令：jinfo –flag 相关垃圾回收器参数 进程ID

```

#### 04-按功能性质区分JVM参数选项

```
Java HotSpot VM中-XX:的可配置参数列表进行描述；
 
这些参数可以被松散的聚合成三类：
行为参数（Behavioral Options）：用于改变jvm的一些基础行为；
性能调优（Performance Tuning）：用于jvm的性能调优；
调试参数（Debugging Options）：一般用于打开跟踪、打印、输出等jvm参数，用于显示jvm更加详细的信息；
 
 
行为参数(功能开关)
-XX:-DisableExplicitGC  禁止调用System.gc()；但jvm的gc仍然有效
-XX:+MaxFDLimit 最大化文件描述符的数量限制
-XX:+ScavengeBeforeFullGC   新生代GC优先于Full GC执行
-XX:+UseGCOverheadLimit 在抛出OOM之前限制jvm耗费在GC上的时间比例
-XX:-UseConcMarkSweepGC 对老生代采用并发标记交换算法进行GC
-XX:-UseParallelGC  启用并行GC
-XX:-UseParallelOldGC   对Full GC启用并行，当-XX:-UseParallelGC启用时该项自动启用
-XX:-UseSerialGC    启用串行GC
-XX:+UseThreadPriorities    启用本地线程优先级
 
性能调优
-XX:LargePageSizeInBytes=4m 设置用于Java堆的大页面尺寸
-XX:MaxHeapFreeRatio=70 GC后java堆中空闲量占的最大比例
-XX:MaxNewSize=size 新生成对象能占用内存的最大值
-XX:MaxPermSize=64m 老生代对象能占用内存的最大值
-XX:MinHeapFreeRatio=40 GC后java堆中空闲量占的最小比例
-XX:NewRatio=2  新生代内存容量与老生代内存容量的比例
-XX:NewSize=2.125m  新生代对象生成时占用内存的默认值
-XX:ReservedCodeCacheSize=32m   保留代码占用的内存容量
-XX:ThreadStackSize=512 设置线程栈大小，若为0则使用系统默认值
-XX:+UseLargePages  使用大页面内存
 
调试参数
-XX:-CITime 打印消耗在JIT编译的时间
-XX:ErrorFile=./hs_err_pid<pid>.log 保存错误日志或者数据到文件中
-XX:-ExtendedDTraceProbes   开启solaris特有的dtrace探针
-XX:HeapDumpPath=./java_pid<pid>.hprof  指定导出堆信息时的路径或文件名
-XX:-HeapDumpOnOutOfMemoryError 当首次遭遇OOM时导出此时堆中相关信息
-XX:OnError="<cmd args>;<cmd args>" 出现致命ERROR之后运行自定义命令
-XX:OnOutOfMemoryError="<cmd args>;<cmd args>"  当首次遭遇OOM时执行自定义命令
-XX:-PrintClassHistogram    遇到Ctrl-Break后打印类实例的柱状信息，与jmap -histo功能相同
-XX:-PrintConcurrentLocks   遇到Ctrl-Break后打印并发锁的相关信息，与jstack -l功能相同
-XX:-PrintCommandLineFlags  打印在命令行中出现过的标记
-XX:-PrintCompilation   当一个方法被编译时打印相关信息
-XX:-PrintGC    每次GC时打印相关信息
-XX:-PrintGC Details    每次GC时打印详细信息
-XX:-PrintGCTimeStamps  打印每次GC的时间戳
-XX:-TraceClassLoading  跟踪类的加载信息
-XX:-TraceClassLoadingPreorder  跟踪被引用到的所有类的加载信息
-XX:-TraceClassResolution   跟踪常量池
-XX:-TraceClassUnloading    跟踪类的卸载信息
-XX:-TraceLoaderConstraints 跟踪类加载器约束的相关信息
 

```

