# 第6节-Redis分布式锁

常问面试题：

- Redis除了用来做缓存，你还见过4基于Redis得到什么用法？
- Redis做分布式锁的时候有需要注意的问题
- 如果Redis是单节点部署的，会带来什么问题？那你准备怎么解决单节点问题呢？
- 集群模式下，比如主从模式，有没有什么问题？
- 你知道Redis是怎么解决集群模式也不靠谱的问题的吗？
- 简单介绍下RedLock把？说说redisson
- 你觉得RedLock有什么问题？
- Redis分布式锁如何续期？看门狗策略你知道吗?

## 1、锁的种类

单机版同一个JVM虚拟机内，synchronized或者Lock锁

分布式不同JVM虚拟机内，单机的线程锁机制不再起作用，资源类在不同服务器之间共享了。

## 2、靠谱的分布式锁必备条件

- 独占性：onlyone，任何时刻只能有且仅有一个线程获得锁
- 高可用：若redis集群环境下，不能因为某一个节点挂了而出现获取锁和释放锁失败的情况
- 防死锁：杜绝死锁，必须有超时控制机制或者撤销操作，有个兜底的跳出方案
- 不乱抢：防止张冠李戴，不能私下unlock别人的锁，只能自己加锁自己释放
- 重入性：同一个节点的同一个线程如果获得锁之后，它也可以再次获取这个锁

分布式锁的原理

```
setnx key value [ex(key在多少秒后过期)]
当key不存在的时候，创建key
当key存在，不进行任何操作
```

## 3、Base案例

**使用场景：多个服务间保证同一时刻同一时间段内同一用户只能有一个请求（防止关键业务出现并发攻击）**

改POM

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.10.RELEASE</version>
        <relativePath/>
    </parent>

    <groupId>com.atguigu.redis</groupId>
    <artifactId>boot_redis01</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
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
        <!--一般通用基础配置-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
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

写yml

```properties

# 端口号
server.port=1111

# ========================redis相关配置=====================
# Redis数据库索引（默认为0）
spring.redis.database=0  
# Redis服务器地址
spring.redis.host=192.168.111.140
# Redis服务器连接端口
spring.redis.port=6379  
# Redis服务器连接密码（默认为空）
spring.redis.password=
# 连接池最大连接数（使用负值表示没有限制） 默认 8
spring.redis.lettuce.pool.max-active=8
# 连接池最大阻塞等待时间（使用负值表示没有限制） 默认 -1
spring.redis.lettuce.pool.max-wait=-1
# 连接池中的最大空闲连接 默认 8
spring.redis.lettuce.pool.max-idle=8
# 连接池中的最小空闲连接 默认 0
spring.redis.lettuce.pool.min-idle=0
```

业务类

config

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory)
    {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);

        return redisTemplate;
    }
}
```

controller

```java
@RestController
public class GoodController
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/buy_goods")
    public String buy_Goods()
    {
        String result = stringRedisTemplate.opsForValue().get("goods:001");
        int goodsNumber = result == null ? 0 : Integer.parseInt(result);

        if(goodsNumber > 0)
        {
            int realNumber = goodsNumber - 1;
            stringRedisTemplate.opsForValue().set("goods:001",realNumber + "");
            System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort);
            return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort;
        }else{
            System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort);
        }
        
        return "商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort;
    }
}
```

测试：

![image-20220531144346008](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531144346008.png)

**该程序的问题：？**

1、单机版没加锁

问题：没有加锁，并发下数字不对，出现超卖

思考：加syn 或者 lock  都ok的，粒度来说的话加lock会更好

解决：

```java
@RestController
public class GoodController
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final Lock lock = new ReentrantLock();

    @GetMapping("/buy_goods")
    public String buyGoods() throws InterruptedException
    {
        /*synchronized (this)
        {
            String number = stringRedisTemplate.opsForValue().get("goods:001");

            int realNumber = number == null ? 0 : Integer.parseInt(number);

            if(realNumber > 0)
            {
                realNumber = realNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001",String.valueOf(realNumber));
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件";
            }
        }*/

        //if (lock.tryLock(2L,TimeUnit.SECONDS))
        if (lock.tryLock())
        {
            try
            {
                String number = stringRedisTemplate.opsForValue().get("goods:001");

                int realNumber = number == null ? 0 : Integer.parseInt(number);

                if(realNumber > 0)
                {
                    realNumber = realNumber - 1;
                    stringRedisTemplate.opsForValue().set("goods:001",String.valueOf(realNumber));
                    return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件";
                }
            }finally {
                lock.unlock();
            }
        }
        return "商品售罄/活动结束，欢迎下次光临";
    }
}
```

在单机环境下，可以使用synchronized或Lock来实现。

但是在分布式系统中，因为竞争的线程可能不在同一个节点上（同一个jvm中），所以需要一个让所有进程都能访问到的锁来实现(比如redis或者zookeeper来构建)。不同进程jvm层面的锁就不管用了，那么可以利用第三方的一个组件，来获取锁，未获取到锁，则阻塞当前想要运行的线程

2、nginx分布式微服务架构

问题：分布式部署后，单机锁还是出现超卖现象，需要分布式锁

![image-20220531145141065](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531145141065.png)

Nginx配置负载均衡

命令地址+配置地址

命令地址  /usr/local/nginx/sbin

配置地址 /usr/local/nginx/conf

启动Nginx并测试通过  /usr/local/nginx/conf

nginx.conf

![image-20220531145341779](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531145341779.png)

如果之前启动过Nginx，修改完上一步配置后请重启 /usr/local/nginx/sbin目录下执行如下命令  

./nginx -s reload

/usr/local/nginx/sbin/nginx目录下执行命令

./nginx -c /usr/local/nginx/conf/nginx.conf

基础命令

启动：./nginx

关闭：./nginx -s stop

重启：./nginx -s reload

启动2个微服务

端口 1111   2222

通过nginx访问，反向代理+负载均衡

使用jmeter模拟高并发请求

![image-20220531145743752](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531145743752.png)

出现超卖情况

解决：

上redis分布式锁setnx

![image-20220531145826775](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531145826775.png)

```java
@RestController
public class GoodController
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/buy_goods")
    public String buy_Goods()
    {
        String key = "zzyyRedisLock";
        String value = UUID.randomUUID().toString()+Thread.currentThread().getName();

        Boolean flagLock = stringRedisTemplate.opsForValue().setIfAbsent(key, value);
        if(!flagLock)
        {
            return "抢夺锁失败,o(╥﹏╥)o";
        }

        String result = stringRedisTemplate.opsForValue().get("goods:001");
        int goodsNumber = result == null ? 0 : Integer.parseInt(result);

        if(goodsNumber > 0)
        {
            int realNumber = goodsNumber - 1;
            stringRedisTemplate.opsForValue().set("goods:001",realNumber + "");
            stringRedisTemplate.delete(key);
            System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort);
            return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort;
        }else{
            System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort);
        }

        return "商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort;


    }
}
```

3、finally必须关闭锁资源，出现异常，可能无法释放锁，必须要在代码层面finally释放锁

解决：加锁解锁，locl/unlock必须同时出现并且保证调用

修改为4.0版本

```java
@RestController
public class GoodController
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/buy_goods")
    public String buy_Goods()
    {
        String key = "zzyyRedisLock";
        String value = UUID.randomUUID().toString()+Thread.currentThread().getName();

        try {
            Boolean flagLock = stringRedisTemplate.opsForValue().setIfAbsent(key, value);
            if(!flagLock)
            {
                return "抢锁失败,o(╥﹏╥)o";
            }

            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);

            if(goodsNumber > 0)
            {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001",realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort);
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort;
            }else{
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort);
            }
            return "商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort;
        } finally {
            stringRedisTemplate.delete(key);
        }
    }
}
```

4、如果服务宕机怎么办？

微服务宕机了，恰好到吗层面根本没有走到finally这块，没办法保证解锁，这个key没有被删除，需要加入一个过期时间限定key

修改为5.0版本

```java
@RestController
public class GoodController
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/buy_goods")
    public String buy_Goods()
    {
        String key = "zzyyRedisLock";
        String value = UUID.randomUUID().toString()+Thread.currentThread().getName();

        try {
            Boolean flagLock = stringRedisTemplate.opsForValue().setIfAbsent(key, value);

            stringRedisTemplate.expire(key,10L,TimeUnit.SECONDS);

            if(!flagLock)
            {
                return "抢锁失败,o(╥﹏╥)o";
            }

            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);

            if(goodsNumber > 0)
            {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001",realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort);
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort;
            }else{
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort);
            }
            return "商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort;
        } finally {
            stringRedisTemplate.delete(key);
        }
    }
}
```

5、设置key+过期时间分开了，必须要合成一行具备原子性

```java
@RestController
public class GoodController
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/buy_goods")
    public String buy_Goods()
    {
        String key = "zzyyRedisLock";
        String value = UUID.randomUUID().toString()+Thread.currentThread().getName();

        try {
            Boolean flagLock = stringRedisTemplate.opsForValue().setIfAbsent(key,value,10L,TimeUnit.SECONDS);

            if(!flagLock)
            {
                return "抢锁失败,o(╥﹏╥)o";
            }

            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);

            if(goodsNumber > 0)
            {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001",realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort);
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort;
            }else{
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort);
            }
            return "商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort;
        } finally {
            stringRedisTemplate.delete(key);
        }
    }
}
```

6、张冠李戴，删除了别人的锁

如果A线程进来，拿到锁以后，业务执行时间超过了锁的超时时间，当B线程访问时，拿到了分布式锁，A执行完逻辑后，删除了B的锁，B解锁的时候就会报错。

![image-20220531150613411](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531150613411.png)

```java
@RestController
public class GoodController
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/buy_goods")
    public String buy_Goods()
    {
        String key = "zzyyRedisLock";
        String value = UUID.randomUUID().toString()+Thread.currentThread().getName();

        try {
            Boolean flagLock = stringRedisTemplate.opsForValue().setIfAbsent(key,value,10L,TimeUnit.SECONDS);

            if(!flagLock)
            {
                return "抢锁失败,o(╥﹏╥)o";
            }

            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);

            if(goodsNumber > 0)
            {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001",realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort);
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort;
            }else{
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort);
            }
            return "商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort;
        } finally {
            if (stringRedisTemplate.opsForValue().get(key).equals(value)) {
                stringRedisTemplate.delete(key);
            }
        }
    }
}
```

7、finally块的判断 + del操作不是原子性的

解决：采用Lua脚本，Redis调用Lua脚本通过eval命令保证代码执行的原子性

RedisUtils

```java
public class RedisUtils
{
    private static JedisPool jedisPool;

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
        throw new Exception("Jedispool was not init");
    }

}
```

```java
@RestController
public class GoodController
{
    public static final String REDIS_LOCK_KEY = "redisLockPay";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${server.port}")
    private String serverPort;


    @GetMapping("/buy_goods")
    public String buy_Goods()
    {
        String value = UUID.randomUUID().toString()+Thread.currentThread().getName();

        try {
            Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK_KEY, value,30L,TimeUnit.SECONDS);

            if(!flag)
            {
                return "抢夺锁失败，请下次尝试";
            }

            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);

            if(goodsNumber > 0)
            {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001",realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort);
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort;
            }else{
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort);
            }
            return "商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort;
        } finally {
            Jedis jedis = RedisUtils.getJedis();

            String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                    "then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";

            try {
                Object result = jedis.eval(script, Collections.singletonList(REDIS_LOCK_KEY), Collections.singletonList(value));
                if ("1".equals(result.toString())) {
                    System.out.println("------del REDIS_LOCK_KEY success");
                }else{
                    System.out.println("------del REDIS_LOCK_KEY error");
                }
            } finally {
                if(null != jedis) {
                    jedis.close();
                }
            }

        }
    }
}
```

截至到这里，基于单个Redis节点实现分布式即可

8、确保redisLock过期时间大于业务的执行时间，Redis分布式锁如何续期？

CAP理论

![image-20220531152955237](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531152955237.png)

C：

一致性指的是所有节点在同一时间的数据完全一致。就好比刚刚举得例子中，小明和小华读取的都是正确的数据，对他们用户来说，就好像是操作了同一个数据库的同一个数据一样。

A：

可用性指服务一直可用，而且是正常响应时间。就好比刚刚的N1和N2节点，不管什么时候访问，都可以正常的获取数据值。而不会出现问题。好的可用性主要是指系统能够很好的为用户服务，不出现用户操作失败或者访问超时等用户体验不好的情况。

P：

分区容错性指在遇到某节点或网络分区故障的时候，仍然能够对外提供满足一致性和可用性的服务。就好比是N1节点和N2节点出现故障，但是依然可以很好地对外提供服务。

集群+CAP   redis对比zookeeper  主要对比zookeeper

Redis单机时CP，集群是AP

redis集群，保证AP，redis异步复制造成的锁丢失，

比如：主节点没来的及把刚刚set进来的这条数据给从节点，master就挂了，从机上位但从机上无该数据。

zookeeper集群的CAP

能保证CP，先进行数据同步，再返回成功标识

![image-20220531154744959](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531154744959.png)

故障问题：

![image-20220531154841237](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531154841237.png)

顺便复习Eureka集群注册中心的CAP

AP

![image-20220531154956123](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531154956123.png)

9、综上所述

Redis集群环境下，我们自己写的分布式锁也不OK，我们之间采用官方推荐的RedLock之Redisson落地实现

下面是基于多个Redis节点实现高可用得分布式锁

使用Redisson

RedisConfig

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory)
    {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);

        return redisTemplate;
    }

    @Bean
    public Redisson redisson()
    {
        Config config = new Config();

        config.useSingleServer().setAddress("redis://192.168.111.140:6379").setDatabase(0);

        return (Redisson) Redisson.create(config);
    }
}
```

业务代码修改

```java
@RestController
public class GoodController
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private Redisson redisson;

    @GetMapping("/buy_goods")
    public String buy_Goods(){
        String key = "zzyyRedisLock";
        RLock redissonLock = redisson.getLock(key);
        redissonLock.lock();
        try{
            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);

            if(goodsNumber > 0)
            {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001",realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort);
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort;
            }else{
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort);
            }
            return "商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort;
        }finally {
            redissonLock.unlock();
        }
    }
}
```

9.0完善到9.1

```java
@RestController
public class GoodController
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private Redisson redisson;

    @GetMapping("/buy_goods")
    public String buy_Goods()
    {
        String key = "zzyyRedisLock";

        RLock redissonLock = redisson.getLock(key);
        redissonLock.lock();

        try
        {
            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);

            if(goodsNumber > 0)
            {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001",realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort);
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort;
            }else{
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort);
            }
            return "商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort;
        }finally {
            if(redissonLock.isLocked() && redissonLock.isHeldByCurrentThread())
            {
              redissonLock.unlock();
            }
        }
    }
}
```

如此，基本分布式锁就不会出现太大问题了。

总结下步骤：

1、synchronized单机版ok，要上分布式

2、nginx分布式微服务，单机锁不行

3、取消单机锁，上redis分布式锁setnx

4、只加了锁，没有释放锁，出现异常，可能无法释放锁，必须要在代码层面finally释放锁

5、服务宕机，代码没有走到finally这块，没有办法保证解锁，这个key没有被删除，需要有lockKey得过期时间保证

6、为Redis得分布式锁key，增加过期时间，此外，还必须要setnx+过期时间必须保证原子性

7、必须规定只能自己删除了自己的锁，防止张冠李戴

8、Redis集群环境下，我们自己写的也不OK，之间使用RedLock之Redisson落地实现

## 4、Redis分布式锁-RedLock算法

Redis分布式锁比较正确的姿势是采用Redisson这个客户端工具

天上飞的理论（RedLock）必然有落地的实现（Redisson）

> 单机案例

三个重要元素

- 加锁：加锁实际上就是在redis中，给key键设置一个值，避免死锁，给定一个过期时间
- 解锁：将key键删除，但是也不能乱删，不能说客户端1的请求给客户端2的锁给删掉，只能自己删自己的

为了保证解锁操作的原子性，我们用LUA脚本完成这一操作。先判断当前锁的字符串是否与传入的值相等，是的话就删除Key，解锁成功。

```java
if redis.call('get',KEYS[1]) == ARGV[1] then 
   return redis.call('del',KEYS[1]) 
else
   return 0 
end
```

- 超时：锁key要注意过期时间，不能长时间占用

面试中回答的主要考点：

加锁关键逻辑

```java
/**
 * 
 * @param key
 * @param uniqueId
 * @param seconds
 * @return
 */
public static boolean tryLock(String key, String uniqueId, int seconds) {
    return "OK".equals(jedis.set(key, uniqueId, "NX", "EX", seconds));
}
```

解锁关键逻辑

```java
/**
 *
 * @param key
 * @param uniqueId
 * @return
 */
public static boolean releaseLock(String key, String uniqueId) {
    String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('del', KEYS[1]) else return 0 end";
    
    return jedis.eval(
            luaScript,
            Collections.singletonList(key),
            Collections.singletonList(uniqueId)
    ).equals(1L);
}
```

单机模式中，一般都是用set/setnx+lua脚本搞定，其实这么做是有缺点的

但是不是高并发场景，也可使用，单机Redis小业务也撑得住

> 多机案例

基于setnx的分布式锁有什么缺点？

![image-20220531212556301](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531212556301.png)

线程 1 首先获取锁成功，将键值对写入 redis 的 master 节点；
在 redis 将该键值对同步到 slave 节点之前，master 发生了故障；
redis 触发故障转移，其中一个 slave 升级为新的 master；
此时新的 master 并不包含线程 1 写入的键值对，因此线程 2 尝试获取锁也可以成功拿到锁；
此时相当于有两个线程获取到了锁，可能会导致各种预期之外的情况发生，例如最常见的脏数据。

**我们加的是排它独占锁，同一时间只能有一个建redis锁成功并持有锁，严禁出现2个以上的请求线程拿到锁。危险的**

Redis之父提出了RedLock算法解决这个问题

Redis也提供了Redlock算法，用来实现基于多个实例的分布式锁。
锁变量由多个实例维护，即使有实例发生了故障，锁变量仍然是存在的，客户端还是可以完成锁操作。Redlock算法是实现高可靠分布式锁的一种有效解决方案，可以在实际开发中使用。

RedLock算法设计理念

Redis的AP集群，问题：redis异步复制造成的锁丢失，主节点没来及把刚刚set进来的这条数据同步给从节点就挂了。

设计理念：

该方案也是基于（set 加锁、Lua 脚本解锁）进行改良的，所以redis之父antirez 只描述了差异的地方，大致方案如下。

假设我们有N个Redis主节点，例如 N = 5这些节点是完全独立的，我们不使用复制或任何其他隐式协调系统，

为了取到锁客户端执行以下操作：

![image-20220531212931771](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531212931771.png)

**该方案为了解决数据不一致的问题，直接舍弃了异步复制只使用 master 节点，同时由于舍弃了 slave，为了保证可用性，引入了 N 个节点，官方建议是 5。**

客户端只有在满足下面的这两个条件时，才能认为是加锁成功。

条件1：客户端从超过半数（大于等于N/2+1）的Redis实例上成功获取到了锁； N是节点数

条件2：客户端获取锁的总耗时没有超过锁的有效时间。

解决方案：

![image-20220531213238397](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531213238397.png)

为什么是奇数？  N = 2X + 1   (N是最终部署机器数，X是容错机器数)

![image-20220531213615823](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531213615823.png)

> 案例

docker起3台redis的master机器，本次设置3台master各自独立无从属关系

![image-20220531213741816](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531213741816.png)

进入上一步刚启动的redis容器实例

![image-20220531213818763](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531213818763.png)

工程配置文件yaml

```properties
spring.application.name=spring-boot-redis
server.port=9090

spring.swagger2.enabled=true


spring.redis.database=0
spring.redis.password=
spring.redis.timeout=3000
#sentinel/cluster/single
spring.redis.mode=single

spring.redis.pool.conn-timeout=3000
spring.redis.pool.so-timeout=3000
spring.redis.pool.size=10

spring.redis.single.address1=192.168.111.147:6381
spring.redis.single.address2=192.168.111.147:6382
spring.redis.single.address3=192.168.111.147:6383
```

业务类

CacheConfiguration.java

```java
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class CacheConfiguration {

    @Autowired
    RedisProperties redisProperties;

    @Bean
    RedissonClient redissonClient1() {
        Config config = new Config();
        String node = redisProperties.getSingle().getAddress1();
        node = node.startsWith("redis://") ? node : "redis://" + node;
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(node)
                .setTimeout(redisProperties.getPool().getConnTimeout())
                .setConnectionPoolSize(redisProperties.getPool().getSize())
                .setConnectionMinimumIdleSize(redisProperties.getPool().getMinIdle());
        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    RedissonClient redissonClient2() {
        Config config = new Config();
        String node = redisProperties.getSingle().getAddress2();
        node = node.startsWith("redis://") ? node : "redis://" + node;
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(node)
                .setTimeout(redisProperties.getPool().getConnTimeout())
                .setConnectionPoolSize(redisProperties.getPool().getSize())
                .setConnectionMinimumIdleSize(redisProperties.getPool().getMinIdle());
        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    RedissonClient redissonClient3() {
        Config config = new Config();
        String node = redisProperties.getSingle().getAddress3();
        node = node.startsWith("redis://") ? node : "redis://" + node;
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(node)
                .setTimeout(redisProperties.getPool().getConnTimeout())
                .setConnectionPoolSize(redisProperties.getPool().getSize())
                .setConnectionMinimumIdleSize(redisProperties.getPool().getMinIdle());
        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }


    /**
     * 单机
     * @return
     */
    /*@Bean
    public Redisson redisson()
    {
        Config config = new Config();

        config.useSingleServer().setAddress("redis://192.168.111.147:6379").setDatabase(0);

        return (Redisson) Redisson.create(config);
    }*/

}
```

RedisPoolProperties.java

```java
@Data
public class RedisPoolProperties {

    private int maxIdle;

    private int minIdle;

    private int maxActive;

    private int maxWait;

    private int connTimeout;

    private int soTimeout;

    /**
     * 池大小
     */
    private  int size;

}
```

RedisProperties.java

```java
@ConfigurationProperties(prefix = "spring.redis", ignoreUnknownFields = false)
@Data
public class RedisProperties {

    private int database;

    /**
     * 等待节点回复命令的时间。该时间从命令发送成功时开始计时
     */
    private int timeout;

    private String password;

    private String mode;

    /**
     * 池配置
     */
    private RedisPoolProperties pool;

    /**
     * 单机信息配置
     */
    private RedisSingleProperties single;


}
```

RedisSingleProperties.java

```java
@Data
public class RedisSingleProperties {
    private  String address1;
    private  String address2;
    private  String address3;
}
```

controller

```java
@RestController
@Slf4j
public class RedLockController {

    public static final String CACHE_KEY_REDLOCK = "ZZYY_REDLOCK";

    @Autowired
    RedissonClient redissonClient1;

    @Autowired
    RedissonClient redissonClient2;

    @Autowired
    RedissonClient redissonClient3;

    @GetMapping(value = "/redlock")
    public void getlock() {
        //CACHE_KEY_REDLOCK为redis 分布式锁的key
        RLock lock1 = redissonClient1.getLock(CACHE_KEY_REDLOCK);
        RLock lock2 = redissonClient2.getLock(CACHE_KEY_REDLOCK);
        RLock lock3 = redissonClient3.getLock(CACHE_KEY_REDLOCK);

        RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);
        boolean isLock;

        try {

            //waitTime 锁的等待时间处理,正常情况下 等5s
            //leaseTime就是redis key的过期时间,正常情况下等5分钟。
            isLock = redLock.tryLock(5, 300, TimeUnit.SECONDS);
            log.info("线程{}，是否拿到锁：{} ",Thread.currentThread().getName(),isLock);
            if (isLock) {
                //TODO if get lock success, do something;
                //暂停20秒钟线程
                try { TimeUnit.SECONDS.sleep(20); } catch (InterruptedException e) { e.printStackTrace(); }
            }
        } catch (Exception e) {
            log.error("redlock exception ",e);
        } finally {
            // 无论如何, 最后都要解锁
            redLock.unlock();
            System.out.println(Thread.currentThread().getName()+"\t"+"redLock.unlock()");
        }
    }
}
```

测试结果

![image-20220531214155801](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531214155801.png)

## 5、Redisson源码分析

单机看门狗demo

```java
public class WatchDogDemo
{
    public static final String LOCKKEY = "AAA";

    private static Config config;
    private static Redisson redisson;

    static {
        config = new Config();
        config.useSingleServer().setAddress("redis://"+"192.168.111.147"+":6379").setDatabase(0);
        redisson = (Redisson)Redisson.create(config);
    }

    public static void main(String[] args)
    {
        RLock redissonLock = redisson.getLock(LOCKKEY);

        redissonLock.lock();
        try
        {
            System.out.println("1111");
            //暂停几秒钟线程
            try { TimeUnit.SECONDS.sleep(25); } catch (InterruptedException e) { e.printStackTrace(); }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
           redissonLock.unlock();
        }

        System.out.println(Thread.currentThread().getName() + " main ------ ends.");

        //暂停几秒钟线程
        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
        redisson.shutdown();
    }
}
```

还记得之前说的缓存续命吗？Redis分布式锁过期了，但是业务逻辑还没处理完怎么办呢？

**守护线程续命**

额外起一个线程，定期检查线程是否还持有锁，如果有则延长过期时间。

**Redisson 里面就实现了这个方案，使用“看门狗”定期检查（每1/3的锁时间检查1次），如果线程还持有锁，则刷新过期时间；**

这个方案还有bug吗?

有的，分布式系统难免的，系统时钟影响

如果线程1从3个实例中获取到了锁，但是这3个实例中的某个实例的系统时间走的稍微快点，则它持有的锁会被提前释放，当它释放后，此时又有3个实例时空虚的，则线程2也可以获取锁，就出现了两个线程同时持有锁的情况了。

在获取锁成功后，给锁加一个watchdog，watchdog会起一个定时任务，在锁没有被释放且快要过期的时候会续期。

![image-20220531220042389](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531220042389.png)

> 缓存续命源码分析

看门狗的默认锁定时间

![image-20220531220615422](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531220615422.png)

通过Redisson新建出来的锁key，默认时30s

尝试加锁的的源码

![image-20220531220923600](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531220923600.png)

怎样自动续期的

![image-20220531221050225](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531221050225.png)

这里面初始化了一个定时器，dely 的时间是 internalLockLeaseTime/3。
在 Redisson 中，internalLockLeaseTime 是 30s，也就是每隔 10s 续期一次，每次 30s。

![image-20220531221116751](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531221116751.png)

watchdog自动延期机制

客户端A加锁成功，就会启动一个watch dog看门狗，他是一个后台线程，会每隔10秒检查一下，如果客户端A还持有锁key，那么就会不断的延长锁key的生存时间，默认每次续命又从30秒新开始

![image-20220531221318447](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531221318447.png)

redis分布式锁的可重入性保证

![image-20220531221446389](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531221446389.png)

![image-20220531221455696](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531221455696.png)

![image-20220531221503812](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531221503812.png)

流程解释：

通过exists判断，如果锁不存在，则设置值和过期时间，加锁成功

通过hexists判断，如果锁已经存在，并且锁的是当前线程，则证明是重入锁，加锁成功。

如果锁已经存在，但锁的不是当前线程，则证明有其他线程持有锁。返回当前锁的过期时间（代表lockzzyy这个锁key的剩余生存时间），加锁失败

加锁查看：

加锁成功后，在redis的内存数据中，就有一条hash结构的数据。

Key为锁的名称；field为随机字符串+线程ID；值为1。见下

![image-20220531222613721](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531222613721.png)

如果同一线程多次调用lock方法，值递增1。----------可重入锁见后

可重入锁查看：

![image-20220531222641082](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531222641082.png)

ttl续命的演示：

加大业务逻辑处理时间，看超过10s后，redisson的续命加时

解锁源码分析：

![image-20220531222737530](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531222737530.png)

常见异常情况

bug情况

![image-20220531222824968](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531222824968.png)

![image-20220531222836759](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220531222836759.png)

单机看门狗写法

```java
@RestController
public class GoodController
{
    public static final String REDIS_LOCK_KEY = "lockzzyy";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private Redisson redisson;

    @GetMapping("/buy_goods")
    public String buy_Goods() throws IOException
    {
        RLock redissonLock = redisson.getLock(REDIS_LOCK_KEY);
        redissonLock.lock();
        try
        {
            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);

            if(goodsNumber > 0)
            {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001",realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort);
                //暂停几秒钟线程
                try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件"+"\t 服务器端口："+serverPort;
            }else{
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort);
            }
            return "商品已经售罄/活动结束/调用超时，欢迎下次光临"+"\t 服务器端口："+serverPort;
        }finally {
            if(redissonLock.isLocked() && redissonLock.isHeldByCurrentThread())
            {
                redissonLock.unlock();
            }
        }
    }
}
```

