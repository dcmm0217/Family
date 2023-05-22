package com.design.yes;

import com.design.no.Activity;
import com.design.no.Stock;

/**
 * 活动控制层
 *
 * @author huangwei
 * @date 2023-05-22
 */
public class ActivityController {

    private RedisUtils redisUtils = new RedisUtils();


    /**
     * 享元模式⼀般情况下使⽤此结构在平时的开发中并不太多，除了⼀些线程池、数据库连接池外，再就是
     * * 游戏场景下的场景渲染。另外这个设计的模式思想是减少内存的使⽤提升效率，与我们之前使⽤的原型
     * * 模式通过克隆对象的⽅式⽣成复杂对象，减少rpc的调⽤，都是此类思想。
     * <p>
     * 感觉类似redis，其实完全可以用redis替代享元，用了jvm内存存储了不常变更的对象的部分属性，再对经常变更的属性进行一个处理
     * 更优解 感觉使用redis会更加合理，引入第三方缓存。
     *
     * @param id
     * @return
     */
    public Activity queryActivityInfo(Long id) {
        Activity activity = ActivityFactory.getActivity(id);
        // 模拟从Redis中获取库存变化信息
        Stock stock = new Stock(1000, redisUtils.getStockUsed());
        activity.setStock(stock);
        return activity;
    }
}
