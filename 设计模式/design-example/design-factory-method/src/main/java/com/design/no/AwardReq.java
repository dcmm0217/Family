package com.design.no;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 抽象奖品请求入参
 *
 * @author huangwei
 * @date 2023-06-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AwardReq {

    /**
     * 用户唯一id
     */
    private String uId;

    /**
     * 奖品类型(可以用枚举定义)；1优惠券、2实物商品、3第三方兑换卡(爱奇艺)
     */
    private Integer awardType;

    /**
     * 奖品编号；sku、couponNumber、cardId
     */
    private String awardNumber;

    /**
     * 业务ID，防重复
     */
    private String bizId;

    /**
     * 扩展信息
     */
    private Map<String, String> extMap;
}
