# 分布式消息RocketMQ

本文是参考尚硅谷老雷老师RocketMQ视频以及笔记，定位是学习笔记。

## 第1章 RockeMQ简述

### 一、MQ概述 

#### 1、MQ简介

MQ，Message Queue，是一种提供消息队列服务的中间件，也称为消息中间件，是一套提供了消息生产、存储、消费全过程API的软件系统。消息即数据。一般消息的体量不会很大。

#### 2、MQ用途

从网上可以查看到很多的关于MQ用途的叙述，但总结起来其实就以下三点。

**限流削峰**

MQ可以将系统的`超量`请求暂存其中，以便系统后期可以慢慢进行处理，从而避免了请求的丢失或系统被压垮。

![image-20211122143023481](https://gitee.com/huangwei0123/image/raw/master/img/image-20211122143023481.png)

**异步解耦**

上游系统对下游系统的调用若为同步调用，则会大大降低系统的吞吐量与并发度，而且系统耦合度太高。而异步调用会解决这些问题。所以两层之间若要实现同步到异步的转换，**一般性做法是，在这两层之间添加一个MQ层**

![image-20211122143234983](https://gitee.com/huangwei0123/image/raw/master/img/image-20211122143234983.png)

**数据收集**

分布式系统会产生海量级数据流，如：业务日志、数据监控、用户行为等。针对这些数据流进行实时或批量采集汇总，然后对这些数据流进行大数据分析，这是当前互联网平台必备的技术。通过MQ完成此类数据的收集是最好的选择。

#### 3、常见的MQ产品

**ActiveMQ** 

ActiveMQ是使用Java语言开发一款MQ产品。早期很多公司与项目中都在使用。但现在的社区活跃度已经很低。现在的项目中已经很少使用了。

**RabbitMQ** 

RabbitMQ是使用ErLang语言开发的一款MQ产品。其吞吐量较Kafka与RocketMQ要低，且由于其不是Java语言开发，所以公司内部对其实现定制化开发难度较大。

**Kafka**

Kafka是使用Scala/Java语言开发的一款MQ产品。其最大的特点就是高吞吐率，常用于大数据领域的实时计算、日志采集等场景。其没有遵循任何常见的MQ协议，而是使用自研协议。对于Spring Cloud Netflix，其仅支持RabbitMQ与Kafka。

**RocketMQ**

RocketMQ是使用Java语言开发的一款MQ产品。经过数年阿里双11的考验，性能与稳定性非常高。其没有遵循任何常见的MQ协议，而是使用自研协议。对于Spring Cloud Alibaba，其支持RabbitMQ、 Kafka，但提倡使用RocketMQ。

**对比**

| 关键词     | ActiveMQ | RabbitMQ | Kafka                         | RocketMQ                      |
| ---------- | -------- | -------- | ----------------------------- | ----------------------------- |
| 开发语言   | Java     | Erlang   | Java                          | Java                          |
| 单机吞吐量 | 万级     | 万级     | 十万级                        | 十万级                        |
| Topic      | -        | -        | 百级Topic时会影响系统的吞吐量 | 千级Topic时会影响系统的吞吐量 |
| 社区活跃度 | 低       | 高       | 高                            | 高                            |

#### 4、MQ常见协议

一般情况下MQ的实现是要遵循一些常规性协议的。常见的协议如下：

**JMS**

JMS，Java Messaging Service（Java消息服务）。是Java平台上有关MOM（Message Oriented Middleware，面向消息的中间件 PO/OO/AO）的技术规范，它便于消息系统中的Java应用程序进行消息交换，并且通过提供标准的产生、发送、接收消息的接口，简化企业应用的开发。ActiveMQ是该协议的典型实现。

**STOMP** 

STOMP，Streaming Text Orientated Message Protocol（面向流文本的消息协议），是一种MOM设计的简单文本协议。STOMP提供一个可互操作的连接格式，允许客户端与任意STOMP消息代理（Broker）进行交互。ActiveMQ是该协议的典型实现，RabbitMQ通过插件可以支持该协议。

**AMQP**

AMQP，Advanced Message Queuing Protocol（高级消息队列协议），一个提供统一消息服务的应用层标准，是应用层协议的一个开放标准，是一种MOM设计。基于此协议的客户端与消息中间件可传递消息，并不受客户端/中间件不同产品，不同开发语言等条件的限制。 RabbitMQ是该协议的典型实现。

**MQTT**

MQTT，Message Queuing Telemetry Transport（消息队列遥测传输），是IBM开发的一个即时通讯协议，是一种二进制协议，主要用于服务器和低功耗IoT（物联网）设备间的通信。该协议支持所有平台，几乎可以把所有联网物品和外部连接起来，被用来当做传感器和致动器的通信协议。 RabbitMQ通过插件可以支持该协议。



### 二、RocketMQ概述

#### 1、RocketMQ简介

RocketMQ是一个统一消息引擎、轻量级数据处理平台。

RocketMQ是⼀款阿⾥巴巴开源的消息中间件。2016年11⽉28⽇，阿⾥巴巴向 Apache 软件基⾦会捐赠RocketMQ，成为 Apache 孵化项⽬。2017 年 9 ⽉ 25 ⽇，Apache 宣布 RocketMQ孵化成为 Apache 顶级项⽬（TLP ），成为国内⾸个互联⽹中间件在 Apache 上的顶级项⽬。

官⽹地址：http://rocketmq.apache.org 



#### 2、RocketMQ发展历程

![image-20211122154313977](https://gitee.com/huangwei0123/image/raw/master/img/image-20211122154313977.png)

2007年，阿里开始五彩石项目，Notify作为项目中交易核心消息流转系统，应运而生。Notify系统是RocketMQ的雏形。

2010年，B2B大规模使用ActiveMQ作为阿里的消息内核。阿里急需一个具有海量堆积能力的消息系统。

2011年初，Kafka开源。淘宝中间件团队在对Kafka进行了深入研究后，开发了一款新的MQ，MetaQ。2012年，MetaQ发展到了v3.0版本，在它基础上进行了进一步的抽象，形成了RocketMQ，然后就将其进行了开源。

2015年，阿里在RocketMQ的基础上，又推出了一款专门针对阿里云上用户的消息系统Aliware MQ。 

2016年双十一，RocketMQ承载了万亿级消息的流转，跨越了一个新的里程碑。11⽉28⽇，阿⾥巴巴

向 Apache 软件基⾦会捐赠 RocketMQ，成为 Apache 孵化项⽬。

2017 年 9 ⽉ 25 ⽇，Apache 宣布 RocketMQ孵化成为 Apache 顶级项⽬（TLP ），成为国内⾸个互联⽹中间件在 Apache 上的顶级项⽬。



## 第2章 RocketMQ的安装与启动

### 一、基本概念

#### 1、消息（Message）

消息是指，消息系统所传输信息的物理载体，生产和消费数据的最小单位，每条消息必须属于一个主题（Topic）

#### 2、主题（Topic）

![image-20211122154642925](https://gitee.com/huangwei0123/image/raw/master/img/image-20211122154642925.png)

Topic表示一类消息的集合，每个主题包含若干条消息，**每条消息只能属于一个主题，是RocketMQ进行消息订阅的基本单位**

Topic：Message  = 1 : n  （每个主题可以保函若干条消息）

message : topic = 1：1（一条消息只能属于一个主题）

#### 3、标签（Tag）

为消息设置标签，用于同一主题下，区分不同类型的消息。来自同一业务单元的消息，可以根据不同业务目的在同一主题下，设置不同标签。

标签能够有效的保持代码的清晰度和连贯性，并优化RocketMQ提供的查询系统。消费者可以根据Tag实现对不同子主题的不同消费逻辑，实现了更好的扩展性。

Topic是消息的一级分类，Tag是消息的二级分类。

Topic：货物

tag=上海、tag=江苏、tag=浙江

消费者： topic = 货物  tag = 上海  、topic = 货物  tag = 上海  | 浙江  、topic = 货物  tag = *

#### 4、队列（Queue）

存储消息的物理实体。一个Topic中可以包含多个Queue，每个Queue中存放的就是该Topic的消息。一个Topic的Queue也被称为一个Topic中消息的分区（Partition）

一个Topic的Queue中的消息只能被一个消费者组中的一个消费者消费。一个Queue中的消息不允许同一个消费者组中的多个消费者同时消费。

![image-20211123110533036](https://gitee.com/huangwei0123/image/raw/master/img/image-20211123110533036.png)

在学习参考其他资料时，还会看到一个概念：分片（Sharing）。分片不同于分区，在RocketMQ中，分片指的是存放相应Topic的Broker。每个分片中会创建相应数量的分区，即Queue，每个Queue的大小都是一样的。

![image-20211123110936281](https://gitee.com/huangwei0123/image/raw/master/img/image-20211123110936281.png)

#### 5、消息标识（MessageId/Key）

RocketMQ中每个消息拥有唯一的MessageId,且可以携带具有业务标识的key，以方便对消息查询。不过需要注意的是，MessageId有两个：在生产者send()消息时会自动生成一个MessageId（msgId），当消息到达Broker后，Broker也会自动生成一个MessageId（offsetMsgId）。msgId，offsetMsgId与Key都称为消息标识。

- msgId：由producer端生成，其生成规则为：

​	**producerIp + 进程pid + MessageClientIDSetter类的ClassLoader的hashCode + 当前时间 + AutomicInteger自增计数器**

- offsetMsgId：由broker端生成，其生成规则为：**brokerIp + 物理分区的offset（Queue中的偏移量）**

- key：由用户指定的业务相关的唯一标识

### 二、系统架构

![image-20211123112105936](https://gitee.com/huangwei0123/image/raw/master/img/image-20211123112105936.png)

RocketMQ架构上主要分为四部分构成：

#### 1、Producer

消息生产者，负责生产消息。Producer通过MQ的负载均衡模块选择对应的Broker集群队列进行消息投递，投递的过程支持快速失败并且低延迟。

> 例如：业务系统产生的日志写入到MQ的过程，就是消息生产的过程
>
> 再如：电商平台中用户提交的秒杀请求写入到MQ的过程，就是消息生产的过程

RocketMQ中的消息生产者都是以生产者组（Producer Group）的形式出现的。生产者组时同一类生产者的集合，这类Producer发送相同的Topic类型的消息。一个生产者组可以同时发送多个主题的消息。



#### 2、Consumer

消息消费者，负责消费消息。一个消息消费者会从Broker服务器中获取到消息，并对业务进行业务相关处理。

> 例如：Qos系统从MQ中读取日志，并对日志进行解析处理的过程就是消息的消费过程
>
> 再如：电商平台的业务系统从MQ中读取秒杀请求，并对请求进行处理的过程就是消息的消费过程

RocketMQ中的消息消费者都是以消费者组(Consumer Group)的形式出现的。消费者组是同一类消费者组的集合，这类Consumer消费的是同一个Topic类型的消息。消费者组使得在消费者消费方面，实现**负载均衡**（将一个Topic中的不同的Queue平均分配给同一个Consumer Group的不同的Consumer，注意，并不是将**负载均衡**）和**容错**（一个Consumer挂了，该Consumer Group中的其他Consumer可以接着消费原Consumer消费的Queue）的目标变得非常容易。

![image-20211124190842217](https://gitee.com/huangwei0123/image/raw/master/img/image-20211124190842217.png)

消费者组中的Consumer的数量应该小于等于订阅Topic的Queue数量。如果超出Queue数量，则多出的Consumer将不能消费消息

![image-20211124191039104](https://gitee.com/huangwei0123/image/raw/master/img/image-20211124191039104.png)

不过，一个Topic类型的消息可以被多个消费者组同时消费。

> 注意：
>
> 1、消费者组只能消费一个Topic的消息，不能同时消费多个Topic消息
>
> 2、一个消费者组中的消费者必须订阅完全相同的Topic

#### 3、NameServer

**功能介绍**

NameServer是一个Broker与Topic路由的注册中心，支持Broker的动态注册与发现

2个功能：

- Broker管理：接受Broker集群的注册信息并且保存下来作为路由的信息的基本数据；提供心跳检测机制，检查Broker是否还存活
- 路由信息管理：每个NameServer中都保存着Broker集群的整个路由信息和用于客户端查询的队列信息。Producer和Conumser通过NameServer可以获取整个Broker集群的路由信息，从而进行消息的投递和消费。

**路由注册**

NameServer通常也是以集群的方式部署，不过，NameServer是无状态的，即NameServer集群中的各个节点间是无差异的，各节点间相互不进行信息通讯。那各节点中的数据是如何进行数据同步的呢？在Broker节点启动时，轮询NameServer列表，与每个NameServer节点建立长连接，发起注册请求。在NameServer内部维护着⼀个Broker列表，用来动态存储Broker的信息。

> 注意，这是与其它像*zk*、*Eureka*、*Nacos*等注册中心不同的地方。
>
> 这种*NameServer*的无状态方式，有什么优缺点： 
>
> 优点：*NameServer*集群搭建简单，扩容简单。 
>
> 缺点：对于*Broker*，必须明确指出所有*NameServer*地址。否则未指出的将不会去注册。也正因 为如此，*NameServer*并不能随便扩容。因为，若*Broker*不重新配置，新增的*NameServer*对于 *Broker*来说是不可见的，其不会向这个*NameServer*进行注册。

Broker节点为了证明自己是活着的，**为了维护与NameServer间的长连接**，会将最新的信息以心跳包的方式上报给NameServer，每30秒发送一次心跳。心跳包中包含 BrokerId、Broker地址(IP+Port)、 Broker名称、Broker所属集群名称等等。

**NameServer在接收到心跳包后，会更新心跳时间戳，记录这个Broker的最新存活时间**。

**路由剔除**

由于Broker关机、宕机或网络抖动等原因，**NameServer没有收到Broker的心跳**，NameServer可能会将其从Broker列表中**剔除**。

NameServer中有⼀个**定时任务**，每隔**10秒就会扫描⼀次Broker表**，查看**每一个Broker的最新心跳时间戳距离当前时间是否超过120秒**，

**如果超过，则会判定Broker失效，然后将其从Broker列表中剔除。**

> 扩展：对于RocketMQ日常运维工作，例如Broker升级，需要停掉Broker的工作。运维需要怎么做?
>
> 先将Broker的读写权限禁掉，一旦client（Consumer或Producer）向broker发送请求，都会接收到Broker的NO_PERMISSION响应，然后client会进行对其它Broker的重试
>
> 当运维观察到这个Broker没有流量后，再关闭它，实现Broker从NameServer的移除

**路由发现**

RocketMQ的路由发现采用的是**Pull模型**。当Topic路由信息出现变化时，**NameServer不会主动推送给客户端**，而是客户端定时拉取主题最新的路由。**默认客户端每30秒会拉取一次最新的路由**。

> 扩展:
>
> 1、Push模型：推送模型。其实时性较好，是一个“发布-订阅”模型，需要维护一个长连接。而长连接的维护是需要资源成本的。该模型适合于的场景
>
> - 实时性要求高
>
> - client数量不多，Server数据变化频繁
>
>   2、Pull模型 ： 拉取模型。存在问题，实时性比较差。
>
>   3、Long polling模型：长轮询模式。尤其对push与pull模型的整合，充分利用这2种模型的优势，屏蔽了他们的劣势。

**客户端NameServer选择策略**

> 这里的客户端指的是*Producer*与*Consumer*
>

客户端在配置时必须要写上NameServer集群的地址，那么客户端到底连接的是哪个NameServer节点呢？客户端首先会生产一个随机数，然后再与NameServer节点数量取模，此时得到的就是所要连接的节点索引，然后就会进行连接。如果连接失败，则会采用round-robin策略，逐个尝试着去连接其它节点。

**首先采用的是随机策略进行的选择，失败后采用的是轮询策略。**

```
扩展：Zookeeper Client是如何选择Zookeeper Server的？ 
简单来说就是，经过两次Shuffle，然后选择第一台Zookeeper Server。 
详细说就是，将配置文件中的zk server地址进行第一次Shuffle，
然后随机选择一个。这个选择出 的一般都是一个hostname。然后获取到该hostname对应的所有ip，再对这些ip进行第二次 Shuffle，从Shuffle过的结果中取第一个server地址进行连接。
```

#### **4、Broker**

Broker充当着消息中转角色，负责存储消息、转发消息。

Broker在RocketMQ系统中负责**接收并存储**从生产者发送来的消息，同时为消费者的拉取请求作准备。

Broker同时也存储着消息相关的元数据，包括**消费者组消费进度偏移offset、主题、队列**等。

**模块构成**

下图为Broker Server的功能模块示意图。

![image-20211124200701786](https://gitee.com/huangwei0123/image/raw/master/img/image-20211124200701786.png)

`Remoting Module`：整个Broker的实体，负责处理来自clients端的请求。而这个Broker实体则由以下模块构成。

`Client Manager`：客户端管理器。负责接收、解析客户端(Producer/Consumer)请求，管理客户端。例如，维护Consumer的Topic订阅信息

`Store Service`：存储服务。提供方便简单的API接口，处理消息存储到物理硬盘和消息查询功能

`HA Service`：高可用服务，提供Master Broker 和 Slave Broker之间的数据同步功能

`Index Service`：索引服务。根据特定的Message key，对投递到Broker的消息进行索引服务，同时也提供根据Message Key对消息进行快速查询的功能。

**集群部署**

![image-20211124200913418](https://gitee.com/huangwei0123/image/raw/master/img/image-20211124200913418.png)

为了增强Broker性能与吞吐量，Broker一般都是以集群形式出现的。各集群节点中可能存放着相同Topic的不同Queue

不过，这里有个问题，**如果某Broker节点宕机，如何保证数据不丢失呢？**其解决方案是，**将每个Broker集群节点进行横向扩展，即将Broker节点再建为一个HA集群，解决单点问题**

Broker节点集群是一个主从集群，即集群中具有Master与Slave两种角色

**Master负责处理读写操作请求，Slave负责对Master中的数据进行备份**(主备模式)

当Master挂掉了，Slave则会自动切换为Master去工作。所以这个Broker集群是主备集群

一个Master可以包含多个Slave，但一个Slave只能隶属于一个Master

Master与Slave 的对应关系是通过指定相同的BrokerName、不同的BrokerId 来确定的

**BrokerId为0表 示Master，非0表示Slave。每个Broker与NameServer集群中的所有节点建立长连接，定时注册Topic信息到所有NameServer**。

#### 5、工作流程

1、启动NameServer，NameServer会开始监听所有端口，等待Broker、Producer、Consumer连接

2、启动Broker时，Broker会与所有的NameServer建立并保持长连接，然后每30s向NameServer定时发送心跳包

3、发送消息前，先创建Topic，创建Topic时需要指定该Topic要存储在哪些Broker上，当然，在创建Topic时，也会将Topic与NameServer的关系写入到NameServer。不过，这步是可选的，也可以在发送消息时自动创建Topic

4、Producer发送消息，启动时先跟NameServer集群中的其中一台建立**长连接**，并从NameServer中获取路由信息，即当前发送的Topic消息的Queue与Broker的地址（IP+Port）的映射关系。然后**根据算法策略从队列**选择一个Queue，与队列所在的Broker建立长连接从而向Broker发送消息。当然，**在获取到路由信息后，Producer会首先将路由信息缓存到本地，再每30s从NameServer更新一次路由信息**。

5、Consumer跟Producer类似，跟其中一台NameServer建立长连接，获取其所订阅Topic的路由信息，然后根据**算法策略从路由信息中获取到其所要消费的Queue**，然后直接跟**Broker建立长连接**，**开始消费其中的消息**。Consumer在获取到路由信息后，**同样也会每30s从NameServer更新一次路由信息**。不过不同于Producer的是，Consumer还会向Broker发送心跳，以确保Broker的存活状态。

**Topic的创建模式**

手动创建Topic时，有两种模式：

- 集群模式：该模式下创建的Topic在该集群中，所有Broker中的Queue数量可以是相同的
- Broker模式：该模式下创建的Topic在该集群中，每个Broker中的Queue数量可以不同

自动创建Topic时，默认采用的是Broker模式，会为每个Broker默认创建4个Queue。

**读/写队列**

从物理上来讲，读/写队列是同一个队列。所以，不存在读/写队列数据同步问题。读/写队列是逻辑卷上进行区分的概念。一**般情况下，读/写队列数量是相同的**。

```
例如，创建Topic时设置的写队列数量为8，读队列数量为4，此时系统会创建8个Queue，分别是0 1 2 3 4 5 6 7。Producer会将消息写入到这8个队列，但Consumer只会消费0 1 2 3这4个队列中的消息，4 5 6 7中的消息是不会被消费到的。

再如，创建Topic时设置的写队列数量为4，读队列数量为8，此时系统会创建8个Queue，分别是0 1 2 3 4 5 6 7。Producer会将消息写入到0 1 2 3 这4个队列，但Consumer只会消费0 1 2 3 4 5 6 7这8个队列中的消息，但是4 5 6 7中是没有消息的。此时假设Consumer Group中包含两个Consuer，Consumer1消 费0 1 2 3，而Consumer2消费4 5 6 7。但实际情况是，Consumer2是没有消息可消费的。

也就是说，当读/写队列数量设置不同时，总是有问题的。那么，为什么要这样设计呢？

例如，原来创建的Topic中包含16个Queue，如何能够使其Queue缩容为8个，还不会丢失消息？可以动态修改写队列数量为8，读队列数量不变。此时新的消息只能写入到前8个队列，而消费都消费的却是16个队列中的数据。当发现后8个Queue中的消息消费完毕后，就可以再将读队列数量动态设置为8。整个缩容过程，没有丢失任何消息。

其这样设计的目的是为了，方便Topic的Queue的缩容。

perm用于设置对当前创建Topic的操作权限：2表示只写，4表示只读，6表示读写。
```



### 三、单机安装与启动

#### 1、准备工作

**软硬件要求**

系统要求是64位的，JDK要求是1.8及其以上版本的

![image-20211129120046169](https://gitee.com/huangwei0123/image/raw/master/img/image-20211129120046169.png)

**下载RocketMQ安装包**

![image-20211129120121396](https://gitee.com/huangwei0123/image/raw/master/img/image-20211129120121396.png)

将下载的安装包上传到Linux、解压

![image-20211129120138200](https://gitee.com/huangwei0123/image/raw/master/img/image-20211129120138200.png)

#### 2、修改初始化内存

修改runserver.sh

使用vim命令打开bin\runserver.sh文件

![image-20211129142931044](https://gitee.com/huangwei0123/image/raw/master/img/image-20211129142931044.png)

修改runbroker.sh

使用vim命令打开bin\runbroker.sh文件

![image-20211129143010391](https://gitee.com/huangwei0123/image/raw/master/img/image-20211129143010391.png)

#### 3、启动

启动NameServer

```shell
nohup sh bin/mqnamesrv & 
tail -f ~/logs/rocketmqlogs/namesrv.log
```

![image-20211129143256196](https://gitee.com/huangwei0123/image/raw/master/img/image-20211129143256196.png)

启动broker

```shell
nohup sh bin/mqbroker -n localhost:9876 & 
tail -f ~/logs/rocketmqlogs/broker.log
```

![image-20211129143446613](https://gitee.com/huangwei0123/image/raw/master/img/image-20211129143446613.png)

#### 4、测试

发送消息

```sh
export NAMESRV_ADDR=localhost:9876 sh bin/tools.sh org.apache.rocketmq.example.quickstart.Producer
```

接收消息

```sh
sh bin/tools.sh org.apache.rocketmq.example.quickstart.Consumer
```

#### 5、关闭Server

无论是nameserver还是borker，都是使用bin/mqshutdown命令。

![image-20211129144429084](https://gitee.com/huangwei0123/image/raw/master/img/image-20211129144429084.png)

### 四、控制台的安装与启动

RocketMQ有一个可视化的dashboard，通过该控制台可以直观的查看到很多数据。

下载地址：https://github.com/apache/rocketmq-externals/releases 

![image-20211130110452263](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130110452263.png)

**修改配置**

修改其src/main/resource中的application.properties配置文件

- 原来的端口号为8080，修改为一个不常用的端口
- 指定RocketMQ的NameServer的地址

![image-20211130110947507](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130110947507.png)

**添加依赖**

在解压目录rocketmq-console的pom.xml中添加如下JAXB依赖。

> *JAXB*，*Java Architechture for Xml Binding*，用于*XML*绑定的*Java*技术，是一个业界标准，是一 项可以根据*XML Schema*生成*Java*类的技术。 

```xml
<dependency> 
    <groupId>javax.xml.bind</groupId> 
    <artifactId>jaxb-api</artifactId> 
    <version>2.3.0</version> 
</dependency> 
<dependency> 
    <groupId>com.sun.xml.bind</groupId> 
    <artifactId>jaxb-impl</artifactId> 
    <version>2.3.0</version> 
</dependency> 
<dependency> 
    <groupId>com.sun.xml.bind</groupId> 
    <artifactId>jaxb-core</artifactId> 
    <version>2.3.0</version> 
</dependency> 
<dependency> 
    <groupId>javax.activation</groupId> 
    <artifactId>activation</artifactId> 
    <version>1.1.1</version> 
</dependency>
```

**重新打包**

在rocketmq-console目录下运行maven的打包命令。

```sh
mvn clean package -Dmaven.test.skip=true
```

![image-20211130111233214](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130111233214.png)

**启动**

![image-20211130111253212](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130111253212.png)

```
java -jar rocketmq-console-ng-1.0.0.jar
```

**访问**

![image-20211130111336387](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130111336387.png)

### 五、集群搭建理论

![image-20211130111418709](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130111418709.png)

#### 1、数据复制与刷盘策略

![image-20211130112438093](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130112438093.png)

**复制策略**

复制策略是Broker的Master与Slave间的数据同步方式。分为**同步复制**和**异步复制**

- 同步复制：消息写入Master后，Master会等待slave同步数据成功后才向producer返回成功ACK
- 异步复制：消息写入Master后，master立即向producer返回成功ACK，无需等待salve同步数据成功

> 异步复制策略会降低系统的写入延迟，*RT*（Return Time）变小，提高了系统的吞吐量

**刷盘策略**

刷盘策略指的是Broker中消息的**落盘**方式，即消息发送到**broker内存**后**持久化到磁盘**的方式。分为同步刷盘和异步刷盘：

- 同步刷盘：当消息持久化到broker的磁盘后才算是消息写入成功
- 异步刷盘：当消息写入到broker内存后即表示消息写入成功，无需等待消息持久化到磁盘

> 1、异步刷盘策略会降低系统的写入延迟，RT变小，提高系统的吞吐量
>
> 2、消息写入到Broker的内存，一般是写入到了PageCache
>
> 3、对于异步刷盘策略，消息会写入到PageCache后立即返回成功ACK，但不会立即做落盘操作，而是当PageCache到达一定量时会自动进行落盘

#### 2、Broker集群模式

根据Broker集群中各个节点间关系的不同，Broker集群可以分为一下几类：

**单Master**

只有一个broker（其本质不能叫集群），这种方式也只能在测试时使用，生产环境下不能使用，因为存在单点问题。

**多Master**

broker集群仅由多个master构成，不存在slave。同一个Topic的各个Queue会平均分布在各个master节点上。

- 优点：配置简单，单个master宕机或重启维护对应用无影响，在磁盘配置为RAID10时，即使机器宕机不可恢复的情况下，由于RAID10磁盘非常可靠，消息也不会丢（异步刷盘丢失少量消息，同步刷盘一条不丢）性能最高
- 缺点：单台机器宕机期间，这台机器上未被消费的消息在机器恢复之前不可订阅（不可消费），消息实时性会受到影响。

> 以上优点的前提是，这些Master都配置了RAID磁盘阵列，如果没有配置，一旦出现某Master宕机，则会发送大量消息丢失的情况

**多Master多Slave模式-异步复制**

broker集群由多个master构成，每个master又配置多哥slave（在配置了RAID磁盘阵列的情况下，一个master一般配置一个slave即可）。master与slave的关系是**主备关系**，**即master负责处理消息的读写请求，而slave仅负责消息的备份与master宕机后的角色切换**。

**异步复制**即前面讲的 `复制策略`中的 `异步复制策略`,即消息写入master成功后，master立即向producer返回成功ACK，无需等待slave同步数据成功。

该模式的最大特点之一是，当master宕机后slave能够 `自动切换` 为master。不过由于slave从master的同步具有短暂的延迟（毫秒级），所以**当master宕机后，这种异步复制方式可能会存在少量消息的丢失问题。**

> slave 从master同步的延迟越短，其可能丢失的消息就越少
>
> 对于master的RAID磁盘阵列，若使用的也是异步复制策略，同样也存在延迟问题，同样也可能会丢失消息。但RAID阵列的秘诀是微秒级的（因为是由硬盘支持的），所以其丢失的数据量也会更少。



**多Master多Slave模式-同步双写**

该模式是`多Master多Slave模式`的`同步复制`实现。所谓`同步双写`,指的是消息写入master成功后，master会等待slave同步数据成功后才向producer返回成功ACK，即master与slave都要写入成功后才会返回成功ACK,也即`双写`

该模式与`异步复制模式`相比，优点事消息的安全性更高，不存在消息丢失的情况，但是单个消息的RT略高，从而导致性能要略低（**大约低10%**）

该模式存在一个大的问题：对于目前版本，master宕机后，slave`不会自动切换`到master

#### 最佳实践

**一般会为Master配置RAID10磁盘阵列，然后再为其配置一个Slave，即利用了RAID10磁盘阵列的高效、安全性、又解决了可能会影响订阅的问题。**

> 1、RAID磁盘阵列的效率要高于Master-Slave集群，因为RAID是硬件支持的。也是正因为如此，所以搭建RAID的成本较高。
>
> 2、多Master+RAID阵列，与多Master多Slave集群的区别是什么？
>
> - 多master+RAID阵列，其仅仅可以保证数据不丢失，即不影响消息写入，但其可能会影响到消息的订阅。但其执行效率要远高于`多master多slave集群`
> - 多master多slave集群，其不仅可以保证数据不丢失，也不会影响消息写入。其运行效率要低于`多master+RAID阵列`

#### RAID10

![image-20211130152844972](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130152844972.png)

RAID10是一个RAID1与RAID10的组合体，所以它继承了RAID0的快速和RAID1的安全

简单来说就是，先做`条带`,再做`镜像`。发即将进来的数据先分散到不同的磁盘，再讲磁盘中的数据做镜像。



### 六、集群搭建实战

**1、集群架构**

这里要搭建一个双主双从异步复制的Broker集群。为了方便，这里使用了两台主机来完成集群的搭建。

![image-20211130154042449](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130154042449.png)

2、克隆生成rocketmqOS1

克隆rocketmqOS主机，并修改配置，指定主机名为rocketmqOS1

3、修改rocketmqOS1配置文件

要修改的配置文件在rocketMQ解压目录的conf/2m-2s-async目录中

![image-20211130154154465](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130154154465.png)

**修改broker-a.properties**

将该配置文件内容修改如下：

![image-20211130154407261](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130154407261.png)

**修改broker-b-s.properties**

将该配置文件内容修改为如下：

![image-20211130154621990](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130154621990.png)

其他配置

除了以上配置外，这些配置文件中还可以设置其他属性

![image-20211130155202290](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130155202290.png)

**4、克隆生成rocketmqOS2**

克隆rocketmqOS1主机，并修改配置。指定主机名为rocketmqOS2。

5、修改rocketmqOS2配置文件

对于rocketmqOS2主机，同样需要修改rocketMQ解压目录的conf目录2m-2s-async中的两个配置文件。

**修改broker-b.properties**

将该配置文件内容修改如下：

```properties
brokerClusterName=DefaultCluster
brokerName=broker-b
brokerId=0
deleteWhen=04
fileReservedTime=48
brokerRole=ASYNC_MASTER
flushDiskType=ASYNC_FLUSH
namesrvAddr=192.168.59.164:9876;192.168.59.165:9876
```

**修改broker-a-s.properties**

将该配置文件内容修改为如下：

![image-20211130155504685](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130155504685.png)

6、启动服务器

**启动NameServer集群**

分别启动rocketmqOS1与rocketmqOS2两个主机中NameServer。

启动命令完全相同

```sh
nohup sh bin/mqnamesrv & 
tail -f ~/logs/rocketmqlogs/namesrv.log
```

**启动两个Master**

分别启动rocketmqOS1与rocketmqOS2两个主机中的broker master。注意，它们指定索要加载的配置文件时不同的。

```sh
nohup sh bin/mqbroker -c conf/2m-2s-async/broker-a.properties &
tail -f ~/logs/rocketmqlogs/broker.log
```

```sh
nohup sh bin/mqbroker -c conf/2m-2s-async/broker-b.properties &
tail -f ~/logs/rocketmqlogs/broker.log
```

**启动两个Slave**

分别启动rocketmqOS1与rocketmqOS2两个主机中的broker salve。注意，他们指定的所要加载的配置文件时不同的。

```sh
nohup sh bin/mqbroker -c conf/2m-2s-async/broker-b-s.properties & 
tail -f ~/logs/rocketmqlogs/broker.log
```

```sh
nohup sh bin/mqbroker -c conf/2m-2s-async/broker-a-s.properties & 
tail -f ~/logs/rocketmqlogs/broker.log
```

### 七、mqadmin命令

**修改bin/tools.sh**

在运行mqadmin命令之前，先要修改mq解压目录下bin\tools.sh配置的JDK的ext目录，本机的ext目录在/usr/java/jdk1.8.0_161/jre/lib/etx

使用vim命令打开tools.sh文件，并在JAVA_OPT配置的—Djava.ext.dirs这一行后面添加ext的路径

![image-20211130160259044](https://gitee.com/huangwei0123/image/raw/master/img/image-20211130160259044.png)

**运行mqadmin**

直接运行该命令，可以看到其可以添加的commands。通过这些commands可以完成很多的功能。

**该命令的官网详解**

该命令在官网中有详细的用法解释

https://github.com/apache/rocketmq/blob/master/docs/cn/operation.md



## 第3章 RocketMQ工作原理

### 一、消息的生产

#### 1、消息生产过程

Producer可以将消息写入到某Broker中的某Queue中，其经历了如下过程

- Producer发送消息之前，会先向NameServer发出获取`消息Topic的路由信息`的请求
- NameServer返回该Topic的`路由表`和`Broker列表`
- Producer根据代码中指定的`Queue选择策略`，从Queue列表中选择一个队列，用于后续存储消息
- Producer对消息做出一些特殊处理，例如:消息本身超过4M，则会对其进行压缩
- Producer向选择出的Queue所在的Broker发出`RPC请求`，将消息发送到选择出的Queue

> 路由表：实际上是一个Map。key为Topic名称，value是一个QueueData实例列表。QueueData并不是一个Queue对应一个QueueData，而是一个Broker中该Topic的所有Queue对应一个QueueData。即，只要涉及到该Topic的Broker，一个Broker对应一个QueueData。QueueData中包含brokerName。
>
> 简单来说，路由表的key为topic名称，value则为所有涉及该Topic的BrokerName列表
>
> Broker列表：其实际也是一个Map。key为brokerName，value为BrokerData。
>
> 一个Broker对应一个BrokerData实例，对吗？不对，一套brokerName名称相同的Master-slave小集群对应一个BrokerData。BrokerData中包含brokerName以及一个map。该map的key为brokerId，value为该broker对应的地址。brokerId为0表示该broker为Master，非0表示slave

#### 2、Queue选择算法

对于无序消息，其Queue选择算法，也称消息投递算法，常见的有两种：

**轮询算法:**

默认选择算法，该算法保证了每个Queue中可以均匀的获取到消息。

> 该算法存在一个问题：由于某些原因，在某些Broker上的Queue可能投递延迟较严重。从而导致Producer的缓存队列中出现较大的消息积压，影响消息的投递性能。

**最小投递延迟算法：**

该算法会统计每次消息投递的时间延迟，然后根据统计出的结果将消息投递到时间延迟最小的Queue，如果延迟相同，则采用轮询算法投递。该算法可以有效提升消息的投递性能。

> 该算法也存在一个问题:消息在Queue上的分配不均匀。投递延迟小的Queue其可能会存在大量的消息，而对该Queue的消费者压力会增大，降低消息的消费能力，可能会导致MQ中消息的堆积。

### 二、消息的存储

RocketMQ中的消息存储在本地文件系统中，这些相关文件默认在当前用户主目录里下的store目录中。

![image-20211201085752355](https://gitee.com/huangwei0123/image/raw/master/img/image-20211201085752355.png)

abort：该文件在Broker启动后会自动创建，正常关闭Broker，该文件会自动消失。若在没有启动

Broker的情况下，发现这个文件是存在的，则说明之前Broker的关闭是非正常关闭。

checkpoint：其中存储着commitlog、consumequeue、index文件的最后刷盘时间戳

commitlog：其中存放着commitlog文件，而消息是写在commitlog文件中的

conæg：存放着Broker运行期间的一些配置数据

consumequeue：其中存放着consumequeue文件，队列就存放在这个目录中

index：其中存放着消息索引文件indexFile 

lock：运行期间使用到的全局资源锁

#### 1、commitlog文件

> 说明：在很多资料中commitlog目录中得文件简称为commitlog文件，但在源码中，该文件被命名为mappedFile

**目录与文件**

commitlog目录存放着很多得到mappedFile文件，当前Borker中得所有消息都是落盘到这些mappedFile文件中的。mappedFile文件大小为1G(大小等于1G),文件名由20位十进制书构成，表示当前文件的第一条消息的起始位偏移量。

> 第一个文件名一定是20位0构成的，因为第一个文件的第一条消息的偏移量commitlog offset 为 0 
>
> 当第一个文件放满时，则会自动生成第二个文件继续存放消息。假设第一个文件大小是 1073741820字节（1G = 1073741824字节），则第二个文件名就是00000000001073741824。
>
> 以此类推，第n个文件名应该是前n-1个文件大小之和。
>
> 一个Broker中所有mappedFile文件的commitlog offset是连续的

需要注意的是，一个Broker中仅包含一个commitlog目录，**所有的mappedFile文件都是存放在该目录中的**，即无论当前Broker中存放着多少Topic的消息，这些消息都是被顺序写入到了mappedFile文件中的，也就是说，**这些消息在Broker中存放时并没有被按照Topic进行分类存放。**

> mappedFile文件时顺序读写的文件，所有其访问效率很高
>
> 无论时SSD磁盘还是SATA磁盘，通常情况下，顺序存取效率都会高于随机存取

**消息单元**

![image-20211208220330020](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208220330020.png)

mappedFile文件内容由一个个的`消息单元`构成，每个消息单元中包含消息总长度MsgLen、消息的物理位置physicalOffset、消息内容Body、消息体查长度BodyLenth、消息主题Topic、Topic长度TopicLenth、消息生产者BornHost、消息发送时间戳BornTimestamp、消息所在队列QueueId、消息在Queue中存储的偏移量QueueOffset等近20余项相关属性

> 需要注意到，消息单元中是包含Queue相关属性的。所以，我们在后续的学习中，就需要十分留意commitlog与queue间的关系是什么？
>
> 一个mappedFile文件中第m+1个消息单元的commitlog offset偏移量
>
> L(m+1) = L(m) + MsgLen(m) (m >= 0)



#### 2、consumequeue

![image-20211208220941089](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208220941089.png)

**目录与文件**

![image-20211208221025397](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208221025397.png)

为了提高效率，会为每个Topic在~/store/consumequeue中创建一个目录，目录名为Topic名称。在该Topic目录下，会再为每个该Topic的Queue建立一个目录，目录名为queueId。每个目录中存放着若干consumequeue文件，consumequeue文件是commitlog的索引文件，可以根据consumequeue定位到具
体的消息。

consumequeue文件名也由20位数字构成，表示当前文件的第一个索引条目的起始位移偏移量。与mappedFile文件名不同的是，其后续文件名是固定的。因为consumequeue文件大小是固定不变的。

**索引条目**

![image-20211208221243607](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208221243607.png)

每个consumequeue文件可以包含30w个索引条目，每个索引条目包含了三个消息重要属性：消息在mappedFile文件中的偏移量CommitLog Offset、消息长度、消息Tag的hashcode值。这三个属性占20个字节，所以每个文件的大小是固定的30w * 20字节

> 一个consumequeue文件中所有消息的Topic一定是相同的。但每条消息的Tag可能是不同

#### 3、对文件的读写

![image-20211208221338145](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208221338145.png)

**消息写入**

一条消息进入到Broker后经历了一下几个过程才最终被持久化

- Broker根据queueId，获取到该消息对应索引条目要在consumequeue目录中写入偏移量，即QueueOffset
- 将ququeId、queueOffset等数据，与消息一起封装为消息单元
- 将消息单元写入到commitlog
- 同时，形成消息索引条目
- 将消息索引条目分发到相应的consumequeue



**消息拉取**

- 当Consumer来拉取消息时会经历以下几个步骤：

  1、Consumer获取到其要消费消息所在的Queue的`消费偏移量offset`，计算出其要消费消息的`消息offset`

  > 消费offset即消费进度，consumer对某个Queue的消费offset，即消费到了该Queue的第几条消息
  >
  > 消息offset = 消费offset + 1

  2、Consumer向Broker发送拉取请求，其中会包含其要拉取消息的Queue、消息offset及消息Tag

  3、Broker计算在该consumequeue中的queueOffset

  > queueOffset = 消息offset * 20字节

  4、从该queueOffset处开始向后查找第一个指定Tag的索引条目

  5、解析该索引条目的前8个字节，即可定位到该消息在commitlog中的commitlog offset

  6、对应commitlog offset中读取消息单元，并发送给consumer

**性能提升**

RocketMQ中，无论是消息本身还是消息索引，都是存储在磁盘上的。其不会影响消息的消费吗？当然不会。其实RocketMQ的性能在目前的MQ产品中性能是非常高的。因为系统通过一系列相关机制大大提升了性能

首先，RocketMQ对文件的读写操作是通过`mmap零拷贝`进行的，**将对文件的操作转化为直接对内存地址进行操作**，从而极大地提高了文件的读写效率

其次，**consumequeue中的数据是顺序存放的**，还引入了`PageCache的预读取机制`，**使得对consumequeue文件的读取几乎接近于内存读取，即使在有消息堆积情况下也不会影响性能**。

> PageCache机制，页缓存机制，是OS对文件的缓存机制，用于加速对文件的读写操作。
>
> 一般来说，程序对文件进行顺序读写的速度几乎接近于内存读写速度，主要原因是由于OS使用PageCache机制对读写访问操作进行性能优化，将一部分的内存用作PageCache。
>
> - 写操作：OS会先将数据写入到PageCache中，随后会以异步方式由pdflush（page dirty flush)内核线程Cache中的数据刷盘到物理磁盘
> - 读操作：若用户要读取数据，其首先会从PageCache中读取，若没有命中，则OS在从物理磁盘上加载该数据到PageCache的同时，也会顺序对其相邻数据块中的数据进行**预读取**

**RocketMQ中可能会影响性能的是对commitlog文件的读取**。因为对commitlog文件来说，**读取消息时会产生大量的随机访问，而随机访问会严重影响性能**。不过，如果选择合适的系统IO调度算法，

比如设置调度算法为Deadline（采用SSD固态硬盘的话），随机读的性能也会有所提升。

#### 4、与Kafka的对比

RocketMQ的很多思想来源于Kafka，其中commitlog与consumequeue

RocketMQ中commitlog目录与consumequeue的结合就类似于kafka中partition分区目录，mappedFile文件就类似于Kafka中的segment段。

> Kafka中的Topic的消息被分割为一个或多个partition。partition是一个物理概念，对应到系统上就是topic目录下的一个或多个目录。每个partition中包含的文件称segment，是具体存放消息的文件
>
> Kafka中消息存放的目录结构是：topic目录下有partition目录，partition目录下有segment
>
> Kafka中没有二级分类标签Tag这个
>
> Kafka中无需索引文件。因为生产者是将消息直接写在了partition中的，消费者也是直接从partition中读取数据的



### 三、indexFile

除了通过通常的指定Topic进行消息消费外，RocketMQ还提供了根据key进行消息查询的功能。该查询是通过store目录中的index子目录中的indexFile进行索引实现的快速查询。当然，这个indexFile中的索引数据是在`包含了key的消息`被发送到Broker时写入的。如果消息中没有包含key，则不会写入。

#### 1、索引条目结构

每个Broker中会包含一组indexFile，每个indexFile都是以一个`时间戳`命名的（这个indexFile被创建时的时间戳）。每个indexFile文件由三部分构成：indexHeader，slots槽位，indexes索引数据。每个indexFile文件中包含500w个slot槽。而每个slot槽又可能会挂载很多的index索引单元。

![image-20211208224544956](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208224544956.png)

indexHeader固定40个字节，其中存放着如下数据：

![image-20211208224558557](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208224558557.png)

- beginTimestamp：该indexFile中第一条消息的存储时间
- endTimestamp：该indexFile中最后一条消息存储时间
- beginPhyoffset：该indexFile中第一条消息在commitlog中的偏移量commitlog offset
- endPhyoffset：该indexFile中最后一条消息在commitlog中的偏移量commitlog offset
- hashSlotCount：已经填充有index的slot数量（并不是每个slot槽下都挂载有index索引单元，这里统计的是所有挂载了index索引单元的slot槽的数量）
- indexCount：该indexFile中包含的索引单元个数（统计出当前indexFile中所有slot槽下挂载的所有index索引单元的数量之和）

indexFile中最复杂的是Slots与Indexes间的关系。在实际存储时，Indexes是在Slots后面的，但为了便于理解，将它们的关系展示为如下形式：

![image-20211208224809208](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208224809208.png)

**key的hash值 % 500w的结果即为slot槽位**，然后将该slot值修改为该index索引单元的indexNo，根据这个indexNo可以计算出该index单元在indexFile中的位置。

不过，该取模结果的重复率是很高的，为了解决该问题，在每个index索引单元中增加了preIndexNo，用于指定该slot中当前index索引单元的前一个index索引单元。

而slot中始终存放的是其下最新的index索引单元的indexNo，这样的话，只要找到了slot就可以找到其最新的index索引单元，而通过这个index索引单元就可以找到其之前的所有index索引单元

> indexNo是一个在indexFile中的流水号，从0开始依次递增。即在一个indexFile中所有indexNo是以此递增的。indexNo在index索引单元中是没有体现的，其是通过indexes中依次数出来的。

index索引单元默写20个字节，其中存放着以下四个属性：

![image-20211208225018596](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208225018596.png)

- keyHash：消息中指定的业务key的hash值
- phyOffset：当前key对应的消息在commitlog中的偏移量commitlog offset
- timeDiff：当前key对应消息的存储时间与当前indexFile创建时间的时间差
- preIndexNo：当前slot下当前index索引单元的前一个index索引单元的indexNo



#### 2、indexFile的创建

indexFile的文件名为当前文件被创建时的时间戳。这个时间戳有什么用处

根据业务key进行查询时，查询条件除了key之外，还需要指定一个要查询的时间戳，表示要查询不大于该时间戳的最新的消息，

即查询指定时间戳之前存储的最新消息。这个时间戳文件名可以简化查询，提高查询效率。具体后面会详细讲解。

indexFile文件是何时创建的？其创建的条件（时机）有两个：

- 当第一条带key的消息发送来后，系统发现没有indexFile，此时创建第一个indexFile文件
- 当一个indexFile中挂载的index索引单元数量超过2000w个时，会创建新的indexFile。当带key的消息发送到来后，系统会找到最新的indexFile，并从其indexHeader中的最后4字节中读取到indexCount。若indexCount >= 2000w时，会创建新的indexFile

> 由于可以推算出，一个indexFile的最大大小是：(40 + 500w * 4 + 2000w * 20)字节



#### 3、查询流程

当消费者通过业务key来查询相应的消息时，其需要经过一个相对较复杂的查询流程。不过，在分析查询流程之前，首先要清楚几个定位计算式子

> 计算指定消息key的slot槽位序号：
> slot槽位序号 = key的hash % 500w

> 计算槽位序号为n的slot在indexFile中的起始位置：
> slot(n)位置 = 40 + (n - 1) * 4

> 计算indexNo为m的index在indexFile中的位置：index(m)位置 = 40 + 500w * 4 + (m - 1) * 2

40为indexFile中indexHeader的字

500w * 4 是所有slots所占的字

具体查询流程如下

![image-20211208230210017](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208230210017.png)

### 四、消息的消费

消费者从Broker中获取消息的方式有2种

- pull拉取方式
- push推送方式

消费者组对于消息消费的模式又分为2种：

- 集群消费Clustering
- 广播消费Broadcasting

#### 1、获取消费类型

**拉取式消费**

Consumer主动从Broker中拉取消息，主动权由Consumer控制，一旦获取了批量消息，就会启动消费过程。

不过，该方式的实时性比较弱，即Broker中有了新的消息时消费者并不能及时发现并消费。

> 由于拉取时间间隔是由用户指定的，所以在设置该间隔时需要注意平稳；
>
> 间隔太短，空请求比例会增加；间隔太长，消息的实时性太差

**推送式消费**

该模式下Broker收到数据后会主动推送给Consumer，该获取方式一般实时性比较高。

该获取方式是典型的`发布-订阅`模式，即Consumer向其关联的Queue注册了监听器，一旦发现有新的消息来到就会触发回调的执行，回调方法是Consumer去Queue中拉取消息，**而这些都是基于Consumer与Broker间的长连接。长连接得到维护时需要消耗系统资源的。**

对比：

- pull : 需要应用去实现对关联Queue的遍历，实时性差，但便于应用控制消息的拉取
- push：封装了对关联Queue的遍历，实时性强，但会占用较多的系统资源

#### 2、消费模式

**广播消费**

![image-20211208232009183](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208232009183.png)

广播消费模式下，相同Consumer Group组每个Consumer实例都接收同一个Topic的全量消息，即每条消息都会被发送到Consumer Group中的`每个`Consumer

**集群消费**

![image-20211208232632798](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208232632798.png)

集群消费模式下，相同Consumer Group的每个Consumer实例`平均分摊`同一个Topic下的消息，即每条消息只会被发送到Consumer Group中的`某个`Consumer

**消息保存进度**

- 广播模式：消费进度保存在Consumer端，因为广播模式下consumer group中每个consumer都会消费所有消息，但他们的消费进度是不同的。所以consumer各自保存各自的消费进度。
- 集群模式：消费进度保存在broker中，consumer group中的所有consumer共同消费一个Topic中的消息，**同一条消息只会被消费一次，消费进度会参与到了消费的负载均衡中，故消费进度是需要共享的**，下图是broker中存放的各个Topic的各个Queue的消费进度。

![image-20211208233350020](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208233350020.png)

#### 3、Rebalance机制

Rebalance机制讨论的前提是：集群消费

**什么是Rebalance**

Rebalance再均衡，指的是，**将一个Topic下的多个Queue在同一个Consumer Group中的多个Consumer间进行重新分配的过程。**

![image-20211208233710437](https://gitee.com/huangwei0123/image/raw/master/img/image-20211208233710437.png)

**Rebalance机制的本意是为了提升消息的`并行消费能力`。**

例如：一个Topic下5个队列，在只有1个消费者的情况下，这个消费者将负责消费这5个队列的消息。如果此时我们增加一个消费者，那么就可以给其中一个消费者分配2个队列，给另外一个消费者分配3个队列，从而提升消息的并行消费的能力。

**Rebalance限制**

**由于一个队列最多分配给一个消费者，因此当某个消费者下的消费实例数量 > 队列的数量是，多余的消费者实例将分配不到任何队列。**

**Rebalance危害**

Rebalance在提升消息消费能力的同时，也带来一些问题：

**消费暂停**：只有一个Consumer时，其负责消费所有队列；在新增了一个Consumer后回触发Rebalance的发生。此时原Consumer就需要暂停部分队列的消费，等到这些队列分配给新的Consumer后，这些暂停消费的队列才能继续被消费

**消费重复**：Consumer在消费新分配给自己的队列的时候，必须接着之前Consumer提交的消费进度的offset继续消费。然而默认情况下，offset是异步提交的，这个**异步性导致提交**到Broker的offset与Consumer实际消费的消息并不一致

（可能Broker消费了0-100，在100段提交了，后又继续消费，而消费进度还只是在100段），这个不一致的差值就是可能会重复消费的消息。

> 同步提交：consumer提交了其消费完毕的一批消息的offset给broker后，**需要等待broker的成功ACK**。当收到ACK后，consumer才会继续获取并消费下一批消息。在等待ACK期间，consumer是阻塞的
>
> 异步提交：consumer提交了其消费完毕的一批消息的offset给broker后，**不需要等待broker的成功ACK**。consumer可以直接获取并消费下一批消息。
>
> 对于一次性读取消息的数量，需要根据具体业务场景选择一个相对均衡的是很有必要的。
>
> 因为数量过大，系统性能提升了，但产生重复消费的消息数量可能会增加；数量过小，系统性能会下降，但被重复消费的消息数量可能会减少

**消息突刺**：由于Rebalance可能导致重复消费，如果需要重复消费的消息过多，或者因为Rebalance暂停时间过长从而导致积压了部分消息。那么有可能会导致在Rebalance结束之后瞬间需要消费很多消息。

**Rebalance产生的原因**

导致Rebalance产生的原因，无非就是两个：

1、消费者所订阅Topic的Queue数量发生变化

2、消费者组中消费者数量发生变化

> 1、Queue数量发生变化的场景
>
> - Broker扩容或缩容
> - Broker升级运维
> - Broker与NameServer间的网络异常
> - Queue扩容或者缩容
>
> 2、消费者数量发生变化的场景
>
> - consumer group扩容或缩容
> - consumer 升级运维
> - consumer 与 NameServer 间网络异常

**Rebalance过程**

在Broker中维护着多个Map集合，这些集合中动态存放着当前Topic中Queue的信息，**Consumer Group中Consumer实例的消息。一旦发现消费者所订阅的Queue数量发生变化，或消费者组中消费者的数量发生变化，立即向Consumer Group中的每个实例发出Rebalance通知**

> TopicConfigManager：key是topic名称，value是TopicConfig。TopicConfig中维护着该Topic中所有的Queue数据
>
> ConsumerManager : key是Consumer Group Id，value是ConsumerGroupInfo。
>
> ConsumerGroupInfo中维护着该Group中所有Consumer实例数据
>
> ConsumerOffsetManager：key为`Topic与 订阅该Topic的Group组合，即topic@group`，value是一个内层Map，内层Map的key为QueueId，内层Map的Value为该Queue的消费进度offset。

Consumer实例在接收到通知后会采用`Queue分配算法`自己获取到相应的Queue，即由Consumer实例自主进行Rebalance

**与Kafka对比**

在Kafka中，一旦发现出现了Rebalance条件，Broker会调用Group Coordinator来完成Rebalance。Coordinator是Broker中的一个进程**，Coordinator会在Consumer Group中选出一个Group Leader**。由这个Leader根据自己本身组情况完成Partition分区的再分配，这个再分配结果会上报给Coordinator，并由Coordinator同步给Group中的所有Consumer实例。

**Kafka中的Rebalance是由Consumer Leader完成的，而RocketMQ中的Rebalance是由每个Consumer自己完成的，Group中不存在Leader**

#### 4、Queue分配算法

一个Topic中的Queue只能由Consumer Group中的一Consumer进行消费，而一个Consumer可以同时消费多个Queue中的消息。那么Queue与Consumer间的配对关系是如何确定的，

即Queue要分配给哪个Consumer进行消费，也是有算法策略的。常见的有四种策略。这些策略是通过在创建Consumer时的
构造器传进去的。

**平均分配策略**

![image-20211209233253356](https://gitee.com/huangwei0123/image/raw/master/img/image-20211209233253356.png)

该算法是要根据 `avg = QueueCout / ConsumerCount`的鸡算结果进行分配的。如果能够整除，则按顺序将avg个Queue逐个分配Consumer；如果不能整除，则将多余的Queue按照Consumer顺序逐个分配。

> 该算法即，先计算号每个Consumer应该分得几个Queue，然后再依次将这些数量的Queue逐个分配给Consumer

**环形平均策略**

![image-20211209233519319](https://gitee.com/huangwei0123/image/raw/master/img/image-20211209233519319.png)

环形平均算法是指，根据消费者的顺序，依次在由queue队列组成的环形图中逐个分配。

> 该算法不用事先计算每个Consumer需要分配几个Queue，直接一个一个分配即可。

**一致性hash策略**

![image-20211209233639096](https://gitee.com/huangwei0123/image/raw/master/img/image-20211209233639096.png)

该算法会将Consumer的hash值作为Node节点存放到hash环上，然后将queue的hash值也放到hash环上，通过`顺时针`方向，距离queue最近的那个consumer就是该queue要分配的consumer。

> 该算法存在的问题：分配不均

**同机房策略**

![image-20211209233846198](https://gitee.com/huangwei0123/image/raw/master/img/image-20211209233846198.png)

该算法会**根据queue的部署机房位置和consumer的位置**，过滤出**当前consumer相同机房的queue**。然后按照平均分配策略或环形平均策略对同机房queue进行分配。如果没有同机房的queue，则按照平均分配策略或环形平均策略对所有queue进行分配。

**对比**

一致性hash算法存在的问题：

两种平均分配策略的分配效率较高，一致性hash策略的较低。

因为一致性hash算法比较复杂，另外，一致性hash策略分配的结果也很大可能存在不平均的情况

一致性hash算法存在的意义：

**其可以有效减少由于消费者组扩容或缩容所带来的大量的Rebalance**

![image-20211210222644161](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210222644161.png)

![image-20211210222720021](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210222720021.png)

**一致性hash算法应用场景：**

**Consumer数量变化比较频繁的场景。**

**5、至少一次原则**

RocketMQ有一个原则：每条消息必须要被`成果消费`一次

那 什么是成功消费呢？

Consumer在消费完消息后会向其`消费进度记录器`提交其消费信息的offset，offset被成功记录到记录器中，那么这条消息就表示被成功消费了。

> 什么是消费进度记录器？
>
> 对于广播消费模式来说，Consumer本身就是消息进度记录器
>
> 对于集群消费来说，Broker就是消息进度记录器



### 五、订阅关系的一致性

订阅关系的一致性指的是，**同一个消费者组（Group ID相同）下所有Consumer实例所订阅的Topic与Tag及对消息的处理逻辑必须完全一致**。否则，消息消费得到逻辑就会混乱，甚至导致消息丢失。

#### 1、正确订阅关系

多个消费者组订阅了多个Topic，并且对每个消费者组里的多个消费者实例的订阅关系保持了一致。

![image-20211210223403031](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210223403031.png)

#### 2、错误订阅关系

一个消费者组订阅了多个Topic，但是该消费者组里的多个Consumer实例订阅关系并没有保持一致。

![image-20211210223548119](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210223548119.png)

**订阅了不同Topic**

该例中的错误在于，同一个消费者组里面的两个Consumer实例订阅了不同的Topic

Consumer实例1-1 （订阅了topic为jodie_test_A，tag为所有的消息）

```java
Properties properties = new Properties();

properties.put(PropertyKeyConst.GROUP_ID, "GID_jodie_test_1");

Consumer consumer = ONSFactory.createConsumer(properties);

consumer.subscribe("jodie_test_A", "*", new MessageListener() {
    public Action consume(Message message, ConsumeContext context) {
        System.out.println(message.getMsgID());
        return Action.CommitMessage;
    }
});
```

Consumer实例1-2：（订阅了topic为jodie_test_B，tag为所有消息）

```java
Properties properties = new Properties();

properties.put(PropertyKeyConst.GROUP_ID, "GID_jodie_test_1");

Consumer consumer = ONSFactory.createConsumer(properties);

consumer.subscribe("jodie_test_B", "*", new MessageListener() {
    public Action consume(Message message, ConsumeContext context) {
        System.out.println(message.getMsgID());
        return Action.CommitMessage;
    }
});
```



**订阅了不同的Tag**

该实例中的错误在于，同一个消费者组中的两个Consumer订阅了相同Topic的不同Tag

Consumer实例2-1：（订阅了topic为jodie_test_A，tag为TagA的消息）

```java
Properties properties = new Properties();

properties.put(PropertyKeyConst.GROUP_ID, "GID_jodie_test_2");

Consumer consumer = ONSFactory.createConsumer(properties);

consumer.subscribe("jodie_test_A", "TagA", new MessageListener() {
    public Action consume(Message message, ConsumeContext context) {
        System.out.println(message.getMsgID());
        return Action.CommitMessage;
    }
});
```

Consumer实例2-2：（订阅了topic为jodie_test_A，tag为所有的消息）

```java
Properties properties = new Properties();

properties.put(PropertyKeyConst.GROUP_ID, "GID_jodie_test_2");

Consumer consumer = ONSFactory.createConsumer(properties);

consumer.subscribe("jodie_test_A", "*", new MessageListener() {
    public Action consume(Message message, ConsumeContext context) {
        System.out.println(message.getMsgID());
        return Action.CommitMessage;
    }
});
```



**订阅了不同数量的Topic**

该实例中的错误在于，同一个消费者组的两个Consumer订阅了不同数量的Topic

Consumer实例3-1：（该Consumer订阅了两个Topic）

```java
Properties properties = new Properties();

properties.put(PropertyKeyConst.GROUP_ID, "GID_jodie_test_3");

Consumer consumer = ONSFactory.createConsumer(properties);

consumer.subscribe("jodie_test_A", "TagA", new MessageListener() {
    public Action consume(Message message, ConsumeContext context) {
        System.out.println(message.getMsgID());
        return Action.CommitMessage;
    }
});

consumer.subscribe("jodie_test_B", "TagB", new MessageListener() {
    public Action consume(Message message, ConsumeContext context) {
        System.out.println(message.getMsgID());
        return Action.CommitMessage;
    }
});
```

Consumer实例3-2：（该Consumer订阅了一个Topic）

```java
Properties properties = new Properties();

properties.put(PropertyKeyConst.GROUP_ID, "GID_jodie_test_3");

Consumer consumer = ONSFactory.createConsumer(properties);

consumer.subscribe("jodie_test_A", "TagB", new MessageListener() {
    public Action consume(Message message, ConsumeContext context) {
        System.out.println(message.getMsgID());
        return Action.CommitMessage;
    }
});
```



### 六、Offset管理

> 这里的offset指的是Consumer的消费进度offset

消费进度offset是用来记录每个Queue的不同消费组的消费进度的。根据消费进度记录器的不同，可以分为两种模式：本地模式和远程模式

#### 1、offset本地管理模式

当消费模式为`广播消费`时，offset使用本地模式存储。因为每条消息会被所有的消费者消费，每个消费者管理自己的消费进度，各个消费者之间不存在消费进度的交集。

Consumer在广播消费模式下offset相关数据以json的形式持久化到Consumer本地磁盘文件中，默认文件路径为当前用户主目录下的 `.rocketmq_offsets/${clientId}/${group}/Offsets.json `

其中 `${clientId}`为当前消费者id，默认为 ip@DEFAULT;  `${group}`消费者组名称

#### 2、offset远程管理模式

当消费模式为`集群消费`时，offset使用远程管理模式。因为所有的Consumer实例对消息采用是负载均衡消费，所有Consumer共享Queue的消费进度。

Consumer在集群消费模式下offset相关数据以json的形式持久化到Broker磁盘中，文件路径为当前用户主目录下的`store/config/consumerOffset.json`

Broker启动时会加载这个文件，并写入到一个双层Map（ConsumerOffsetManager中）。外层Map的key为`topic@group`，value为内层map。内层map的key为queueId，value为offset。当发生Rebalance时，新的Consumer会从该Map中获取到对应数据来继续消费。

**集群模式下采用offset远程管理模式，主要是为了保证Rebalance机制**

#### 3、offset用途

消费者时如何从最开始持续消费消息的？消费者要消费的第一条消息的起始位置时用户自己通过

`consumer.setConsumerFromWhere()`方法指定的。

在Consumer启动后，其要消费的第一条消息的起始位置常有三种，这三种位置可以通过枚举类型常量设置。

这个枚举类型为ConsumerFromWhere

![image-20211210230333239](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210230333239.png)

```
CONSUME_FROM_LAST_OFFSET：从queue的当前最后一条消息开始消费
CONSUME_FROM_FIRST_OFFSET：从queue的第一条消息开始消费
CONSUME_FROM_TIMESTAMP：从指定的具体时间戳位置的消息开始消费。这个具体时间戳是通过另外一个语句指定的 。
consumer.setConsumeTimestamp(“20210701080000”) yyyyMMddHHmmss
```

当消费完一批消息后，Consumer会提交其消费进度offset给Broker，**Broker在收到消费进度后会将其更新到那个双层Map（ConsumerOffsetManager）及consumerOffset.json文件中，**

然后向该**Consumer进行ACK**，而ACK内容包含三项数据：当前消费队列的最小offset(minOffset)、最大offset（maxOffset）、及下次消费的起始offset（nextBeginOffset）。

#### 4、重试队列

![image-20211210230728497](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210230728497.png)

当rocketMQ对消息的消费出现异常时，**会将发生异常的消息的offset提交到Broker中的重试队列**。系统发生消息消费异常时会为当前的`topic@group`创建一个**重试队列**，该队列以`%retry%开透`，到达重试时间后开始进行消费重试。

#### 5、offset的同步提交与异步提交

集群消费模式下，Consumer消费完消息后，会向Broker提交消费进度offset，其提交方式分为两种：

`同步提交` ：消费者在消费完一批消息后会向broker提交这些消息的offset，然后等待broker的成功响应。若在等待超时之前收到了**成功响应**，则继续读取下一批消息进行消费（**从ACK中获取nextBeginOffset**）。如果**没有收到响应**，则会**重新提交**，直到获取到响应。**而在这个等待过程中，消费者时阻塞的。其严重影响了消费者的吞吐量。**

`异步提交` ： 消费者在消费完一批消息后向broker提交offset，但**无需等待**Broker的成功响应，可以继续读取并消费下一批消息。这种方式**增加了消费者的吞吐量**。但需要注意，broker在收到提交的offset后，还是会向消费者进行响应的。**可能还没有收到ACK，此时Consumer会从Broker中直接获取nextBeginOffset**

### 七、消费幂等
