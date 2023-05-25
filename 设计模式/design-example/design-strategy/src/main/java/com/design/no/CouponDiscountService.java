package com.design.no;

/**
 * 优惠券折扣计算接口
 *
 * @author huangwei
 * @date 2023-05-25
 */
public class CouponDiscountService {

    /**
     * 计算商品折扣
     *
     * @param type        类型
     * @param typeContent 折扣率
     * @param skuPrice    单价
     * @param typeExt     满减金额
     * @return 折扣后价格
     * <p>
     * * 优惠券类型；
     * * 1. 直减券
     * * 2. 满减券
     * * 3. 折扣券
     * * 4. n元购
     */
    public double discountAmount(int type, double typeContent, double skuPrice, double typeExt) {
        // 1.直减券
        if (1 == type) {
            return skuPrice - typeContent;
        }
        // 2.满减券
        if (2 == type) {
            if (skuPrice < typeExt) {
                return skuPrice;
            }
            return skuPrice - typeExt;
        }
        // 3.折扣券
        if (3 == type) {
            return skuPrice * typeContent;
        }
        // 4. n元购
        if (4 == type) {
            return typeContent;
        }
        return skuPrice;
    }
}
