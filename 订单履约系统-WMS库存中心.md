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

