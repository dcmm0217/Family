# 第8节-Mysql与Redis双写一致性以及更新探讨

面试题：

- 使用缓存就会涉及到缓存与数据库的双写，这种情况你们的一致性怎么保证的？
- 双写一致性你是先动缓存还是先动数据库，为什么这样做？

## 1、Canal

> 是什么？

canal [kə'næl]，中文翻译为 水道/管道/沟渠/运河，主要用途是用于 MySQL 数据库增量日志数据的订阅、消费和解析，是阿里巴巴开发并开源的，采用Java语言开发；

历史背景是早期阿里巴巴因为杭州和美国双机房部署，存在跨机房数据同步的业务需求，实现方式主要是基于业务 trigger（触发器） 获取增量变更。从2010年开始，阿里巴巴逐步尝试采用解析数据库日志获取增量变更进行同步，由此衍生出了canal项目；

一句话：Canal是基于 Mysql变更日志增量订阅和消费的组件

> 能干嘛？

- 数据库镜像
- 数据库实时备份
- 索引构建和实时维护（拆分异构索引，倒排索引等）
- 业务cache刷新
- 带业务逻辑的增量数据处理

## 2、工作原理，面试回答

> 传统Mysql主从复制工作原理

![image-20220606155248733](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606155248733.png)

MySQL的主从复制将经过如下步骤：

1、当 master 主服务器上的数据发生改变时，则将其改变写入二进制事件日志文件中；

2、salve 从服务器会在一定时间间隔内对 master 主服务器上的二进制日志进行探测，探测其是否发生过改变，如果探测到 master 主服务器的二进制事件日志发生了改变，则开始一个 I/O Thread 请求 master 二进制事件日志；

3、同时 master 主服务器为每个 I/O Thread 启动一个dump  Thread，用于向其发送二进制事件日志；

4、slave 从服务器将接收到的二进制事件日志保存至自己本地的中继日志文件中；

5、salve 从服务器将启动 SQL Thread 从中继日志中读取二进制日志，在本地重放，使得其数据和主服务器保持一致；

6、最后 I/O Thread 和 SQL Thread 将进入睡眠状态，等待下一次被唤醒；

> Canal工作原理

canal 模拟 MySQL slave 的交互协议，伪装自己为 MySQL slave ，向 MySQL master 发送dump 协议

MySQL master 收到 dump 请求，开始推送 binary log 给 slave (即 canal )

canal 解析 binary log 对象(原始为 byte 流)

![image-20220606155728861](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606155728861.png)

**结论之一：分布式系统只有最终一致性，很难做到强一致性**

## 3、mysql-canal-redis双写一致性Coding

> mysql方面

建表脚本

```sql
CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userName` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4
```

查看mysql版本， canal只支持mysql5.5及其以上版本

当前的主机二进制日志 show master status

查看SHOW VARIABLES LIKE  log_bin

![image-20220606160453697](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606160453697.png)

开启mysql的binlog写入功能

找到mysql的配置文件.ini文件

```ini
log-bin=mysql-bin #开启 binlog
binlog-format=ROW #选择 ROW 模式
server_id=1    #配置MySQL replaction需要定义，不要和canal的 slaveId重复

```

ROW模式 除了记录sql语句之外，还会记录每个字段的变化情况，能够清楚的记录每行数据的变化历史，但会占用较多的空间。

STATEMENT模式只记录了sql语句，但是没有记录上下文信息，在进行数据恢复的时候可能会导致数据的丢失情况；

MIX模式比较灵活的记录，理论上说当遇到了表结构变更的时候，就会记录为statement模式。当遇到了数据更新或者删除情况下就会变为row模式；

![image-20220606160749792](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606160749792.png)

window - mmy.ini

linux - my.cnf

必须重启mysql 

再次 查看SHOW VARIABLES LIKE  log_bin

![image-20220606160854544](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606160854544.png)

授权canal连接mysql账号

mysql默认的用户在mysql库的user表里面

![image-20220606161057125](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606161057125.png)

默认没有canal账号，此处新建+授权

```sql
DROP USER 'canal'@'%';
CREATE USER 'canal'@'%' IDENTIFIED BY 'canal';  
GRANT ALL PRIVILEGES ON *.* TO 'canal'@'%' IDENTIFIED BY 'canal';  
FLUSH PRIVILEGES;
 
SELECT * FROM mysql.user;
```

![image-20220606161208095](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606161208095.png)

> canal服务端

下载

`https://github.com/alibaba/canal/releases`

解压后整体放入/mycanal路径下

![image-20220606161358464](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606161358464.png)

**配置修改**

/mycanal/canal.deployer-1.1.5/conf/example路径下

![image-20220606161500432](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606161500432.png)

instance.properties

换成自己的mysql的IP地址

![image-20220606161606558](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606161606558.png)

换成自己的在mysql新建的canal账户

![image-20220606161631856](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606161631856.png)

启动

/mycanal/canal.deployer-1.1.5/bin路径下执行 ./startup.sh

查看server日志

![image-20220606161731150](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606161731150.png)

查看instance日志

![image-20220606161810246](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606161810246.png)

> canal客户端（java编写业务程序）

1、建module

2、改pom

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.5.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.zzyy.study</groupId>
    <artifactId>canal_demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>canal_demo</name>
    <description>Demo project for Spring Boot</description>


    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <junit.version>4.12</junit.version>
        <log4j.version>1.2.17</log4j.version>
        <lombok.version>1.16.18</lombok.version>
        <mysql.version>5.1.47</mysql.version>
        <druid.version>1.1.16</druid.version>
        <mybatis.spring.boot.version>1.3.0</mybatis.spring.boot.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.alibaba.otter</groupId>
            <artifactId>canal.client</artifactId>
            <version>1.1.0</version>
        </dependency>
        <!--guava-->
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>23.0</version>
        </dependency>
        <!--web+actuator-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!--SpringBoot与Redis整合依赖-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>
        <!-- jedis -->
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>3.1.0</version>
        </dependency>
        <!-- springboot-aop 技术-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <!-- redisson -->
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson</artifactId>
            <version>3.13.4</version>
        </dependency>
        <!--Mysql数据库驱动-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.47</version>
        </dependency>
        <!--集成druid连接池-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.1.10</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>${druid.version}</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>${druid.version}</version>
        </dependency>
        <!--mybatis和springboot整合-->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>${mybatis.spring.boot.version}</version>
        </dependency>
        <!-- 添加springboot对amqp的支持 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <!--通用基础配置-->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>${log4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>RELEASE</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.73</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>

```

3、改yaml

```properties
server.port = 5555
```

4、业务类

RedisUtils

```java
package com.zzyy.study.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils
{
    public static JedisPool jedisPool;

    static {
        JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        jedisPool=new JedisPool(jedisPoolConfig,"192.168.111.147",6379);
    }

    public static Jedis getJedis() throws Exception {
        if(null!=jedisPool){
            return jedisPool.getResource();
        }
        throw new Exception("Jedispool is not ok");
    }


    /*public static void main(String[] args) throws Exception
    {
        try(Jedis jedis = RedisUtils.getJedis())
        {
            System.out.println(jedis);

            jedis.set("k1","xxx2");
            String result = jedis.get("k1");
            System.out.println("-----result: "+result);
            System.out.println(RedisUtils.jedisPool.getNumActive());//1
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/
}
 
```

RedisCanalClientExample

```java
 
package com.zzyy.study.t1;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;
import com.zzyy.study.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RedisCanalClientExample
{

    public static final Integer _60SECONDS = 60;

    public static void main(String args[]) {

        // 创建链接canal服务端
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("192.168.111.147",
                11111), "example", "", "");
        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            //connector.subscribe(".*\\..*");
            connector.subscribe("db2020.t_user");
            connector.rollback();
            int totalEmptyCount = 10 * _60SECONDS;
            while (emptyCount < totalEmptyCount) {
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    emptyCount++;
                    try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
                } else {
                    emptyCount = 0;
                    printEntry(message.getEntries());
                }
                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }
            System.out.println("empty too many times, exit");
        } finally {
            connector.disconnect();
        }
    }

    private static void printEntry(List<Entry> entrys) {
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error,data:" + entry.toString(),e);
            }

            EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(), eventType));

            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.INSERT) {
                    redisInsert(rowData.getAfterColumnsList());
                } else if (eventType == EventType.DELETE) {
                    redisDelete(rowData.getBeforeColumnsList());
                } else {//EventType.UPDATE
                    redisUpdate(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private static void redisInsert(List<Column> columns)
    {
        JSONObject jsonObject = new JSONObject();
        for (Column column : columns)
        {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
            jsonObject.put(column.getName(),column.getValue());
        }
        if(columns.size() > 0)
        {
            try(Jedis jedis = RedisUtils.getJedis())
            {
                jedis.set(columns.get(0).getValue(),jsonObject.toJSONString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void redisDelete(List<Column> columns)
    {
        JSONObject jsonObject = new JSONObject();
        for (Column column : columns)
        {
            jsonObject.put(column.getName(),column.getValue());
        }
        if(columns.size() > 0)
        {
            try(Jedis jedis = RedisUtils.getJedis())
            {
                jedis.del(columns.get(0).getValue());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void redisUpdate(List<Column> columns)
    {
        JSONObject jsonObject = new JSONObject();
        for (Column column : columns)
        {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
            jsonObject.put(column.getName(),column.getValue());
        }
        if(columns.size() > 0)
        {
            try(Jedis jedis = RedisUtils.getJedis())
            {
                jedis.set(columns.get(0).getValue(),jsonObject.toJSONString());
                System.out.println("---------update after: "+jedis.get(columns.get(0).getValue()));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
```

## 4、缓存双写一致性之更新策略探讨

> 缓存双写一致性，谈谈你的理解

如果redis中有数据---需要和数据库的值相同

如果redis中无数据---数据库中的值要是最新值

> 缓存按照操作来分，有细分2种

1、只读缓存

2、读写缓存

同步直写策略：写缓存时也同步写数据库，缓存和数据库中的数据一致

对于读写缓存来说，要想保证缓存和数据库中的数据一致性，就要采用同步直写策略

> 数据库和缓存一致性的几种更新策略

挂牌报错，凌晨升级

单线程，这样重量级的数据操作最好不要多线程 

**目的：总之，达到最终一致性**

给缓存设置过期时间，是保证最终一致性的解决方案。

我们可以对存入缓存的数据设置过期时间，所有的写操作以数据库为准，对缓存操作只是尽最大努力即可。也就是说如果数据库写成功，缓存更新失败，那么只要到达过期时间，则后面的读请求自然会从数据库中读取新值然后回填缓存，达到一致性，切记以mysql的数据库写入库为准。

**上述方案和后续落地案例是调研后的主流+成熟的做法，但是考虑到各个公司业务系统的差距，不是100%绝对正确，不保证绝对适配全部情况。**

==重点：讨论3种更新策略==

#### 1、先更新数据库，再更新缓存

异常问题：

```
1 先更新mysql的某商品的库存，当前商品的库存是100，更新为99个。

2 先更新mysql修改为99成功，然后更新redis。

3 此时假设异常出现，更新redis失败了，这导致mysql里面的库存是99而redis里面的还是100 。

4  上述发生，会让数据库里面和缓存redis里面数据不一致，读到脏数据
```

#### 2、先删除缓存，再更新数据库

异常问题：

第一步：A线程先成功删除了redis里面的数据，然后去更新mysql，此时mysql正在更新中，还没有结束。（比如网络延时）

B突然出现要来读取缓存数据。

![image-20220606163947534](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606163947534.png)

第二步：此时redis里面的数据是空的，B线程来读取，先去读redis里数据(已经被A线程delete掉了)，此处出来2个问题：

2.1 B从mysql获得了旧值，B线程发现redis里没有(缓存缺失)马上去mysql里面读取，从数据库里面读取来的是旧值。

2.2 B会把获得的旧值写回redis ， 获得旧值数据后返回前台并回写进redis(刚被A线程删除的旧数据有极大可能又被写回了)。

![image-20220606164243029](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606164243029.png)

第三步：A线程更新完mysql，发现redis里面的缓存是脏数据

两个并发操作，一个是更新操作，另一个是查询操作，A更新操作删除缓存后，B查询操作没有命中缓存，B先把老数据读出来后放到缓存中，然后A更新操作更新了数据库。

于是，在缓存中的数据还是老的数据，导致缓存中的数据是脏的，而且还一直这样脏下去了。

总结流程：

（1）请求A进行写操作，删除缓存后，工作正在进行中......A还么有彻底更新完

（2）请求B开工，查询redis发现缓存不存在

（3）请求B继续，去数据库查询得到了myslq中的旧值

（4）请求B将旧值写入redis缓存

（5）请求A将新值写入mysql数据库 

上述情况就会导致不一致的情形出现。 

![image-20220606164435615](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606164435615.png)

总结：先删除缓存，再更新数据库

如果数据库更新失败，导致B线程请求再次访问缓存时，发现redis里面没数据，缓存缺失，再去读取mysql时，从数据库中读取到旧值

> 解决方案：

复习阿里内部的缓存击穿方案：

多个线程同时去查询数据库的这条数据，那么我们可以在第一个查询数据的**请求上使用一个 互斥锁来锁住它**。

其他的线程走到这一步拿不到锁就等着，等第一个线程查询到了数据，然后做缓存。

后面的线程进来发现已经有缓存了，就直接走缓存。 

![image-20220606165947571](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606165947571.png)

**采用延时双删策略**

![image-20220606170056280](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606170056280.png)

![image-20220606170142668](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606170142668.png)

双删方案面试题

这个删除该休眠多久呢？

**线程A sleep的时间，就需要大于线程B读取数据再写入缓存的时间。**

这个时间怎么确定呢？

在业务程序运行的时候，统计下线程读数据和写缓存的操作时间，自行评估自己的项目的读数据业务逻辑的耗时，以此为基础来进行估算。然后写数据的休眠时间则在读数据业务逻辑的耗时基础上加百毫秒即可。

这么做的目的，就是确保读请求结束，写请求可以删除读请求造成的缓存脏数据。

当前演示的效果时mysql单机，如果mysql主从读写分离架构该如何？

（1）请求A进行写操作，删除缓存

（2）请求A将数据写入数据库了，

（3）请求B查询缓存发现，缓存没有值

（4）**请求B去从库查询，这时，还没有完成主从同步，因此查询到的是旧值**

（5）请求B将旧值写入缓存

（6）数据库完成主从同步，从库变为新值 上述情形，就是数据不一致的原因。还是使用双删延时策略。

只是，睡眠时间修改为在主从同步的延时时间基础上，加几百ms

这种同步淘汰策略，吞吐量降低怎么办? 也就是接口RT增大

![image-20220606172556900](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606172556900.png)

#### 3、先更新数据库，再删除缓存

![image-20220606172841167](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606172841167.png)

**解决方案：重试+补偿的策略**

![image-20220606172908886](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606172908886.png)

1、可以把要删除的缓存值或者是要更新的数据库值暂存到消息队列中（例如使用Kafka/RabbitMQ等）。

2、当程序没有能够成功地删除缓存值或者是更新数据库值时，可以从消息队列中重新读取这些值，然后再次进行删除或更新。

3、如果能够成功地删除或更新，我们就要把这些值从消息队列中去除，以免重复操作，此时，我们也可以保证数据库和缓存的数据一致了，否则还需要再次进行重试

4、如果重试超过的一定次数后还是没有成功，我们就需要向业务层发送报错信息了，通知运维人员。



> 先更新缓存，再更新数据库，这种策略，直接拒绝。
>
> 有可能更新缓存成功，更新数据库失败。数据库的基准数据都丢了，没有一个兜底的正确数据了都，生产是巨大的bug
>
> 别的方案就是短暂性的数据不一致，这种是数据直接错了。并不允许的。

#### 4、总结利弊

在大多数业务场景下，我们会把Redis作为只读缓存使用。假如定位是只读缓存来说，理论上我们既可以先删除缓存值再更新数据库，也可以先更新数据库再删除缓存，但是没有完美方案，两害相衡趋其轻的原则

个人建议是，**优先使用先更新数据库，再删除缓存的方案**。理由如下：

1、先删除缓存值再更新数据库，有可能导致请求因缓存缺失而访问数据库，给数据库带来压力，严重导致打满mysql。

2、如果业务应用中读取数据库和写缓存的时间不好估算，那么，延迟双删中的等待时间就不好设置。

多补充一句：如果使用先更新数据库，再删除缓存的方案

如果业务层要求必须读取一致性的数据，那么我们就需要在更新数据库时，先在Redis缓存客户端暂存并发读请求，等数据库更新完、缓存值删除后，再读取数据，从而保证数据一致性。

![image-20220606174525564](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220606174525564.png)

