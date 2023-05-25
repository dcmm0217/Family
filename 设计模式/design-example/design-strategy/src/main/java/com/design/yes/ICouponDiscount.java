package com.design.yes;

import java.math.BigDecimal;

/**
 * 优惠券折扣计算接口
 *
 * @author huangwei
 * @date 2023-05-25
 */
public interface ICouponDiscount<T> {

    /**
     * 优惠券金额计算
     *
     * @param couponInfo 折扣券信息：直减、满减、折扣、N元购
     * @param skuPrice   单价
     * @return 优惠后金额
     */
    BigDecimal discountAmount(T couponInfo, BigDecimal skuPrice);
}
