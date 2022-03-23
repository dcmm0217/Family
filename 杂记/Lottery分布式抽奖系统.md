# Lottery分布式抽奖系统

#### 1、@Mapper 和 @Repository的区别

这两种注解的区别在于： 

1.使用@mapper后，不需要在spring配置中设置扫描地址，通过mapper.xml里面的namespace属性对应相关的mapper类，spring将动态的生成Bean后注入到ServiceImpl中。 

 2.@repository则需要在Spring中配置扫描包地址，然后生成dao层的bean，之后被注入到ServiceImpl中。



#### 2、Dubbo调用不成功

在生产上必须配合注册中心来使用，我这里使用广播模式。（或者直连模式）

错误一直生产者拒绝链接，我竟然没想到是生产者的问题。

发现是生成者没有开启Dubbo服务发现

```java
@SpringBootApplication
@EnableDubbo
public class LotteryInterfacesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryInterfacesApplication.class, args);
    }
}
```

在启动类中要开启@EnableDubbo

![image-20220323223900319](https://gitee.com/huangwei0123/image/raw/master/img/image-20220323223900319.png)

消费者

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {
    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Reference(interfaceClass = IActivityBooth.class,url = "dubbo://127.0.0.1:20880")
    private IActivityBooth activityBooth;

    @Test
    public void testRpc(){
        ActivityReq activityReq = new ActivityReq(100001L);
        ActivityRes res = activityBooth.queryActivityById(activityReq);
        logger.info("测试结果：{}", JSON.toJSON(res));
    }
}
```

![image-20220323224040835](https://gitee.com/huangwei0123/image/raw/master/img/image-20220323224040835.png)