package com.design.yes;

/**
 * 发奖规则
 *
 * @author huangwei
 * @date 2023-06-01
 */
public class StoreFactory {

    /**
     * 发奖处理规则
     *
     * @param commodityType
     * @return
     */
    public ICommodity getCommodityService(Integer commodityType) {
        if (null == commodityType) {
            return null;
        }
        if (1 == commodityType) {
            return new CouponCommodityService();
        }
        if (2 == commodityType) {
            return new GoodsCommodityService();
        }
        if (3 == commodityType) {
            return new CardCommodityService();
        }
        throw new RuntimeException("不存在的商品服务类型");
    }
}
