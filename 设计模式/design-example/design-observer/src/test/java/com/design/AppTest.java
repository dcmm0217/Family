package com.design;

import com.alibaba.fastjson.JSON;
import com.design.no.LotteryResult;
import com.design.no.LotteryService;
import com.design.no.LotteryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class AppTest {

    @Test
    public void test() {
        LotteryService lotteryService = new LotteryServiceImpl();
        LotteryResult lotteryResult = lotteryService.doDraw("123456");
        log.info("测试结果:{}", JSON.toJSONString(lotteryResult));
    }

    @Test
    public void test1(){
        com.design.yes.LotteryService lotteryService = new com.design.yes.LotteryServiceImpl();
        LotteryResult result = lotteryService.draw("789456");
        log.info("测试结果：{}", JSON.toJSONString(result));
    }

}
