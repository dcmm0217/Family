package com.design.yes;

import com.alibaba.fastjson.JSON;
import com.design.CouponResult;
import com.design.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 发优惠券
 *
 * @author huangwei
 * @date 2023-06-01
 */
public class CouponCommodityService implements ICommodity {

    private Logger logger = LoggerFactory.getLogger(CouponCommodityService.class);

    private CouponService couponService = new CouponService();

    @Override
    public void sendCommodity(String uId, String commodityId, String bizId, Map<String, String> extMap) {
        CouponResult couponResult = couponService.sendCoupon(uId, commodityId, bizId);
        logger.info("请求参数[优惠券] => uId：{} commodityId：{} bizId：{} extMap：{}", uId, commodityId, bizId, JSON.toJSON(extMap));
        logger.info("测试结果[优惠券]：{}", JSON.toJSON(couponResult));
        if (!"0000".equals(couponResult.getCode())) {
            throw new RuntimeException(couponResult.getInfo());
        }
    }
}
