package com.design;


import com.alibaba.fastjson.JSON;
import com.design.no.Activity;
import com.design.yes.ActivityController;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppTest {

    private Logger logger = LoggerFactory.getLogger(AppTest.class);

    private ActivityController activityController = new ActivityController();

    @Test
    public void test_queryActivityInfo() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Long req = 10001L;
            Activity activity = activityController.queryActivityInfo(req);
            logger.info("测试结果:{},{}", req, JSON.toJSONString(activity));
            Thread.sleep(1200);
        }
    }

}
