package com.design.yes.event.listener;

import com.design.no.LotteryResult;
import lombok.extern.slf4j.Slf4j;

/**
 * 短信发送事件
 *
 * @author huangwei
 * @date 2023-05-29
 */
@Slf4j
public class MessageEventListener implements EventListener {
    @Override
    public void doEvent(LotteryResult result) {
        log.info("给⽤户 {} 发送短信通知(短信)：{}", result.getuId(), result.getMsg());
    }
}
