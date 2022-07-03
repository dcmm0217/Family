# 第1节-ElasticSearch入门

## 1、什么是ElasticSearch?

如果你没有听说过 **Elastic Stack**，那你一定听说过 **ELK** ，实际上 **ELK** 是三款软件的简称，分别是**Elasticsearch**、 **Logstash**、**Kibana** 组成，在发展的过程中，又有新成员 **Beats** 的加入，所以就形成了**Elastic Stack**。所以说，**ELK** 是旧的称呼，**Elastic Stack** 是新的名字。

![image-20220703230109582](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703230109582.png)

全系的 **ElasticStack** 技术栈包括：

![image-20220703230137828](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703230137828.png)

> Elasticsearch

Elasticsearch 基于 **Java**，是个开源分布式搜索引擎，它的特点有：分布式，零配置，自动发现，索引自动分片，索引副本机制，**restful** 风格接口，多数据源，自动搜索负载等。

> Logstash

**Logstash** 基于 **Java**，是一个开源的用于收集,分析和存储日志的工具。

> Kibana

**Kibana** 基于 **nodejs**，也是一个开源和免费的工具，**Kibana** 可以为 **Logstash** 和 **ElasticSearch** 提供的日志分析友好的 **Web** 界面，可以汇总、分析和搜索重要数据日志。

> Beats

**Beats** 是 **elastic** 公司开源的一款采集系统监控数据的代理 **agent**，是在被监控服务器上以客户端形式运行的数据收集器的统称，可以直接把数据发送给 **Elasticsearch** 或者通过 **Logstash** 发送给 **Elasticsearch**，然后进行后续的数据分析活动。**Beats**由如下组成:

- Packetbeat：是一个网络数据包分析器，用于监控、收集网络流量信息，Packetbeat嗅探服务器之间的流量，解析应用层协议，并关联到消息的处理，其支 持ICMP (v4 and v6)、DNS、HTTP、Mysql、PostgreSQL、Redis、MongoDB、Memcache等协议；
- Filebeat：用于监控、收集服务器日志文件，其已取代 logstash forwarder；
- Metricbeat：可定期获取外部系统的监控指标信息，其可以监控、收集 Apache、HAProxy、MongoDB MySQL、Nginx、PostgreSQL、Redis、System、Zookeeper等服务；

Beats和Logstash其实都可以进行数据的采集，但是目前主流的是使用Beats进行数据采集，然后使用 Logstash进行数据的分割处理等，早期没有Beats的时候，使用的就是Logstash进行数据的采集。

## 2、ElasticSearch快速入门

#### 2.1  Elasticsearch 安装

Elasticsearch 的官方地址：https://www.elastic.co/cn/

Elasticsearch 最新的版本是 7.11.2（截止 2021.3.10），我们选择 7.8.0 版本（最新版本半
年前的版本）

下载地址：https://www.elastic.co/cn/downloads/past-releases#elasticsearch

Elasticsearch 分为 Linux 和 Windows 版本，基于我们主要学习的是 Elasticsearch 的 Java
客户端的使用，所以课程中使用的是安装较为简便的 Windows 版本。

![image-20220703230630340](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703230630340.png)

Windows 版的 Elasticsearch 的安装很简单，解压即安装完毕，解压后的 Elasticsearch 的
目录结构如下

![image-20220703230642208](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703230642208.png)

![image-20220703230651653](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703230651653.png)

![image-20220703230704650](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703230704650.png)

==注意：9300 端口为 Elasticsearch 集群间组件的通信端口，9200 端口为浏览器访问的 http协议 RESTful 端口。==

打开浏览器（推荐使用谷歌浏览器），输入地址：http://localhost:9200，测试结果

![image-20220703230845727](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703230845727.png)

使用Linux进行安装

![image-20220703231213078](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703231213078.png)

下载好安装包

拉取Docker容器

因为我们需要部署在 **Linux** 下，为了以后迁移 **ElasticStack** 环境方便，我们就使用 **Docker** 来进行部署，首先我们拉取一个带有 **ssh** 的 **Centos** 镜像

```shell
# 拉取镜像
docker pull moxi/centos_ssh
# 制作容器
docker run --privileged -d -it -h ElasticStack --name ElasticStack -p 11122:22 -p 9200:9200 -p 5601:5601 -p 9300:9300 -v /etc/localtime:/etc/localtime:ro  moxi/centos_ssh /usr/sbin/init
```

然后直接远程连接 **11122** 端口即可

单机版安装

因为 **ElasticSearch** 不支持 **root** 用户直接操作，因此我们需要创建一个elsearch用户

```shell
# 添加新用户
useradd elsearch

# 创建一个soft目录，存放下载的软件
mkdir /soft

# 进入，然后通过xftp工具，将刚刚下载的文件拖动到该目录下
cd /soft

# 解压缩
tar -zxvf elasticsearch-7.9.1-linux-x86_64.tar.gz

#重命名
mv elasticsearch-7.9.1/ elsearch
```

因为刚刚我们是使用 **root** 用户操作的，所以我们还需要更改一下 **/soft** 文件夹的所属，改为 **elsearch** 用户

```shell
chown elsearch:elsearch /soft/ -R

#然后在切换成 elsearch 用户进行操作
# 切换用户
su - elsearch

#然后我们就可以对我们的配置文件进行修改了

# 进入到 elsearch下的config目录
cd /soft/elsearch/config

#然后找到下面的配置
#打开配置文件
vim elasticsearch.yml 

#设置ip地址，任意网络均可访问
network.host: 0.0.0.0 
```

在 **Elasticsearch** 中如果**network.host** 不是 **localhost** 或者**127.0.0.1** 的话，就会认为是生产环境，而生产环境的配置要求比较高，我们的测试环境不一定能够满足，一般情况下需要修改**两处配置**，如下：

```shell
# 修改jvm启动参数
vim conf/jvm.options

#根据自己机器情况修改
-Xms128m 
-Xmx128m
```

然后在修改**第二处**的配置，这个配置要求我们到宿主机器上来进行配置

```shell
# 到宿主机上打开文件
vim /etc/sysctl.conf
# 增加这样一条配置，一个进程在VMAs(虚拟内存区域)创建内存映射最大数量
vm.max_map_count=655360
# 让配置生效
sysctl -p
```

启动ElasticSearch

首先我们需要切换到 **elsearch** 用户

```shell
su - elsearch
```

然后在到 **bin**目录下，执行下面

```shell
# 进入bin目录
cd /soft/elsearch/bin
# 后台启动
./elasticsearch -d
```

启动成功后，访问下面的 **URL**

```shell
http://202.193.56.222:9200/
```



windows可能出现的问题：

1、Elasticsearch 是使用 java 开发的，且 7.8 版本的 ES 需要 JDK 版本 1.8 以上，默认安装
包带有 jdk 环境，如果系统配置 JAVA_HOME，那么使用系统默认的 JDK，如果没有配
置使用自带的 JDK，一般建议使用系统配置的 JDK

2、双击启动窗口闪退，通过路径访问追踪错误，如果是“空间不足”，请修改
config/jvm.options 配置文件

```shell
# 设置 JVM 初始内存为 1G。此值可以设置与-Xmx 相同，以避免每次垃圾回收完成后 JVM 重新分配内存
# Xms represents the initial size of total heap space
# 设置 JVM 最大可用内存为 1G
# Xmx represents the maximum size of total heap space
-Xms1g
-Xmx1g
```

Linux可能出现的问题：

如果出现下面的错误信息

```shell
java.lang.RuntimeException: can not run elasticsearch as root
    at org.elasticsearch.bootstrap.Bootstrap.initializeNatives(Bootstrap.java:111)
    at org.elasticsearch.bootstrap.Bootstrap.setup(Bootstrap.java:178)
    at org.elasticsearch.bootstrap.Bootstrap.init(Bootstrap.java:393)
    at org.elasticsearch.bootstrap.Elasticsearch.init(Elasticsearch.java:170)
    at org.elasticsearch.bootstrap.Elasticsearch.execute(Elasticsearch.java:161)
    at org.elasticsearch.cli.EnvironmentAwareCommand.execute(EnvironmentAwareCommand.java:86)
    at org.elasticsearch.cli.Command.mainWithoutErrorHandling(Command.java:127)
    at org.elasticsearch.cli.Command.main(Command.java:90)
    at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:126)
    at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:92)
For complete error details, refer to the log at /soft/elsearch/logs/elasticsearch.log
[root@e588039bc613 bin]# 2020-09-22 02:59:39,537121 UTC [536] ERROR CLogger.cc@310 Cannot log to named pipe /tmp/elasticsearch-5834501324803693929/controller_log_381 as it could not be opened for writing
2020-09-22 02:59:39,537263 UTC [536] INFO  Main.cc@103 Parent process died - ML controller exiting
```

就说明你没有切换成 **elsearch** 用户，因为不能使用 **root** 用户去操作 **ElasticSearch**

```shell
su - elsearch
```

```shell
[1]:max file descriptors [4096] for elasticsearch process is too low, increase to at least[65536]
```

解决方法：切换到 **root** 用户，编辑 **limits.conf** 添加如下内容

```shell
vi /etc/security/limits.conf

# ElasticSearch添加如下内容:
* soft nofile 65536
* hard nofile 131072
* soft nproc 2048
* hard nproc 4096
```

```
[2]: max number of threads [1024] for user [elsearch] is too low, increase to at least
[4096]
```

也就是最大线程数设置的太低了，需要改成 **4096**

```shell
#解决：切换到root用户，进入limits.d目录下修改配置文件。
vi /etc/security/limits.d/90-nproc.conf
#修改如下内容：
* soft nproc 1024
#修改为
* soft nproc 4096
```

```
[3]: system call filters failed to install; check the logs and fix your configuration
or disable system call filters at your own risk
```

解决：**Centos6** 不支持 **SecComp**，而 **ES5.2.0** 默认 **bootstrap.system_call_filter** 为 **true**

```shell
vim config/elasticsearch.yml
# 添加
bootstrap.system_call_filter: false
bootstrap.memory_lock: false
```

```
[elsearch@e588039bc613 bin]$ Exception in thread "main" org.elasticsearch.bootstrap.BootstrapException: java.nio.file.AccessDeniedException: /soft/elsearch/config/elasticsearch.keystore
Likely root cause: java.nio.file.AccessDeniedException: /soft/elsearch/config/elasticsearch.keystore
    at java.base/sun.nio.fs.UnixException.translateToIOException(UnixException.java:90)
    at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:111)
    at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:116)
    at java.base/sun.nio.fs.UnixFileSystemProvider.newByteChannel(UnixFileSystemProvider.java:219)
    at java.base/java.nio.file.Files.newByteChannel(Files.java:375)
    at java.base/java.nio.file.Files.newByteChannel(Files.java:426)
    at org.apache.lucene.store.SimpleFSDirectory.openInput(SimpleFSDirectory.java:79)
    at org.elasticsearch.common.settings.KeyStoreWrapper.load(KeyStoreWrapper.java:220)
    at org.elasticsearch.bootstrap.Bootstrap.loadSecureSettings(Bootstrap.java:240)
    at org.elasticsearch.bootstrap.Bootstrap.init(Bootstrap.java:349)
    at org.elasticsearch.bootstrap.Elasticsearch.init(Elasticsearch.java:170)
    at org.elasticsearch.bootstrap.Elasticsearch.execute(Elasticsearch.java:161)
    at org.elasticsearch.cli.EnvironmentAwareCommand.execute(EnvironmentAwareCommand.java:86)
    at org.elasticsearch.cli.Command.mainWithoutErrorHandling(Command.java:127)
    at org.elasticsearch.cli.Command.main(Command.java:90)
    at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:126)
    at org.elasticsearch.bootstrap.Elasticsearch.main(Elasticsearch.java:92)
```

我们通过排查，发现是因为 **/soft/elsearch/config/elasticsearch.keystore** 存在问题

![image-20220703231839040](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703231839040.png)

也就是说该文件还是所属于**root** 用户，而我们使用 **elsearch** 用户无法操作，所以需要把它变成 **elsearch**

```shell
chown elsearch:elsearch elasticsearch.keystore
```

```
[1]: the default discovery settings are unsuitable for production use; at least one of [discovery.seed_hosts, discovery.seed_providers, cluster.initial_master_nodes] must be configured
ERROR: Elasticsearch did not exit normally - check the logs at /soft/elsearch/logs/elasticsearch.log
```

继续修改配置 **elasticsearch.yaml**

```
# 取消注释，并保留一个节点
node.name: node-1
cluster.initial_master_nodes: ["node-1"]
```

#### 2.2  ElasticSearch中的基本概念

> 索引：

索引是 **Elasticsearch** 对逻辑数据的逻辑存储，所以它可以分为更小的部分。

可以把索引看成关系型数据库的表，索引的结构是为快速有效的全文索引准备的，特别是它不存储原始值。

**Elasticsearch** 可以把索引存放在一台机器或者分散在多台服务器上，每个索引有一或多个分片（**shard**），每个分片可以有多个副本（**replica**）。

> 文档：

- 存储在 **Elasticsearch** 中的主要实体叫文档（**document**）。用关系型数据库来类比的话，一个文档相当于数据库表中的一行记录。
- **Elasticsearch** 和 **MongoDB** 中的文档类似，都可以有不同的结构，但 **Elasticsearch** 的文档中，相同字段必须有相同类型。
- 文档由多个字段组成，每个字段可能多次出现在一个文档里，这样的字段叫多值字段（**multivalued**）。每个字段的类型，可以是文本、数值、日期等。字段类型也可以是复杂类型，一个字段包含其他子文档或者数组。

> 映射

所有文档写进索引之前都会先进行分析，如何将输入的文本分割为词条、哪些词条又会被过滤，这种行为叫做 映射（**mapping**）。一般由用户自己定义规则。

> 文档类型

- 在 **Elasticsearch** 中，一个索引对象可以存储很多不同用途的对象。例如，一个博客应用程序可以保存文章和评论。
- 每个文档可以有不同的结构。
- 不同的文档类型不能为相同的属性设置不同的类型。例如，在同一索引中的所有文档类型中，一个叫 **title** 的字段必须具有相同的类型。

> RESTful API

在 **Elasticsearch** 中，提供了功能丰富的 **RESTful API** 的操作，包括基本的 **CRUD**、创建索引、删除索引等操作。

> 创建非结构化索引

在 **Lucene** 中，创建索引是需要定义字段名称以及字段的类型的，在 **Elasticsearch** 中提供了非结构化的索引，就是不需要创建索引结构，即可写入数据到索引中，实际上在 **Elasticsearch** 底层会进行结构化操作，此操作对用户是透明的。

> 创建空索引

```json
PUT /haoke
{
    "settings": {
        "index": {
        "number_of_shards": "2", #分片数
        "number_of_replicas": "0" #副本数
        }
    }
}
```

> 删除索引

```json
#删除索引
DELETE /haoke
{
    "acknowledged": true
}
```

> 插入数据

**URL** 规则：**POST** /{索引}/{类型}/{id}

```json
POST /haoke/user/1001
#数据
{
    "id":1001,
    "name":"张三",
    "age":20,
    "sex":"男"
}
```

使用 **postman** 操作成功后

使用`http://127.0.0.1:9200/_cat/indices?v`就可以看到我们的索引信息了

**说明：非结构化的索引，不需要事先创建，直接插入数据默认创建索引。不指定id插入数据：**

![image-20220703232830844](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703232830844.png)

> 更新数据

在 **Elasticsearch** 中，文档数据是不能修改的，但是可以通过覆盖的方式进行更新。

```json
PUT /haoke/user/1001
{
    "id":1001,
    "name":"张三",
    "age":21,
    "sex":"女"
}
```

覆盖成功后的结果如下：

![image-20220703232855298](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703232855298.png)

![image-20220703232918119](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703232918119.png)

可以看到数据已经被覆盖了。问题来了，可以局部更新吗？

-- 可以的。前面不是说，文档数据不能更新吗？其实是这样的：在内部，依然会查询到这个文档数据，然后进行覆盖操作，步骤如下：

1. 从旧文档中检索JSON
2. 修改它
3. 删除旧文档
4. 索引新文档

```json
#注意：这里多了_update标识
POST /haoke/user/1001/_update
{
    "doc":{
        "age":23
    }
}
```

![image-20220703232946172](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703232946172.png)

![image-20220703232951920](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703232951920.png)

> 删除索引

在 **Elasticsearch** 中，删除文档数据，只需要发起 **DELETE** 请求即可，不用额外的参数

```json
DELETE 1 /haoke/user/1001
```

![image-20220703233010290](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703233010290.png)

需要注意的是，**result** 表示已经删除，**version** 也增加了。

如果删除一条不存在的数据，会响应 **404**

![image-20220703233024509](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220703233024509.png)

**删除一个文档也不会立即从磁盘上移除，它只是被标记成已删除。Elasticsearch将会在你之后添加更多索引的时候才会在后台进行删除内容的清理。【相当于批量操作】**

