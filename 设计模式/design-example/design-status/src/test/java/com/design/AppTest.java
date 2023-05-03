package com.design;


import com.alibaba.fastjson.JSON;
import com.design.no.Result;
import com.design.yes.StateHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


/**
 * Unit test for simple App.
 */
@Slf4j
public class AppTest {

    @Test
    public void test_Editing2Arraignment() {
        String activityId = "100001";
        ActivityInfoService.init(activityId, StatusEnum.EDITING);

        StateHandler stateHandler = new StateHandler();
        Result result = stateHandler.arraignment(activityId, StatusEnum.EDITING);

        log.info("测试结果(编辑中To提审活动)：{}", JSON.toJSONString(result));
        log.info("活动信息：{} 状态：{}", JSON.toJSONString(ActivityInfoService.queryActivityInfo(activityId)),
                JSON.toJSONString(ActivityInfoService.queryActivityInfo(activityId).getStatus()));
    }

}
