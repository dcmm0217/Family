# 第3节-Redis数据类型介绍以及应用

## 1、9大类型

- String（字符类型）
- Hash（散列类型）
- List（列表类型）
- Set（集合类型）
- SortedSort（有序集合类型，简称zset）
- Bitmap（位图）
- HyperLogLog（统计）
- GEO（地理）

- Stream

Stream简单了解

Redis Stream 是 Redis 5.0 版本新增加的数据结构。Redis Stream 主要用于消息队列（MQ，Message Queue）

Redis 本身是有一个 Redis 发布订阅 (pub/sub) 来实现消息队列的功能，但它有个缺点就是消息无法持久化，如果出现网络断开、Redis 宕机等，消息就会被丢弃。简单来说发布订阅 (pub/sub) 可以分发消息，但无法记录历史消息。

而 Redis Stream 提供了消息的持久化和主备复制功能，可以让任何客户端访问任何时刻的数据，并且能记住每一个客户端的访问位置，还能保证消息不丢失。它算是redis自己消息功能的补充。

但是，**一般主流MQ都固定了(Kafka/RabbitMQ/RocketMQ/Pulsar)。**

备注：

- 命令不区分大小写，key是区分大小写的
- help@类型名词

## 2、String类型

```shell
##最常见的命令
set key value 

get key

##同时设置/获取多个键值
mset key1 value1 key2 value2 ....

mget key1 key2 

##数值增减
## 递增数字 incr key
## 增加指定的整数 incrby key increment
## 递减数值 decr key
## 减少指定的整数 decrby key decrement

## 获取字符串长度
strlen key

##分布式锁
setnx key value 
setnx key value [EX seconds][PX milliseconds][NX|XX]
```

**应用场景**：比如抖音无限点赞某个视频或者商品，点一下加一次

![image-20220509214742575](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509214742575.png)

是否喜欢的文章

![image-20220509214832960](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509214832960.png)

## 3、Hash类型

Hash在Java中就是Map<String,Map<Object,Object>>

```shell
## 一次设置一个字段值
hset key find value

## 一个获取一个字段值
hget key field 

## 一个设置多个字段值
hmset key field value [field value ...]

## 一个获取多个字段值
hmget key field [field ...]

## 获取所有字段值
hgetall key 

## 获取某个key内的全部数量
hlen key

## 删除一个key
hdel key
```

应用场景：

- 京东购物车，设计目前不再采用，当前小中厂可用

```
新增商品 → hset shopcar:uid1024 334488 1
新增商品 → hset shopcar:uid1024 334477 1
增加商品数量 → hincrby shopcar:uid1024 334477 1
商品总数 → hlen shopcar:uid1024
全部选择 → hgetall shopcar:uid1024
```

![image-20220509215750309](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509215750309.png)

## 4、List类型

简单说明：

一个**双端链表的结构**，容量是2的32次方减1个元素，大概40多亿，主要功能有push/pop等，一般用在栈、队列、消息队列等场景。

```shell
## 向列表左边添加元素
lpush key value [value ...]

## 向列表右边添加元素
rpush key value[value ...]

## 查看列表
lrange key start stop

## 获取列表中元素的个数
llen key
```

**应用场景：**

微信公众号订阅的消息：

```
1 大V作者李永乐老师和CSDN发布了文章分别是 11 和 22

2 阳哥关注了他们两个，只要他们发布了新文章，就会安装进我的List  lpush likearticle:阳哥id    11 22

3 查看阳哥自己的号订阅的全部文章，类似分页，下面0~10就是一次显示10条     lrange likearticle:阳哥id 0 9
```

![image-20220509220251990](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509220251990.png)

商品评论列表

需求1：用户针对某一商品发布评论，一个商品会被不同的用户进行评论，保存商品评论时，要按时间顺序排序

需求2：用户在前端页面查询该商品的评论，需要按照时间顺序降序排序

```
使用list存储商品评论信息，key是该商品的id，value是商品评论信息商品编号为1001的商品评论key【items:comment:1001】

lpush items:comment:1001 {"id":1001,"name":"huawei","date":1600484283054,"content":"lasjfdljsa;fdlkajsd;lfjsa;ljf;lasjf;lasjfdlsad"}
```

## 5、Set类型

```shell
## 添加元素  
sadd key member [member ...]

## 删除元素
srem key member [member ...]

## 遍历集合中的所有元素
smembers key

## 判断元素是否在集合中
sismember key member 

## 获取集合中的元素总数
scard key

## 从集合中随机弹出一个元素，元素不删除
srandmember key [数字]

## 从集合中随机弹出一个元素，出一个删一个
spop key [数字]


```

集合运算

A：abc12

B：123ax

```
集合的差集运算A-B
	属于A但不属于B的元素构成的集合
		sdiff key [key ...]
		
集合的交集运算A∩B
	属于A同时也属于B的共同拥有的元素构成的集合
		sinter key [key ...]
		
集合的并集运算A∪B
	属于A或者属于B的元素合并后的集合
		sunion key [key ...]
		
```

应用场景：

微信抽奖小程序：

![image-20220509221756656](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509221756656.png)

![image-20220509221821452](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509221821452.png)

微信朋友圈点赞：

![image-20220509221919787](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509221919787.png)

![image-20220509221928321](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509221928321.png)

微博好友关注社交关系

![image-20220509222126315](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509222126315.png)

共同关注的人

![image-20220509222237787](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509222237787.png)

我关注的人也关注他（大家爱好相同）

![image-20220509222328451](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509222328451.png)

QQ内推可能认识的人

![image-20220509222349121](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220509222349121.png)

