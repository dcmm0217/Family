package com.design.yes.event.listener;

import com.design.no.LotteryResult;

/**
 * 事件监听器
 *
 * @author huangwei
 * @date 2023-05-29
 */
public interface EventListener {

    /**
     * 事件抽象接口
     * @param result
     */
    void doEvent(LotteryResult result);
}
