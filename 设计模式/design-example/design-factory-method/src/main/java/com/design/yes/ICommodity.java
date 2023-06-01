package com.design.yes;

import java.util.Map;

/**
 * 活动发奖接口
 *
 * @author huangwei
 * @date 2023-06-01
 */
public interface ICommodity {

    /**
     * 抽奖行为
     *
     * @param uId
     * @param commodityId
     * @param bizId
     * @param extMap
     */
    void sendCommodity(String uId, String commodityId, String bizId, Map<String, String> extMap);
}
