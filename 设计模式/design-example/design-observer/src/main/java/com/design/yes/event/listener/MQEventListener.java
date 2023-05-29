package com.design.yes.event.listener;

import com.design.no.LotteryResult;
import lombok.extern.slf4j.Slf4j;

/**
 * MQ发送事件
 *
 * @author huangwei
 * @date 2023-05-29
 */
@Slf4j
public class MQEventListener implements EventListener {
    @Override
    public void doEvent(LotteryResult result) {
        log.info("记录用户 {} 摇号结果(MQ)：{}", result.getuId(), result.getMsg());
    }
}
