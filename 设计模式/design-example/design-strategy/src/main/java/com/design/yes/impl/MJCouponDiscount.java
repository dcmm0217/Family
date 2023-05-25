package com.design.yes.impl;

import com.design.yes.ICouponDiscount;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 满减计算
 *
 * @author huangwei
 * @date 2023-05-25
 */
public class MJCouponDiscount implements ICouponDiscount<Map<String, String>> {


    @Override
    public BigDecimal discountAmount(Map<String, String> couponInfo, BigDecimal skuPrice) {
        String x = couponInfo.get("x");
        String o = couponInfo.get("n");

        // 小于商品金额条件的，直接返回商品原价
        if (skuPrice.compareTo(new BigDecimal(x)) < 0) {
            return skuPrice;
        }
        // 减去优惠金额判断
        BigDecimal discountAmount = skuPrice.subtract(new BigDecimal(o));
        if (discountAmount.compareTo(BigDecimal.ZERO) < 1) {
            return BigDecimal.ONE;
        }
        return discountAmount;
    }
}
