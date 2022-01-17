# Redis面试连环炮

## 面试题

- Redis和Memcache有什么区别
- Redis的线程模型是什么？
- Redis的数据类型及应用场景？
- 为什么单线程的Redis比多线程的Memcache的效率要高？
- 为什么Redis是单线程但是还可以支撑高并发？
- Redis如何通过读写分离来承受百万的QPS
- Redis的持久化策略有哪些？AOF和RDB各有什么优缺点
- Redis的过期策略以及LRU算法
- 如何保证Redis的高并发和高可用？
- redis的主从复制原理能介绍一下么？
- redis的哨兵原理能介绍一下么？
- Redis主备切换的数据丢失问题：异步复制、集群脑裂
- Redis哨兵的底层原理

Redis最基本的一个**内部原理**和特点就是NIO异步的单线程工作模型。Memcache是早些年个大互联网公司常用的缓存方案，但是现在近几年都是使用的redis，没有什么公司使用Memcache了。

注意：Redis中单个Value的大小最大为512MB，redis的key和string类型value限制均为512MB

## 1、Redis和Memcache的区别

从Redis作者给出的几个比较

- Redis拥有更多的数据结构

> Redis相比Memcache来说，拥有更多的数据结构和支持更丰富的数据操作，通常在Memcache里，你需要将数据拿到客户端来进行类似的修改，在set进去。这就大大增加了网络IO的次数和体积，在Redis中，这些复杂的操作通常和一般的set/get一样高效。所以，如果需要缓存能够支持更复杂的结构和操作，那么Redis是不错的选择

- Redis内存利用率对比

> 使用简单的key-value存储的话，Memcache的内存利用率更高，而Redis采用Hash结构来做key-value存储，由于其组合式的压缩，其内存利用率会高于Memcache

- 性能对比

> 由于Redis只使用了单核，而Memcache可以使用多核，所以平均每核上Redis在存储小数据比Memcache性能更高，而在100K以上的数据中，Memcache性能更高，虽然Redis最近也在存储大数据的性能上进行优化，但是比起Memcache还有略有逊色。

- 集群模式

> Memcache没有原生的集群模式，需要依赖客户端来实现往集群中分片写入数据，但是Redis目前是原生支持cluster模式的。

## 2、Redis都有哪些数据类型，及使用场景

- String

> 最基本的类型，就和普通的set 和 get，做简单的key - value 存储

- Hash

> 这个是 类似于Map的一种结构，就是一半可以将结构化数据，比如对象（前提是这个对象没有嵌套其它对象）给缓存在redis中，每次读写redis缓存的时候，可以操作hash里面的某个字段

```java
key=150
value={
  "id": 150,
  "name": "张三",
  "age": 20,  
}
```

> Hash类的数据结构，主要用来存放一些对象，把一些简单的对象给缓存起来，后续操作的时候，你可以直接仅仅修改这个对象中某个字段的值。

- List

> 1. 有序列表，可以通过list存储一些列表型的数据结构，类似**粉丝列表，文章的评论列表**之类的东西。
> 2. 可以通过lrange命令，从某个元素开始读取多少个元素，可以基于list实现分页查询，基于Redis实现简单的高性能分页，可以做类似微博那种**下拉不断分页**的东西，性能高，就是一页一页走。
> 3. 可以制作一个**简单的消息队列**，从list头插入，从list 的尾巴取出

- Set

> 1. 无序列表，自动去重
>
> 2. 直接基于**Set将系统中需要去重的数据丢进去**，如果你需要对一些数据进行快速的全局去重，就可以使用基于JVM内存里的HashSet进行去重，**但是如果你的某个系统部署在多台机器上的话，只有使用Redis进行全局的Set去重**
> 3. 可以基于set玩儿交集、并集、差集的操作，比如交集吧，可以把两个人的粉丝列表整一个交集，看看俩人的共同好友是谁？把两个大v的粉丝都放在两个set中，对两个set做交集

- Sort Set

> 1. 排序的set，**去重但是可以排序**，写进去的时候给一个分数，自动根据分数排序，这个可以玩儿很多的花样，最大的特点是有个分数可以**自定义排序规则**
> 2. 比如说你要是想根据时间对数据排序，那么可以写入进去的时候用某个时间作为分数，人家自动给你按照时间排序了
> 3. 排行榜：将每个用户以及其对应的什么分数写入进去，zadd board score username，接着zrevrange board 0 99，就可以获取排名前100的用户；zrank board username，可以看到用户在排行榜里的排名

```
zadd board 85 zhangsan
zadd board 72 wangwu
zadd board 96 lisi
zadd board 62 zhaoliu

96 lisi
85 zhangsan
72 wangwu
62 zhaoliu

zrevrange board 0 3

获取排名前3的用户

96 lisi
85 zhangsan
72 wangwu

zrank board zhaoliu
```

