# 请你谈谈Redis分布锁的理解

## 1、什么是分布式锁？

1、**分布式锁**：分布式锁就是分布式场景下的锁，比如多台不同机器上的进程，去竞争同一项资源，就是分布式锁。

**作用：**当多个进程不在同一个系统中，用分布式锁控制多个进程对资源的访问

如果是单机情况下，单JVM，线程之间共享内存，只要使用线程锁就可以解决并发问题。

如果是分布式情况下，多JVM，，线程A和线程B很可能不是在同一JVM中，这样线程锁就无法起到作用了，这时候就要用到分布式锁来解决。

## 2、分布式锁的特性

2、**分布式锁应该有的特性：**

- 互斥性：锁的目的就是获取资源的使用权，所以只能让一个竞争者去持有锁，这一点要尽可能保证。
- 安全性：要避免死锁情况的发生。当一个竞争者在持有锁期间，由于意外崩溃而导致未能主动解锁，其持有的锁也能够被正常释放，并保证后续其他竞争者也能够继续加锁。
- 对称性（可重入性）：同一把锁，加锁和解锁的对象必须是同一竞争者，不能把其他竞争者持有的锁给释放了。
- 可靠性：需要有一定的异常处理能力，容灾能力。



## 3、分布式锁的实现方式

3、**分布式锁的实现方式：**

分布式锁，一般会依托第三方组件来实现，而利用**Redis**实现则是工作中应用最多的一种。

我们从最基础的步骤开始，依照分布式锁的特性，层层递进，步步完善，将它优化到最优，完整地了解如何用**Redis**来实现一个分布式锁

#### ==最简化版本==

直接用Redis的setnx命令（**setnx key value**）

**结果：**如果key不存在，则会将key设置为value，并返回1；如果key存在，不会有任务影响，返回0

基于这个特性，我们就可以用setnx实现加锁的目的：通过setnx加锁，加锁之后其他服务无法加锁，用完之后，再通过delete解锁，深藏功与名。

![image-20220224175156082](https://gitee.com/huangwei0123/image/raw/master/img/image-20220224175156082.png)

#### ==支持过期时间的版本==

最简化版本有一个问题：

如果获取锁的服务挂掉了，那么锁就一直得不到释放，就像石沉大海，杳无音信。所以，我们需要一个超时来兜底

Redis中有expire命令，用来设置一个key的超时时间。**但是setnx和expire不具备原子性**，如果setnx获取锁之后，服务挂掉，依旧是泥牛入海

很自然，我们会想到，set和expire，有没有原子操作？

当然有，Redis早就考虑到了这种场景，推出了如下执行语句： **set key value nx ex seconds**

nx表示具备setnx特定，ex表示增加了过期时间，最后一个参数就是过期时间的值。

![image-20220224175324186](https://gitee.com/huangwei0123/image/raw/master/img/image-20220224175324186.png)

能够支持过期时间，目前这个锁基本上是能用了

**但是存在一个问题：会存在服务A释放掉服务B的锁的可能**

#### ==**加上owner**==（自主标记）

试想下场景：

服务A获取了锁，由于业务流程比较长，或者网络延迟、GC卡顿等原因，导致锁过期，而业务还会继续进行。

这时候，业务B已经拿到了锁，准备去执行，这个时候服务A恢复过来并做完了业务，就会释放锁，而B却还在继续执行。

在真实的分布式场景中，可能存在几十个竞争者，那么上述情况发生的概率就很高，导致同一份资源频繁被不同竞争者访问，分布式锁也失去了意义。

基于这个场景，我们可以发现，问题关键在于，竞争者可以释放其他人的锁。那么在异常情况下，就会出现问题，所以我们需要进一步给出解决方案：

==分布式锁需要满足谁申请，谁释放的原则，不能释放别人的锁，也就是锁，分布式锁，是要有归属的。==

![image-20220224214414090](https://gitee.com/huangwei0123/image/raw/master/img/image-20220224214414090.png)

#### ==引入Lua==

加入owner后的版本可以称得上是完善了吗？还有没有什么隐患呢？

其实还是存在一个问题的，完整的流程是竞争者获取锁执行任务，执行完毕后检查锁是不是自己的，最后进行释放。

如果在执行完毕，检查锁，再释放，这些操作不是原子化的。

可能锁获取时还是自己的，删除时却已经是别人的了。这怎么办呢？

Redis没有提供这种场景的一个原子化操作，那怎么办呢？

我们可以采用Lua脚本来保证原子性，**Redis + Lua** 可以说是专门为解决原子性问题而生。

有了Lua的特性，Redis才是真正的在分布式锁、秒杀等场景，有了用武之地，下面是改造后的流程：

![image-20220224220844971](https://gitee.com/huangwei0123/image/raw/master/img/image-20220224220844971.png)

到了这一步，分布式锁的前3个特性：对称性、安全性、可靠性，就满足了。

可以说是一个可以使用的分布式锁了，能够满足绝大多数的场景需要。

#### ==那如何保证可靠性呢？==

针对一些异常场景，包括Redis挂掉了、业务执行时间过长、网络波动等情况，我们来一起分析如何处理。

**容灾考虑**：前面我们提到的，基本是基于单机考虑的，如果Redis挂掉了，那锁就不能获取了。这个问题该如何解决呢？

一般来说有两种方法：**主从容灾**和**多级部署**。

**主从容灾：**

最简单的一种方式，就是为Redis配置从节点，当主节点挂了，用从节点顶包。

![image-20220224221159221](https://gitee.com/huangwei0123/image/raw/master/img/image-20220224221159221.png)

但是需要主从切换，需要人工参与，会提高人力劳动成本。不过Redis已经有成熟的解决方案了，也就是**Redis哨兵模式（Sentinel）**，可以灵活自动切换，不再需要人工介入。

![image-20220224221329509](https://gitee.com/huangwei0123/image/raw/master/img/image-20220224221329509.png)

通过增加从节点的方式，虽然一定程度上解决了单点容灾的问题，但并不是尽善尽美的，由于同步有延时，Slave可能会损失掉部分数据，分布式锁可能失效，这就会发生短暂的多机获取到执行权限。

有没有更可靠的办法呢？

**多机部署：**

如果对一致性的要求高一些，可以尝试多机部署，比如Redis的RedLock（红锁），大概思路就是多个机器，通常是奇数个（为什么是奇数个？），达到一半以上同意加锁才算加锁成功，这样，可靠性会向ETCD靠近。

现在假设有5个Redis主节点，基本保证它们不会同时宕机，获取锁和释放锁的过程中，客户端会执行以下操作。

1、向5个Redis申请加锁

2、只要超过一半，也就是3个Redis返回成功，那么就是获取到了锁。如果超过一半失败，需要向每个Redis发生解锁命令

3、由于向5个Redis发送请求，会有一定时耗，所以锁剩余持有时间，需要减去请求时间。这个可以作为判断以据，**如果剩余时间已经为0，那么也是获取锁失败。**

4、使用完成以后，向5个Redis发送解锁请求。

![image-20220224222157128](https://gitee.com/huangwei0123/image/raw/master/img/image-20220224222157128.png)

这种模式的好处在于，如果挂了2台Redis，整个集群还是可以使用的，给了运维更多时间来修复。

另外，单点Redis的所有手段，这种多机配置都可以使用，比如为每个节点配置哨兵模式，由于加锁是一半以上同意就成功，那么如果单个节点进行了主从切换，单个节点数据的丢失，就不会让锁失效了。这样增强了可靠性。

**可靠性深究**

**什么是RedLock：**

==**多节点redis实现的分布式锁算法**==

**RedLock作用：有效防止单点故障**

多个服务间保证同一时刻同一时间段内同一用户只能有一个请求（防止关键业务出现并发攻击，也就是分布式锁的作用）

是不是有RedLock，就一定能保证可靠得到分布式锁？

**由于分布式系统的三大困境（简称为NPC），所以没有完全可靠的分布式锁！**

让我们来看看RedLock在NPC下的表现

==N：NetWork Deplay（网络延迟）==

当分布式锁获得返回包的时间过长，此时可能虽然加锁成功，但是已经时过境迁，锁很快就会过期。Red Lock算了做了些考量，也就是前面说的**锁剩余持有时间，需要减去请求时间**，如此一来，就可以一定程度解决网络延迟的问题。

==P：Process Pause（进程暂停）==

比如发送GC，获取锁之后GC了，出与GC执行中，然后锁超时。

![image-20220224222954970](https://gitee.com/huangwei0123/image/raw/master/img/image-20220224222954970.png)

其他锁获取，这种情况几乎无解。这时候GC回来了，那么两个进程就获取到了同一个分布式锁。

![image-20220224223052998](https://gitee.com/huangwei0123/image/raw/master/img/image-20220224223052998.png)

也许你会说，在GC回来之后，可以再去查一次啊？

这里有两个问题，首先你怎么知道GC回来了？这个可以在做业务之前，通过时间，进行一个粗略判断，但也是很吃场景经验的；第二，如果你判断的时候是ok的，但是判断完GC了呢？**这点RedLock是无法解决的。**

C：Clock Drift（时钟漂移）

如果竞争者A，获得了RedLock，在5台分布式机器上都加上了锁。为了方便分析，我们直接假设5台机器都发生了时钟漂移，锁瞬间过期了。这时候竞争者B拿到了锁，此时A和B拿到了相同的执行权限。

根据上述的分析，可以看出，RedLock也不能扛住NPC的挑战，因此，单单从分布式锁本身出发，完全可靠是不可能的。要实现一个相对可靠的分布式锁机制，还是需要和业务的配合，业务本身要幂等可重入，这样的设计可以省却很多麻烦。



**最后，我觉得分布式锁需要于业务联动配合更加切实才行，脱离了业务，就是空中阁楼，不着实地。**



## 4、实战如何使用分布式锁

#### 1、自己封装的Redis锁

Lock接口

```java
public interface Lock {
    /**
     * 锁，保证并发的时同一个业务同时只能有一个会通过该栅栏。防重防并发
     *
     * @param key 需要拦截的KEY，随意指定
     * @param value    拦截的业务的值，随意指定
     * @param timeOut   当前KEY的超时时间 （这个时间很重要，太短可能会因为长事务没有被提交，导致栅栏失效，所以需要平衡自己的具体业务的自行时间，合理的设置超时时间 )
     * @return Boolean
     */
    public boolean lock(String key, String value, Integer timeOut);

    /**
     * 解锁
     * @param key 锁的名称
     */
    public void unlock(String key);

    /**
     * 解锁 - 防止长事务情况下，解错锁
     * @param key 锁的名称
     * @param value 保存的值
     */
    public void unlock(String key,String value);
}
```

RedisLock

```java
public class RedisLock implements Lock {

    private Logger logger = LoggerFactory.getLogger(RedisLock.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 分布式锁
     *
     * @param key      需要拦截的KEY，按规则指定
     * @param value    拦截的业务的值，指定为1 或其他值
     * @param timeOut   当前KEY的超时时间 （这个时间很重要，太短可能会因为长事务没有被提交，导致栅栏失效，所以需要平衡自己的具体业务的自行时间，合理的设置超时时间 )
     * @return Boolean
     */
    @Override
    public boolean lock(String key, String value, Integer timeOut) {
        if (timeOut == null || timeOut == 0 || StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            throw new BusinessException("获取锁参数非法");
        }
        final String luaScript = " "
                + " local result = tonumber(redis.call('SETNX', KEYS[1],ARGV[1]));"
                + " if result==0 then "
                + " return result;"
                + " end "
                + " redis.call('EXPIRE',KEYS[1],ARGV[2]);" + " return result";
        final List<String> keys = putList("lock:" + key);
        DefaultRedisScript<Long> getRedisScript = new DefaultRedisScript<>();
        getRedisScript.setResultType(Long.class);
        getRedisScript.setScriptText(luaScript);
        Long result =  redisTemplate.execute(getRedisScript, keys, value, timeOut);
        if (result == null || result == 0) {
            logger.info("redis result is {}", false);
            return false;
        }
        logger.info("redis result is {}", true);
        return true;
    }

    @Override
    public void unlock(String key) {
        redisTemplate.delete("lock:"+key);
    }

    @Override
    public void unlock(String key, String value) {
        //TODO 从redis中获取值，然后对比 再决定是否删除
        redisTemplate.delete("lock:"+key);
    }
}
```

#### 2、使用Redisson

```java
public class LockExamples {
    public static void main(String[] args) throws InterruptedException {
        // 默认连接上127.0.0.1:6379
        RedissonClient client = Redisson.create();
        //RLock 继承了 java.util.concurrent.locks.Lock 接口
        RLock lock = client.getLock("lock");
        lock.lock();
        System.out.println("lock acquired");
        Thread t = new Thread(() -> {
            RLock lock1 = client.getLock("lock");
            lock1.lock();
            System.out.println("lock acquired by thread");
            lock1.unlock();
            System.out.println("lock released by thread");
        });
        t.start();
        t.join(1000);
        lock.unlock();
        System.out.println("lock released");
        t.join();
        client.shutdown();
    }
}
```

![image-20220224225611684](https://gitee.com/huangwei0123/image/raw/master/img/image-20220224225611684.png)

