# Redis面试连环炮

## 面试题

- Redis和Memcache有什么区别？
- Redis的线程模型是什么？
- Redis的数据类型及应用场景？
- 为什么单线程的Redis比多线程的Memcache的效率要高？
- 为什么Redis是单线程但是还可以支撑高并发？
- Redis如何通过读写分离来承受百万的QPS
- Redis的持久化策略有哪些？AOF和RDB各有什么优缺点
- Redis的过期策略以及LRU算法
- 如何保证Redis的高并发和高可用？
- Redis的主从复制原理能介绍一下么？
- Redis的哨兵原理能介绍一下么？
- Redis主备切换的数据丢失问题：异步复制、集群脑裂
- Redis哨兵的底层原理

## 剖析

Redis最基本的一个**内部原理**和**特点就是NIO异步的单线程工作模型**。Memcache是早些年个大互联网公司常用的缓存方案，但是现在近几年都是使用的redis，没有什么公司使用Memcache了。

注意：Redis中单个Value的大小最大为512MB，**redis的key和string类型value限制均为512MB**

## **Redis和Memcache区别**

从Redis作者给出的几个比较

- **Redis拥有更多的数据结构**
  - Redis相比Memcache来说，拥有更多的数据结构和支持更丰富的数据操作，通常在Memcache里，你需要将数据拿到客户端来进行类似的修改，在set进去。这就大大增加了网络IO的次数和体积，在Redis中，这些复杂的操作通常和一般的set/get一样高效。所以，如果需要缓存能够支持更复杂的结构和操作，那么Redis是不错的选择
- **Redis内存利用率对比**
  - 使用简单的key-value存储的话，Memcache的内存利用率更高，而Redis采用Hash结构来做key-value存储，由于其组合式的压缩，其内存利用率会高于Memcache
- **性能对比**
  - 由于Redis只使用了单核，而Memcache可以使用多核，所以平均每核上Redis在存储小数据比Memcache性能更高，而在100K以上的数据中，Memcache性能更高，虽然Redis最近也在存储大数据的性能上进行优化，但是比起Memcache还有略有逊色。
- **集群模式**
  - Memcache没有原生的集群模式，需要依赖客户端来实现往集群中分片写入数据，但是Redis目前是原生支持cluster模式的。

## Redis都有哪些数据类型，及使用场景

- String
  - 最基本的数据类型，普通的set和get，做简单的key-value存储

- Hash

  - 这个是类似于Map的一种结构，就是就是一半可以将结构化数据，比如对象（前提是这个对象我没有嵌套其他对象）给缓存Redis中，每次读写redis缓存的时候，可以操作hash里面的某个字段。

    ```java
    key=150
    value={
      "id": 150,
      "name": "张三",
      "age": 20,  
    }
    ```

    - Hash类的数据结构，主要存放一些对象，把一些简单的对象给缓存起来，后续操作的时候，你可以直接仅仅修改这个对象中的某个字段值。

- List
  - 有序列表，可以通过List存储一些列表型的数据结构，类似做 **`粉丝列表`，`文章的评论列表 `**之类的东西。
  - 可以通过**Irange命令**，从某个元素开始读取多少个元素，可以基于list实现分页查询，基于Redis实现简单的高性能分页，可以做类似微博那种下拉不断分页的东西，性能高，就是一页一页走。
  - 可以制作一个**简单的消息队列**，从List头插入，从List尾巴取出。

- Set
  - 无序列表，自动去重
  - 直接**基于Set将系统中需要去重的数据丢进去**，如果你需要对一些数据进行快速的全局去重，就可以使用基于JVM内存里的HashSet进行去重，**但是如果你的某个系统部署在多台机器上的话，只有使用Redis进行全局的Set去重**
  - 可以基于Set玩 交集、并集、差集的操作，比如交集，**可以把两个人粉丝列表整一个交集，看看俩人的共同好友是谁？把两个大V的粉丝都放在两个Set中，对两个Set做交集。**

- Sort Set
  - 排序的set，**去重但是可以排序，写进去的时候给一个分数，自动根据分数排序**，这个可以玩很多花样，最大特点是有个分数可以自定义排序规则
  - 比如说你要是想根据时间对数据排序，那么可以写入进去的时候用某个时间作为分数，人家自动给你按照时间排序了。
  - 排行榜：将每个用户以及其对应的什么分数写入进去，`zadd board score username`，接着`zrevrange board 0 99`，就可以获取排名前100的用户，`zrank board username` ，可以看到用户在排行榜里的排名。

```
zadd board 85 zhangsan
zadd board 72 wangwu
zadd board 96 lisi
zadd board 62 zhaoliu

96 lisi
85 zhangsan
72 wangwu
62 zhaoliu

#获取排名前3的用户
zrevrange board 0 3
96 lisi
85 zhangsan
72 wangwu

#查看zhaoliu 在排行榜里的排名
zrank board zhaoliu
3
```

## Redis持久化对于生产环境的意义

**故障发生时候会怎么样？**

**如何应对故障的发生？**

#### 1、Redis持久化的意义

Redis持久化的意义，在于**故障恢复**，也属于**高可用**的一个环节。

例如：当存放在内存中数据，会因为Redis的突然挂掉，而导致数据丢失。

![image-20211210174929403](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210174929403.png)

Redis的持久化，就是将内存中的数据，持久化到磁盘上，然后将磁盘上的数据放到阿里云ODPS（阿里云数据仓库）中

![image-20211210175140631](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210175140631.png)

通过持久化将数据存储在磁盘中，然后定期比如说同步和备份到一些云存储服务上去。

#### 2、Redis中的RDB和AOF两种持久化机制

当出现Redis宕机时，我们需要做的是重启redis，**尽快的让其对外提供服务，缓存全部无法命中，在redis中根本找不到数据，这时候就会出现`缓存雪崩`的问题。**

所有的请求，没有在Redis中命中，就会去Mysql数据库 这种数据库源头去找，**一下子Mysql无法承受高并发**，那么系统将直接宕机。这个时候Mysql宕机，**因为没办法从Mysql中将缓存恢复到Redis中，因为Redis中的数据是从Mysql中来的**。（整个系统就G了）

#### 3、RDB持久化机制

简单来说RDB：就是将Redis中的数据，每隔一段时间，进行数据持久化。

![image-20211210175956809](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210175956809.png)

#### 4、AOF持久化机制

Redis将内存中的数据，存放到一个AOF文件中，但是因为Redis只会写一个AOF文件，因此这个AOF文件会越来越大。（追加写入模式）

AOF机制对每条写入命令**作为日志**，以append-only的模式写入一个日志文件中，在redis重启的时候，**可以通过回放AOF日志中的写入指令来重新构建整个数据集。**

![image-20211210180616280](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210180616280.png)

因为Redis中的数据是有一定限量的，不可能说Redis内存中的数据不限量增长，进而导致AOF无限量增长。

内存大小是一定的，到一定时候，Redis就会用`缓存淘汰算法LRU`，自动将一部分数据从内存中清除。

AOF，是存放每条写命令的，所以会不断的膨胀，当大到一定的时候，AOF会做rewrite操作。

AOF rewrite操作，就会基于当时redis内存中的数据，来重新构造一个更小的AOF文件，然后就将旧的膨胀的很大的文件给删了。

![image-20211210181144104](https://gitee.com/huangwei0123/image/raw/master/img/image-20211210181144104.png)

如果我们想要Redis仅仅作为纯内存的缓存来使用，那么可以禁止RDB和AOF所有的持久化机制

通过AOF和RDB，都可以将Redis内存中的数据给持久化到磁盘上来，然后可以将这些数据备份到其他地方去，例如阿里云的OSS

如果Redis挂了，服务器上的内存和磁盘上的数据都丢了，可以从云服务上拷贝回来之间的数据，放到指定目录下，然后去重启Redis，Redis就会自动根据持久化数据文件，去恢复内存中的数据，继续对外提供服务。

**如果同时使用RDB和AOF两种持久化机制，那么在Redis重启的时候，会使用AOF来重新构建数据，因为AOF中的数据更加完整。**

