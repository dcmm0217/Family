# 订单履约系统-WMS（库存中心）

## 1、并发事物导致的写丢失

场景：oms->调用wms 同步配货单，生成出库单

描述：oms调用批量同步接口，发送消息到mq，oms自己去消费时，通过多线程去调用同步配货单的接口。调用同步配货单接口

问题：**==oms一下同步10个配货单，wms生成10个出库单，单据商品明细生成成功，库存流水生成正确。库存变更无故缺失，导致单据生成成功-库存没改对。==**

以下是ES中日志分析情况。

![image-20220908134100607](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220908134100607.png)



功能描述：

- 同步配货单接口添加了分布式锁

![image-20220908153119827](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220908153119827.png)

- 修改库存加了分布式锁

![image-20220908160932559](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220908160932559.png)

- 库存计算在内存中

![image-20220908153253554](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220908153253554.png)

情况过滤：

1、同一个配货单的创建操作，防止重复生成出库单。

2、不同配货单的创建操作，会继续执行下去，**关注单据商品明细**，如果存在`sku + whCode`是相同的值，此时会锁不住，**造成写丢失**



解决方案：

**1、数据库悲观锁，`select ...... for update` 在事物A未提交之前，阻塞事物B**

![image-20220908165705922](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/123)

**2、加大分布式锁的范围，直接将生成出库单变成串行操作**

![image-20220908170421095](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220908170421095.png)

**3、数据库乐观锁实现，新增字段version版本号** 

mybatis-plus使用乐观锁

![image-20220908184224058](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220908184224058.png)

![image-20220908184237400](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220908184237400.png)

![image-20220908184340163](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20220908184340163.png)

`update|updateById`必须要传主键，不然乐观锁修改不会生效。后续失败的单据再OMS进行重试。我们保证库存修改的正确性即可。

**后续思考和优化：**

OMS消息发送至mq，在WMS这边进行消费，用顺序消息进行消费，保证扣减一致？再回调OMS是否同步成功？



## 2、WMS的项目亮点

#### 1、redis分布式锁

#### 2、对外接口适配器工程

#### 3、使用设计模式（模板方法）

#### 4、线程池优化调用第三方接口、1-线程池 2-线程池 业务隔离 3-引入消息队列优化

#### 5、库存自动校正

#### 6、库存备份



## 3、库存校验/矫正

**前提：库存流水不能有错**

库存校验的核心逻辑：`根据单据状态，和刷库存的理想状态去反查当前单据的库存变化是否正确`;

入参可为多种筛选条件，可根据单号进行校验，根据时间区间进行校验等

校验步骤：

- 1、查出符合条件的所有单据

- 2、统计所有出单据的库存流水情况

- 3、根据各个单据状态去匹配当前单据统计的流水情况

  ```
  单据如果是 '状态'， 那么流水统计的结果一定是 '结果'
  待出库、ups带分拆包裹、待同步海外仓   +待发 -可售
  已出库  						  -在库 -可售  待发为0
  已取消、同步第三方失败作废           应没有库存变化
  ```

  如果单据库存变化符合上述改变，则说明库存变更正确。则不提出告警。

发现有库存修改不正确的场景，则生成矫正记录`stockOperationCorrectRecord`

更新矫正记录以后，==**可控**==的去执行（库存流水修复：重新生成库存流水记录，回补库存） + 库存修复（构造新的库存对象，然后去调用底层的通用刷库存的方法），做成开关的形式，会让库存更好管理

## 4、库存备份

通过Job来异步执行 `WmsStockHistoryJob`

job参数，支持按仓备份，不传参数，即默认全部仓库备份库存，执行频率为1天1次，备份昨天的库存数据

这样有利于我们核对当天的库存数据，有利于系统的稳定性。（不能出错，出错了后面备份的全是错的）

## 5、Redis分布式锁

1、采用的Redisson来作为分布式锁，做了一个工具的封装，不使用原生客户端接口

```java
@Slf4j
@Component
public class RedisLockUtil{
    
    @Autowired
    private RedissonClient redissonClient;
    
    // 分布式锁的前缀
    private static final String PREFIX = "wms:lock:";
    // 默认过期时间
    private static final long TIMEOUT_DEFAULT_MIS = 60000L;
    
    /**
     * nx分布式锁
     *
     * @param key       加锁key
     * @param leaseTime 加锁时间
     * @return 是否成功 true成功 false失败
     */
    public boolean nxLock(String key, long leaseTime) {
        key = PREFIX.concat(key);
        RLock lock = redissonClient.getLock(key);
        try {
            // 没有指定加锁时间,使用watch dog机制,默认加锁30s,且自动加时间 (该功能紧急下线，先不处理，默认加锁2分钟)
            if (leaseTime == 0) {
                return lock.tryLock(500, 2 * 60 * 1000L, TimeUnit.MILLISECONDS);
            } else {
                return lock.tryLock(500, leaseTime, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.error("分布式锁加锁失败,key:{},异常信息:{}", key, e);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 解锁-只有是当前线程加锁才进行解锁
     *
     * @param key key
     * @return 是否成功 true成功 false失败
     */
    public boolean delNxLock(String key) {

        key = PREFIX.concat(key);
        try {
            RLock lock = redissonClient.getLock(key);
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
                return true;
            }
            log.info(lock == null ? "锁已删除,不进行解锁key:{}" : "不是当前线程加锁,不进行解锁key:{}", key);
            return false;
        } catch (Exception e) {
            log.error("分布式锁解锁失败,key:{},异常信息:{}", key, e);
            return false;
        }
    }
    
    /**
     * 解锁(不检测是否当前线程加锁)
     *
     * @param key key
     * @return 是否成功 true成功 false失败
     */
    public boolean delNxLockNoThreadCheck(String key) {

        key = PREFIX.concat(key);
        try {
            RLock lock = redissonClient.getLock(key);
            if (lock != null) {
                lock.forceUnlock();
                return true;
            }
            log.info(lock == null ? "锁已删除,不进行解锁key:{}" : "不是当前线程加锁,不进行解锁key:{}", key);
            return true;
        } catch (Exception e) {
            log.error("分布式锁解锁失败,key:{},异常信息:{}", key, e);
            return false;
        }
    }
}



```

**在一些上下游交互中，我们会使用上游传递给我们的 唯一键 进行组合来做为分布式锁。**

作用：

- 防止重复推单 
- 保证下游系统幂等
- 防止并发请求导致的数据混乱

2、切面分布式锁

- 自定义分布式锁注解
- 定义切面完善逻辑

**分布式锁的注解**

```java
/**
 * 自定义分布式锁注解
 *
 * @author huangw
 * @since 2022-08-05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {

    /**
     * lock key
     */
    String key() default "";

    /**
     * 默认过期时间,指定为0时,启用watch dog机制,默认30s,剩余1/3时间自动增加时间
     */
    int expire() default 60 * 1000;

    /**
     * 生成锁类型,默认混合生成
     */
    RedisKeyGeneTypeEnum geneType() default RedisKeyGeneTypeEnum.KEY_USER_ID;

}
```

**切面加锁逻辑**

```java
/**
 * 注解分布式锁切面
 *
 * @author huangw
 * @since 2022-08-05
 */
@Slf4j
@Aspect
@Component
public class RedisLockAspect {

    @Resource
    private RedisLockUtil redisLockUtil;

    @Resource
    private BaseComponent baseComponent;

    @Around(value = "@annotation(redisLock)")
    public Object lock(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {

        String key = this.geneRedisKey(redisLock, joinPoint);
        // 加锁
        boolean lockFlag = redisLockUtil.nxLock(key, redisLock.expire());
        if (!lockFlag) {
            throw new OmsStockException("当前业务正在处理中,请稍后重试,业务键值:" + key);
        }

        try {
            return joinPoint.proceed();
        } finally {
            boolean unLockFlag = redisLockUtil.delNxLock(key);
            if (!unLockFlag) {
                log.error("解锁失败,业务键值:" + key);
            }
        }
    }

    /**
     * 生成加锁的key值
     *
     * @param redisLock 锁设置
     * @return key
     */
    private String geneRedisKey(RedisLock redisLock, ProceedingJoinPoint joinPoint) {

        RedisKeyGeneTypeEnum redisKeyGeneTypeEnum = redisLock.geneType();
        if (redisKeyGeneTypeEnum == null) {
            throw new OmsStockException("锁设置异常!");
        }
        String key = redisLock.key();
        Long userSsoId = null;
        switch (redisKeyGeneTypeEnum) {
            // 指定key生成
            case KEY:
                if (StringUtils.isBlank(key)) {
                    throw new OmsStockException("锁设置异常!");
                }
                return this.isEl(key) ? this.getByEl(key, joinPoint) : key;
            // 根据登录用户id生成
            case USER_ID:
                userSsoId = baseComponent.getUserSsoId();
                if (userSsoId == null) {
                    throw new OmsStockException("锁设置异常!");
                }
                return userSsoId.toString();
            case KEY_USER_ID:
                if (StringUtils.isBlank(key)) {
                    throw new OmsStockException("锁设置异常!");
                }
                userSsoId = baseComponent.getUserSsoId();
                if (userSsoId == null) {
                    throw new OmsStockException("锁设置异常!");
                }
                return this.isEl(key) ? this.getByEl(key, joinPoint) : key + userSsoId;
            default:
                throw new OmsStockException("锁设置异常!");
        }
    }

    /**
     * 解析 SpEL 表达式并返回其值
     */
    private String getByEl(String el, ProceedingJoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String[] paramNames = getParameterNames(method);
        Object[] arguments = point.getArgs();

        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(el);
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < arguments.length; i++) {
            context.setVariable(paramNames[i], arguments[i]);
        }

        return expression.getValue(context, String.class);
    }

    /**
     * 获取方法参数名列表
     */
    private String[] getParameterNames(Method method) {
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        return u.getParameterNames(method);
    }

    private boolean isEl(String str) {
        return str.contains("#");
    }

}
```

切面分布式锁生成key的策略

```java
/**
 * redis key生成策略
 *
 * @auther huangw
 * @since 2022-08-10 15:40
 */
@Getter
public enum RedisKeyGeneTypeEnum {

    /**
     * 根据指定key生成
     */
    KEY,

    /**
     * 根据登录用户id生成
     */
    USER_ID,

    /**
     * 混合生成 key + user_id
     */
    KEY_USER_ID;
}
```

## 6、多线程异步优化第三方接口调用





## 7、项目设计模式的使用



## 8、适配器项目作用
