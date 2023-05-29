package com.design.no;

/**
 * 摇号服务接口
 *
 * @author huangwei
 * @date 2023-05-29
 */
public interface LotteryService {

    /**
     * 摇号接口
     *
     * @param uId
     * @return
     */
    LotteryResult doDraw(String uId);
}
