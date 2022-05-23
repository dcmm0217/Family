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



#### 3、GEO
