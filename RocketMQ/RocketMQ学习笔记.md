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



