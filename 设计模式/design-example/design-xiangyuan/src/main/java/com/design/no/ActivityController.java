package com.design.no;

import java.util.Date;

/**
 * 活动控制层
 *
 * @author huangwei
 * @date 2023-05-22
 */
public class ActivityController {

    /**
     * 这⾥模拟的是从接⼝中查询活动信息，基本也就是从数据库中获取所有的商品信息和库存。有点像
     * 最开始写的商品销售系统，数据库就可以抗住购物量
     *
     * 当后续因为业务的发展需要扩展代码将库存部分交给redis处理，那么就需要从redis中获取活动的
     * 库存，⽽不是从库中，否则将造成数据不统⼀的问题
     *
     *
     * @param id
     * @return
     */
    public Activity queryActivityInfo(Long id) {
        // 模拟从实际业务应用从接口中获取活动信息
        Activity activity = new Activity();
        activity.setId(10001L);
        activity.setName("图书嗨乐");
        activity.setDesc("图书优惠券分享激励分享活动第二期");
        activity.setStartTime(new Date());
        activity.setStopTime(new Date());
        activity.setStock(new Stock(1000, 1));
        return activity;
    }
}
