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

## 2、五种基本数据类型

#### 1、String类型

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

#### 2、Hash类型

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

#### 3、List类型

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

#### 4、Set类型

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

#### 5、Zset类型

向有序集合里面加入一个元素和该元素的分数

添加元素  

```
zadd key score member[score member ...]
```

按照元素分数从小到大的顺序，返回索引从start到stop之间的所有元素

```
zrange key start stop [withscores]
```

获取元素的分数

```
zscore key member
```

删除元素

```
zrem key member [member ...]
```

获取指定分数范围的元素

```
zrangebyscore key min max [withscores offset count]
```

新增某个元素的分数

```
zincrby key increment member
```

获取集合中元素的数量

```
zcard key 
```

获取指定分数范围内元素的个数

```
zcount key min max
```

按照排名范围删除元素

```
zremrangebyrank key start stop
```

获取元素的排名

```
从小到大
zrank key member
从大到小
zrerank key member
```

> 应用场景：

1、根据商品销售对商品进行排序显示

思路：定义商品销售排行榜(sorted set集合)，key为goods:sellsort，分数为商品销售数量。

![image-20220521224914781](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220521224914781.png)

2、抖音热搜

![image-20220521225251811](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220521225251811.png)

1、点击视频

```
zincrby hotvcr:20200919 1 八佰
zincrby hotvcr:20200919 15 八佰 2 花木兰
```

2、展示当日排行前10条

```
ZREVRANGE hotvcr:20200919 0 9 withscores
```

案例实战02：微信文章阅读统计量

- 需求

![image-20220521225540668](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220521225540668.png)

- 代码

```java
@RestController
@Slf4j
@Api(description = "喜欢的文章接口")
public class ArticleController
{
    @Resource
    private ArticleService articleService;

    @ApiOperation("喜欢的文章，点一次加一个喜欢")
    @RequestMapping(value ="/view/{articleId}", method = RequestMethod.POST)
    public void likeArticle(@PathVariable(name="articleId") String articleId)
    {
        articleService.likeArticle(articleId);
    }
}
```

```java
@Service
@Slf4j
public class ArticleService
{
    public static final String ARTICLE = "article:";
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void likeArticle(String articleId)
    {
        String key = ARTICLE+articleId;
        Long likeNumber = stringRedisTemplate.opsForValue().increment(key);
        log.info("文章编号:{},喜欢数:{}",key,likeNumber);
    }
}
```

- 结论

中小厂可以用，QPS特别高的大厂不可以用。（why）

缓存穿透问题？

瞬间百万/千万的点击量，会把Redis内存打爆

update 阅读量这个key  修改最新的阅读数字

那种10w+ /100w + 的总统计量，是一种保护程序稳定性的做法

```java
if(number >= 10w+){
    return;
}else{
    incr k1;
}
// 如果超过10w还需要详细统计的话，需要使用set + 时间片的方法 
```

## 3、三种特殊类型

为什么出来这三种类型？

先看看大厂真实需求 + 面试题反馈

面试题1：

```
手机App中的每天的⽤⼾登录信息：1天对应1系列⽤⼾ID或移动设备ID；
电商⽹站上商品的⽤⼾评论列表：1个商品对应了1系列的评论；
⽤⼾在⼿机App上的签到打卡信息：1天对应1系列⽤⼾的签到记录；
应⽤⽹站上的⽹⻚访问信息：1个⽹⻚对应1系列的访问点击。

// 统计登录信息、打卡信息、评论信息、日活等
```

面试题2：

```
记录对集合中的数据进行统计

在移动应用中，需要统计每天的新增用户数和第2天的留存用户数；
在电商网站的商品评论中，需要统计评论列表中的最新评论；
在签到打卡中，需要统计一个月内连续打卡的用户数；
在网页访问记录中，需要统计独立访客（UniqueVisitor，UV）量。

痛点：
类似今日头条、抖音、淘宝这样的额用户访问级别都是亿级的，请问如何处理？

```

需求痛点：

亿级数据的收集 + 统计

要求：

存得进 + 取得快 + 多统计 

常用统计类型

- 聚合统计

统计多个集合元素的聚合效果，就前面讲解过的 **交差并等集合统计**。交并差集和聚合函数的使用

- 排序统计

抖音视频最新评论留言的场景，请你设计一个列表展示。考察你的数据结构和设计思路

```
以抖音vcr最新的留言评价为案例，所有评论需要两个功能，按照时间排序+分页显示

 能够排序+分页显示的redis数据结构是什么合适？
```

使用List解答

```
每个商品评价对应一个List集合，这个List包含了对这个商品的所有评论，而且会按照评论时间保存这些评论，
每来一个新评论就用LPUSH命令把它插入List的队头。但是，如果在演示第二页前，又产生了一个新评论，
第2页的评论不一样了。原因：
List是通过元素在List中的位置来排序的，当有一个新元素插入时，原先的元素在List中的位置都后移了一位，
原来在第1位的元素现在排在了第2位，当LRANGE读取时，就会读到旧元素。
```

![image-20220523222802768](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523222802768.png)

使用Zset解答

![image-20220523222833915](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523222833915.png)

总结：

==在面对需要展示最新列表、排行榜等场景时，如果需要对数据更新频繁或者需要分页显示，建议使用Zset==

- 二值统计

集合元素的取值就只有0和1两种，在钉钉上班签到打卡的场景中，我们只用记录有签到（1）或没签到（0）

使用bitmap是最佳选择（统计打卡）

- 基数统计

指统计一个集合中 **不重复的元素个数**

使用hyperloglog是最佳选择（统计日活）



#### 1、BitMap

> 是什么？

![image-20220523223337277](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523223337277.png)

==说明：用String类型作为底层数据结构实现的一种统计二值状态的数据类型==

位图本质是数组，它是基于String数据类型的按位的操作。该数组由多个二进制位组成，每个二进制位都对应一个偏移量（我们称为一个索引或者位格）。

Bitmap支持的最大位数是2^32^位，它可以极大的节约存储空间，使用512M内存就可以存储多达42.9亿的字节信息（2^32^=4294967296）

总的来说就是由0和1状态表现得二进制位得bit数组

> 能干嘛？

1、用于状态统计

​	Y、N，类似于AtomicBoolean

2、根据需求做对应得统计功能

​	eg：用户是否登录过Y、N，比如京东每日签到送京豆

​			电影、广告是否被点击播放过

​			钉钉打卡上下班，签到统计

3、大厂真实案例

​	eg：日活统计（建议使用hyperloglog）

​			连续签到打卡

​			最近一周得活跃用户

​			统计用户一年内登录得天数

> 真实案例：京东签到领取京豆

需求说明：

![image-20220523230424746](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523230424746.png)

小厂传统方式使用Mysql统计：

建表SQL

```sql
CREATE TABLE user_sign
(
  keyid BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  user_key VARCHAR(200),#京东用户ID
  sign_date DATETIME,#签到日期(20210618)
  sign_count INT #连续签到天数
)
 
INSERT INTO user_sign(user_key,sign_date,sign_count)
VALUES ('20210618-xxxx-xxxx-xxxx-xxxxxxxxxxxx','2020-06-18 15:11:12',1);
 
SELECT
    sign_count
FROM
    user_sign
WHERE
    user_key = '20210618-xxxx-xxxx-xxxx-xxxxxxxxxxxx'
    AND sign_date BETWEEN '2020-06-17 00:00:00' AND '2020-06-18 23:59:59'
ORDER BY
    sign_date DESC
    LIMIT 1;
 

```

困难和解决思路

```
方法正确但是难以落地实现

签到用户量较小时这么设计能行，但京东这个体量的用户（估算3000W签到用户，一天一条数据，一个月就是9亿数据）

对于京东这样的体量，如果一条签到记录对应着当日用记录，那会很恐怖......(再牛逼得数据库也不够用啊，而且是mysql是关系型数据库，取数据得时候非常慢)
```

如何解决：

1 一条签到记录对应一条记录，会占据越来越大的空间。

2 一个月最多31天，刚好我们的int类型是32位，那这样一个int类型就可以搞定一个月，32位大于31天，当天来了位是1没来就是0。

3 一条数据直接存储一个月的签到记录，不再是存储一天的签到记录。

大厂方法：基于Redis得Bitmap实现签到日历

建表-按位-redis bitmap

在签到统计时，每个用户一天的签到用1个bit位就能表示，
一个月（假设是31天）的签到情况用31个bit位就可以，一年的签到也只需要用365个bit位，根本不用太复杂的集合类型

> Bitmap基本命令

setbit

```
set key offset value 

setbit键 偏移量 只能是0或者1

重点：bitmap的偏移量是从0开始算的
```

![image-20220523231253551](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523231253551.png)

getbit

```
getbit key offset

get key （也是对的，bitmap本质是string，会输出二进制位对应的ascii的内容）
```

setbit和getbit案例说明

按照天

![image-20220523231515212](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523231515212.png)

按照年

按年去存储一个用户的签到情况，365 天只需要 365 / 8 ≈ 46 Byte，1000W 用户量一年也只需要 44 MB 就足够了。

假如是亿级的系统，每天使用1个1亿位的Bitmap约占12MB的内存（10^8/8/1024/1024）

10天的Bitmap的内存开销约为120MB，内存压力不算太高。

在实际使用时，最好对Bitmap设置过期时间，让Redis自动删除不再需要的签到记录以节省内存开销。

bitmap的底层编码说明，get命令如何操作?

- 实质是二进制的ascii编码对应
- redis里面使用type命令看看bitmap实质是什么类型？string
- man ascii

![image-20220523231806186](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523231806186.png)

- 设置命令

![image-20220523231823560](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523231823560.png)

![image-20220523231831280](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523231831280.png)

strlen

![image-20220523231859600](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523231859600.png)

不是字符串长度而是占据几个字节，超过8位后自己按照8位一组一byte再扩容，统计字节数占用多少

bitcount

全部键里面含有1的有多少个？

![image-20220523232012550](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523232012550.png)

一年365天，全年天天登录占用多少个字节？

![image-20220523232056589](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523232056589.png)

bitop

连续2天都签到的用户

加入某个网站或者系统，它的用户有1000W，做个用户id和位置的映射
比如0号位对应用户id：uid-092iok-lkj
比如1号位对应用户id：uid-7388c-xxx

![image-20220523232122141](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220523232122141.png)



#### 2、hyperloglog

互联网统计常用名词解释

什么是UV：

​	unique visitor ： 独立访客，一般理解为客户端IP，需要去重考虑

什么是PV：

​	page View 页面浏览量，不用去重

什么是DAU：

​	daily active user ：日活跃用户量，登录或者使用了某个产品的用户数（去重登录的用户），常用于反映网站、互联网应用或者是网络游戏的运营情况

什么是MAU：

​	monthly active user：月活跃量



需求：

- 统计某个网站的UV，统计某个文章的UV
- 用户搜索网站关键词的数量
- 统计用户每天搜索不同词条个数

> hyperloglog是什么？

去重复统计功能的基数估计算法-就是hyperloglog

![image-20220525232951840](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220525232951840.png)

> 什么叫基数？

​	是一种数据集，去重复后的真实个数就叫基数

![image-20220525233040369](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220525233040369.png)

基数统计：用于统计一个集合中不重复的元素个数，就是对集合去重复后剩余元素的计算

一句话：去重脱水后的真实数据

> hyperloglog如何做的？如何演化出来的？

基数统计就是用hyperloglog

去重复统计你会想到哪些方式？

1、java - HashSet

2、bitmap

如果数据显较大亿级统计,使用bitmaps同样会有这个问题。

bitmap是通过用位bit数组来表示各元素是否出现，每个元素对应一位，所需的总内存为N个bit。

基数计数则将每一个元素对应到bit数组中的其中一位，比如bit数组010010101(按照从零开始下标，有的就是1、4、6、8)。
新进入的元素只需要将已经有的bit数组和新加入的元素进行按位或计算就行。这个方式能大大减少内存占用且位操作迅速。

But，假设一个样本案例就是一亿个基数位值数据，一个样本就是一亿

**如果要统计1亿个数据的基数位值,大约需要内存100000000/8/1024/1024约等于12M,内存减少占用的效果显著。**

这样得到统计一个对象样本的基数值需要12M。

**如果统计10000个对象样本(1w个亿级),就需要117.1875G将近120G，可见使用bitmaps还是不适用大数据量下(亿级)的基数计数场景，但是bitmaps方法是精确计算的。**

结论：样本元素越多越内存消耗急剧增大，难以管控+各种慢，对于亿级统计不太适合，大数据害死人。

办法？概率算法？

==通过牺牲准确率来换取空间，对于不要求绝对准确率的场景下可以使用，因为概率算法不直接存储数据本身，
通过一定的概率统计方法预估基数值，同时保证误差在一定范围内，由于又不储存数据故此可以大大节约内存。==

**HyperLogLog就是一种概率算法的实现。**

> 原理说明

只是进行不重复得基数统计，不是集合也不保存数据，只记录数量而不是具体内容。

有误差：

​	非精确统计，牺牲准确率来换取空间，误差仅仅是0.81%左右

这个误差如何来的？

​	Redis之父的回答：

![image-20220526000600513](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526000600513.png)

经典面试题：

​	为什么redis集群的最大槽数是16384个？

​	Redis集群并没有使用一致性hash而是引入了哈希槽的概念。Redis 集群有16384个哈希槽，每个key通过CRC16校验后对16384取模来决定放置哪个槽，集群的每个节点负责一部分hash槽。但为什么哈希槽的数量是16384（2^14）个呢？

CRC16算法产生的hash值有16bit，该算法可以产生2^16=65536个值。
换句话说值是分布在0~65535之间。那作者在做mod运算的时候，为什么不mod65536，而选择mod16384？

回答：

```
正常的心跳数据包带有节点的完整配置，可以用幂等方式用旧的节点替换旧节点，以便更新旧的配置。
这意味着它们包含原始节点的插槽配置，该节点使用2k的空间和16k的插槽，但是会使用8k的空间（使用65k的插槽）。同时，由于其他设计折衷，Redis集群不太可能扩展到1000个以上的主节点。
因此16k处于正确的范围内，以确保每个主机具有足够的插槽，最多可容纳1000个矩阵，但数量足够少，可以轻松地将插槽配置作为原始位图传播。请注意，在小型群集中，位图将难以压缩，因为当N较小时，位图将设置的slot / N位占设置位的很大百分比。
```

![image-20220526000920340](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526000920340.png)

(1)如果槽位为65536，发送心跳信息的消息头达8k，发送的心跳包过于庞大。
在消息头中最占空间的是myslots[CLUSTER_SLOTS/8]。 当槽位为65536时，这块的大小是: 65536÷8÷1024=8kb 
因为每秒钟，redis节点需要发送一定数量的ping消息作为心跳包，如果槽位为65536，这个ping消息的消息头太大了，浪费带宽。

(2)redis的集群主节点数量基本不可能超过1000个。
集群节点越多，心跳包的消息体内携带的数据越多。如果节点过1000个，也会导致网络拥堵。因此redis作者不建议redis cluster节点数量超过1000个。 那么，对于节点数在1000以内的redis cluster集群，16384个槽位够用了。没有必要拓展到65536个。

(3)槽位越小，节点少的情况下，压缩比高，容易传输
Redis主节点的配置信息中它所负责的哈希槽是通过一张bitmap的形式来保存的，在传输过程中会对bitmap进行压缩，但是如果bitmap的填充率slots / N很高的话(N表示节点数)，bitmap的压缩率就很低。 如果节点数很少，而哈希槽数量很多的话，bitmap的压缩率就很低。 

> 基本命令

![image-20220526001021737](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526001021737.png)

![image-20220526001029291](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526001029291.png)

> 实战案例：天猫网站首页亿级UV的Redis统计方案

需求：

UV的统计需要去重，一个用户一天内的多次访问只能算作一次

淘宝、天猫首页的UV，平均每天是1-1.5亿左右

每天存1.5个亿的IP，访问者来了后先去查是否存在，不存在加入

方案讨论：

​	用mysql -不行，亿级，不合适

​	用redis的hash结构存储

![image-20220526001347440](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526001347440.png)

redis——hash = <keyDay,<ip,1>>

按照ipv4的结构来说明，每个ipv4的地址最多是15个字节(ip = "192.168.111.1"，最多xxx.xxx.xxx.xxx)

某一天的1.5亿 * 15个字节= 2G，一个月60G，redis死定了

​	用hyperloglog

![image-20220526001516041](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526001516041.png)

HyperLogLogController

```java
@Api(description = "案例实战总03:天猫网站首页亿级UV的Redis统计方案")
@RestController
@Slf4j
public class HyperLogLogController
{

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation("获得ip去重复后的首页访问量，总数统计")
    @RequestMapping(value = "/uv",method = RequestMethod.GET)
    public long uv()
    {
        //pfcount
        return redisTemplate.opsForHyperLogLog().size("hll");
    }
}
```

hyperloglogService

```java
@Service
@Slf4j
public class HyperLogLogService {
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 模拟有用户来点击首页，每个用户就是不同的ip，不重复记录，重复不记录
     */
    //@PostConstruct
    public void init() {
        log.info("------模拟后台有用户点击，每个用户ip不同");
        //自己启动线程模拟，实际上产不是线程
        new Thread(() -> {
            String ip = null;
            Random random = new Random();
            for (int i = 1; i <= 200; i++) {

                ip = random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255);

                Long hll = redisTemplate.opsForHyperLogLog().add("hll", ip);
                log.info("ip={},该ip访问过的次数={}", ip, hll);
                //暂停3秒钟线程
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t1").start();
    }
}
```



#### 3、GEO

地理位置知识简介

移动互联网时代LBS应用越来越多，交友软件中附近的小姐姐、外卖软件中附近的美食店铺、打车软件附近的车辆等等，那这种附近各种形形色色的XXX地址位置选择是如何实现的？

地球上的地理位置是使用二维的经纬度表示，经度范围 (-180, 180]，纬度范围 (-90, 90]，只要我们确定一个点的经纬度就可以名曲他在地球的位置。

例如滴滴打车，最直观的操作就是实时记录更新各个车的位置，

然后当我们要找车时，在数据库中查找距离我们(坐标x0,y0)附近r公里范围内部的车辆

使用如下SQL即可：

select taxi from position where x0-r < x < x0 + r and y0-r < y < y0+r

但是这样会有什么问题呢？
1.查询性能问题，如果并发高，数据量大这种查询是要搞垮数据库的

2.这个查询的是一个矩形访问，而不是以我为中心r公里为半径的圆形访问。

3.精准度的问题，我们知道地球不是平面坐标系，而是一个圆球，这种矩形计算在长距离计算时会有很大误差



所以Redis在3.2版本以后添加对地理位置的处理

> 原理

![image-20220526001837517](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526001837517.png)

> 命令

```
geoadd 多个经度(longitude)、纬度(latitude)、位置名称(member) 添加到指定的key中

geopos 从键里面返回所有给定位置元素得的位置（经度和纬度）

geodist 返回给定2个位置之间的距离

georadius 以给定的经纬度为中心，返回与中心距离不超过给定最大距离的所有位置元素

georadiusbymember georadius类似

geohash 返回一个或多个位置元素的geohash表示
```

> 命令实操

geoadd添加经纬度坐标

![image-20220526002509143](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526002509143.png)

GEOADD city 116.403963 39.915119 "天安门" 116.403414 39.924091 "故宫" 116.024067 40.362639 "长城"

中文乱码处理

redis-cli --raw

geopos返回经纬度

![image-20220526002608435](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526002608435.png)

geohash返回坐标的geohash表示

geohash算法生成base32位编码值，3维-2维-1维-二进制base32位编码

geodist两个位置之间距离

![image-20220526002809906](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526002809906.png)

georadius 以半斤为中心，查找附近的xxxx

georadius 以给定的经纬度为中心， 返回键包含的位置元素当中， 与中心的距离不超过给定最大距离的所有位置元素。

GEORADIUS city 116.418017 39.914402 10 km withdist withcoord count 10 withhash desc

```
WITHDIST: 在返回位置元素的同时， 将位置元素与中心之间的距离也一并返回。 距离的单位和用户给定的范围单位保持一致。
WITHCOORD: 将位置元素的经度和维度也一并返回。
WITHHASH: 以 52 位有符号整数的形式， 返回位置元素经过原始 geohash 编码的有序集合分值。 这个选项主要用于底层应用或者调试， 实际中的作用并不大
COUNT 限定返回的记录数。
```

![image-20220526002911815](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526002911815.png)

georadiusbymember

![image-20220526002951523](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526002951523.png)

> 案例实战，美团地图位置附近酒店推送

需求分析：

![image-20220526003029859](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220526003029859.png)

架构设计：

Redis的新类型GEO

编码实现：

关键点：使用georadius 给定经纬度，找出某一半径内的元素

```java
@RestController
public class GeoController
{
    public  static final String CITY ="city";

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation("新增天安门故宫长城经纬度")
    @RequestMapping(value = "/geoadd",method = RequestMethod.POST)
    public String geoAdd()
    {
        Map<String, Point> map= new HashMap<>();
        map.put("天安门",new Point(116.403963,39.915119));
        map.put("故宫",new Point(116.403414 ,39.924091));
        map.put("长城" ,new Point(116.024067,40.362639));

        redisTemplate.opsForGeo().add(CITY,map);

        return map.toString();
    }

    @ApiOperation("获取地理位置的坐标")
    @RequestMapping(value = "/geopos",method = RequestMethod.GET)
    public Point position(String member) {
        //获取经纬度坐标
        List<Point> list= this.redisTemplate.opsForGeo().position(CITY,member);
        return list.get(0);
    }

    @ApiOperation("geohash算法生成的base32编码值")
    @RequestMapping(value = "/geohash",method = RequestMethod.GET)
    public String hash(String member) {
        //geohash算法生成的base32编码值
        List<String> list= this.redisTemplate.opsForGeo().hash(CITY,member);
        return list.get(0);
    }

    @ApiOperation("计算两个位置之间的距离")
    @RequestMapping(value = "/geodist",method = RequestMethod.GET)
    public Distance distance(String member1, String member2) {
        Distance distance= this.redisTemplate.opsForGeo().distance(CITY,member1,member2, RedisGeoCommands.DistanceUnit.KILOMETERS);
        return distance;
    }

    /**
     * 通过经度，纬度查找附近的
     * 北京王府井位置116.418017,39.914402,这里为了方便讲课，故意写死
     */
    @ApiOperation("通过经度，纬度查找附近的")
    @RequestMapping(value = "/georadius",method = RequestMethod.GET)
    public GeoResults radiusByxy() {
        //这个坐标是北京王府井位置
        Circle circle = new Circle(116.418017, 39.914402, Metrics.MILES.getMultiplier());
        //返回50条
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().includeCoordinates().sortAscending().limit(10);
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults= this.redisTemplate.opsForGeo().radius(CITY,circle, args);
        return geoResults;
    }

    /**
     * 通过地方查找附近
     */
    @ApiOperation("通过地方查找附近")
    @RequestMapping(value = "/georadiusByMember",method = RequestMethod.GET)
    public GeoResults radiusByMember() {
        String member="天安门";
        //返回50条
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().includeCoordinates().sortAscending().limit(10);
        //半径10公里内
        Distance distance=new Distance(10, Metrics.KILOMETERS);
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults= this.redisTemplate.opsForGeo().radius(CITY,member, distance,args);
        return geoResults;
    }
}

```

