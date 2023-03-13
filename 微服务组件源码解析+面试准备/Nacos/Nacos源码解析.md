# Nacos源码解析 

版本：

- springCloud Alibaba 2.2.3.RELEASE
- nacos 1.3.3

- Spring Cloud Hoxton.SR3
- springboot 2.2.5.RELEASE

版本对应信息可在github上查询 ： https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E



## 1、Nacos Client 源码

#### 1.1 Nacos的系统架构

![image-20230312192926896](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/imgimage-20230312192926896.png)

sidecar，是从SCA2.1.1版本后增加的模块，是一种多语言异构模块，用于实现其它语言使用SCA相关组 件的模块。

#### 1.2 数据模型

`Name Server`用于记录各个命名空间`namespace`中的实例信息。`namespace`、`group`与服务`service`或资 源`dataId`间的关系如下图。

![image-20230312193039173](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/imgimage-20230312193039173.png)

groupId@@微服务名称@@clusterName

注意，图中的Service不是一个简单的服用提供者，而是很多提供者实例的集合。而这个集合中的提供 者又可以分属于很多的Cluster。

![image-20230312193409140](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/imgimage-20230312193409140.png)

#### 1.3 临时实例与持久实例 

在服务注册时有一个属性ephemeral用于描述当前实例在注册时是否以临时实例出现。为true则为临时 实例，默认值；为false则为持久实例。

![image-20230312193504473](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/imgimage-20230312193504473.png)

区别：

临时实例与持久实例的实例的存储位置与健康检测机制是不同的。

- ==临时实例==：默认情况。服务实例仅会注册在Nacos内存，不会持久化到Nacos磁盘。其健康检测机制为Client模式，即Client主动向Server上报其健康状态（类似推模式）。默认心跳间隔为5秒。在15秒内Sever未收到Client的心跳，则会将其标记为“不健康”状态；在30秒内若收到了Client心跳，则重新恢复“健康状态”，否则该实例将会从Server端内存清除。即对于不健康的实例，Server会自动清除
- ==持久实例==：服务实例不仅会注册到Nacos内存，同时也会被持久化到Nacos磁盘。其健康检测机 制为Server模式，即Server会主动去检测Client的健康状态（类似于拉模式），默认每20秒检测一 次。健康检测失败后服务实例会被标记为“不健康”状态，但不会被清除，因为其是持久化在磁盘 的。其对不健康持久实例的清除，需要专门进行。

应用场景：==**临时实例适合于存在突发流量暴增可能的互联网项目。因为临时实例可以实现弹性扩容。**==

#### 1.4 重要API

**Instance类**：代表一个Nacos Client主机实例

**ServiceInfo类**：微服务信息实例。其中包含着一个Instance列表

**NamingService接口：**该接口只有一个实现类，NacosNamingService。通过这个类的实例，可以完成Client与Server间的通 信，==例如注册/取消注册，订阅/取消订阅，获取Server状态，获取Server中指定的Instance==。

注意，心跳不是通过这个类实例完成的。