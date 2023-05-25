package com.design.yes.impl;

import com.design.yes.ICouponDiscount;

import java.math.BigDecimal;

/**
 * 直减优惠
 *
 * @author huangwei
 * @date 2023-05-25
 */
public class ZJCouponDiscount implements ICouponDiscount<Double> {

    @Override
    public BigDecimal discountAmount(Double couponInfo, BigDecimal skuPrice) {
        BigDecimal discountAmount = skuPrice.subtract(new BigDecimal(couponInfo));
        if (discountAmount.compareTo(BigDecimal.ZERO) < 1) {
            return BigDecimal.ONE;
        }
        return discountAmount;
    }
}
