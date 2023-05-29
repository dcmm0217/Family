package com.design.no;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 小客车指标调控服务
 *
 * @author huangwei
 * @date 2023-05-29
 */
public class LotteryServiceImpl implements LotteryService {

    private final Logger logger = LoggerFactory.getLogger(LotteryServiceImpl.class);

    MinibusTargetService minibusTargetService = new MinibusTargetService();

    @Override
    public LotteryResult doDraw(String uId) {
        String lottery = minibusTargetService.lottery(uId);
        // 发短信
        logger.info("给用户 {} 发送短信通知(短信)：{}", uId, lottery);
        // 发MQ消息
        logger.info("记录用户 {} 摇号结果(MQ)：{}", uId, lottery);

        return new LotteryResult(uId, lottery, new Date());
    }
}
