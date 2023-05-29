package com.design.yes;

import com.design.no.LotteryResult;
import com.design.no.MinibusTargetService;

import java.util.Date;

/**
 * 小客车指标调控实现类
 *
 * @author huangwei
 * @date 2023-05-29
 */
public class LotteryServiceImpl extends LotteryService {

    private MinibusTargetService minibusTargetService = new MinibusTargetService();


    @Override
    protected LotteryResult doDraw(String uId) {
        String lottery = minibusTargetService.lottery(uId);
        return new LotteryResult(uId, lottery, new Date());
    }
}
