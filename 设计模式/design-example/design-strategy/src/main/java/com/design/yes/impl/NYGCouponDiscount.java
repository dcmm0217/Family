package com.design.yes.impl;

import com.design.yes.ICouponDiscount;

import java.math.BigDecimal;
import java.util.Map;

/**
 * N元购策略
 *
 * @author huangwei
 * @date 2023-05-25
 */
public class NYGCouponDiscount implements ICouponDiscount<Double> {

    /**
     * 无论原价多少钱，都直接N元购买
     *
     * @param couponInfo 折扣券信息：直减、满减、折扣、N元购
     * @param skuPrice   单价
     * @return 折扣后价格
     */
    @Override
    public BigDecimal discountAmount(Double couponInfo, BigDecimal skuPrice) {
        return new BigDecimal(couponInfo);
    }
}
