package com.design.no;

/**
 * 小客车指标调控服务
 *
 * @author huangwei
 * @date 2023-05-29
 */
public class MinibusTargetService {

    /**
     * 模拟上牌资格摇号
     *
     * @param uId
     * @return
     */
    public String lottery(String uId) {
        return Math.abs(uId.hashCode()) % 2 == 0 ? "恭喜你，编码".concat(uId).concat("在本次摇号中") : "很遗憾，编码".concat(uId).concat("在本次摇号未中签或摇号资格已过期");
    }
}
