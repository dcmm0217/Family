## 记一次修改公司架构师封装的jar包

#### 1、问题提出

测试提出bug单

```
票据智能导入，短时间内上传会记忆上次传输的文件，如首次上传存在错误数据，修改后再次上传，上次的错误数据依然显示
```

提单给票据业务开发人员，杰哥。

杰哥排查

```
校验成功
successList:[AbBillSmartImportVo .......]
errorList:[]
校验失败
errorList:[AbBillSmartImportVo .......]
successList:[]
确定校验逻辑无误，猜测应是基类读到了重复缓存，建议给中台架构部阿威排查。
```

效果：

第一次导入成功

![image-20211030165424484](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20211030165424484.png)

第二次导入失败

![image-20211030165545062](https://mygiteepic.oss-cn-shenzhen.aliyuncs.com/img/image-20211030165545062.png)

第三次导入成功，错误信息依然存在

#### 2、排查问题

1、根据定位产生问题的微服务

````
url:xxxxxx:/api/bill/accebill/dynamicImport/dynamicImport
````

2、找到校验的接口，并执行跑业务流程，

发现校验成功，errorList的确为空，业务校验方法问题排除。

提出疑问：可能底层封装的jar包缓存有问题

```java
/**
     * 智能导入分页预览
     *
     * @param request  请求对象
     * @param response 响应对象
     */
@ApiOperation(value = "单位导入分页预览", notes = "单位导入分页预览")
@PostMapping("/dynamicImport")
public void dynamicImport(HttpServletRequest request, HttpServletResponse response) {
    dynamicImportService.dynamicImport(request, response, AbBillSmartImportVo.class, abBillDynamicImportService);
}
```

3、查看应用调用的jar包，并打入断点debug，尝试复现问题，并判断逻辑代码执行顺序

```
//1、导入校验的统一入口
public <K extends BaseExcelDto> void dynamicImport()

//2、执行票据导入校验方法
public ExcelImportResult<AbBillSmartImportVo> validate()

//3、Excel导入导出接口实现类
public <T extends BaseExcelDto> void cacheImportData()

//4、显示提示信息直接读取缓存
List<?> list = this.redisCacheService.getPreviewData(request);
```

4、发现问题

```
1、导入数据正确，生成缓存，AbBillSmartImportVo:006f101770ee4999a90ee535c64aa503:success

2、导入数据错误，生成缓存，AbBillSmartImportVo:006f101770ee4999a90ee535c64aa503:error

3、再次导入整确数据，错误信息还在在，并发现错误缓存也还在，正确缓存被更新有效期。

判断缓存的问题，开始想如何修改。仔细分析封装的源码
```

5、修改代码

```java
 @Override
    public <T extends BaseExcelDto> void cacheImportData(HttpServletRequest request, IUser user,
                                                         String importType, ExcelPreview<T> excelPreview) {
        // 获取redis缓存过期时间（秒）
        String expireTime = request.getParameter("expireTime");
        Long expireTimeToLive = StringUtils.hasText(expireTime) ? Long.parseLong(expireTime) : null;

        // 组装缓存key值
        String key = user.getTenantId() + ":" + excelPreview.getClassName() + ":" + user.getCurrentUserId();
        excelPreview.setCacheId(key);
        // 将成功的数据进行redis分页缓存处理
        if (!CollectionUtils.isEmpty(excelPreview.getSuccessList())) {
            String successKey = key + ":success";
            addRedisSortedSet(successKey, excelPreview.getSuccessList(), expireTimeToLive);
            excelPreview.setSuccessTotal(excelPreview.getSuccessList().size());
        } else {
            excelPreview.setSuccessTotal(0);
            //addRedisSortedSet(key + ":success", excelPreview.getSuccessList(), expireTimeToLive);
        }
        // 将失败的数据进行redis分页缓存处理
        if (!CollectionUtils.isEmpty(excelPreview.getErrorList())) {
            String errorKey = key + ":error";
            addRedisSortedSet(errorKey, excelPreview.getErrorList(), expireTimeToLive);
            excelPreview.setErrorTotal(excelPreview.getErrorList().size());
        } else {
            excelPreview.setErrorTotal(0);
            //addRedisSortedSet(key + ":error", excelPreview.getSuccessList(), expireTimeToLive);
        }
        
         @Override
    public <T extends BaseExcelDto> void addRedisSortedSet(@NotNull String key, List<T> list, Long expireTime) {
        // 从redis获取
        RScoredSortedSet<Object> scoredSortedSet = redissonClient.getScoredSortedSet(getCacheId(key));
        // 存在先删除
        if (!scoredSortedSet.isEmpty()) {
            scoredSortedSet.delete();
        }
        // 重新组装
        Map<Object, Double> objects = Maps.newHashMap();
        list.forEach(ConsumerUtils.consumerWithIndex((item, index) -> objects.put(item, Double.valueOf(index))));
        // 批量将数据添加到redis里面
        scoredSortedSet.addAll(objects);
        // 设置过期日期，默认10分钟清除导入缓存
        long expireTimeToLive = null == expireTime ? EXPIRE_TIME : expireTime;
        scoredSortedSet.expire(expireTimeToLive, TimeUnit.SECONDS);
    }
```

发现导入校验成功时只更新了成功的缓存，如果失败的缓存还存在的话，没有去删除，而且查看失败数据页面还能查到。

```java
 // 将成功的数据进行redis分页缓存处理
        if (!CollectionUtils.isEmpty(excelPreview.getSuccessList())) {
            String successKey = key + ":success";
            addRedisSortedSet(successKey, excelPreview.getSuccessList(), expireTimeToLive);
            excelPreview.setSuccessTotal(excelPreview.getSuccessList().size());
        } else {
            excelPreview.setSuccessTotal(0);
            addRedisSortedSet(key + ":success", excelPreview.getSuccessList(), expireTimeToLive);
        }
        // 将失败的数据进行redis分页缓存处理
        if (!CollectionUtils.isEmpty(excelPreview.getErrorList())) {
            String errorKey = key + ":error";
            addRedisSortedSet(errorKey, excelPreview.getErrorList(), expireTimeToLive);
            excelPreview.setErrorTotal(excelPreview.getErrorList().size());
        } else {
            excelPreview.setErrorTotal(0);
            addRedisSortedSet(key + ":error", excelPreview.getSuccessList(), expireTimeToLive);
        }
```

解决方案：

导入数据成功时，删除失败的缓存

导入失败时，删除成功的缓存



#### 3、测试

搞定

#### 4、提交代码