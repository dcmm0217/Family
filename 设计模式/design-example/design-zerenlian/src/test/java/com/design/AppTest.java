package com.design;

import com.alibaba.fastjson.JSON;
import com.design.no.AuthController;
import com.design.no.AuthService;
import com.design.yes.AuthLink;
import com.design.yes.Level1AuthLink;
import com.design.yes.Level2AuthLink;
import com.design.yes.Level3AuthLink;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class AppTest {

    @Test
    public void test_AuthController() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        AuthController authController = new AuthController();
        Date date1 = dateFormat.parse("2023-05-11 00:00:00");
        Date date2 = dateFormat.parse("2023-05-25 00:00:00");
        // 模拟三级负责人审批
        log.info("测试结果：{}", JSON.toJSONString(authController.doAuth("10001", "1000998004813441", new Date())));
        log.info("测试结果：{}", "模拟三级负责人审批，王工");
        AuthService.auth("1000013", "1000998004813441");

        // 模拟二级负责人审批
        log.info("测试结果：{}", JSON.toJSONString(authController.doAuth("10001", "1000998004813441", date1)));
        log.info("测试结果：{}", "模拟二级负责人审批，张经理");
        AuthService.auth("1000012", "1000998004813441");

        // 模拟一级负责人审批
        log.info("测试结果：{}", JSON.toJSONString(authController.doAuth("10001", "1000998004813441", date2)));
        log.info("测试结果：{}", "模拟一级负责人审批，黄总");
        AuthService.auth("1000011", "1000998004813441");

        log.info("测试结果：{}", "审批完成");
    }

    @Test
    public void test_AuthLink() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        AuthLink authLink = new Level3AuthLink("1000013", "王工")
                .appendNext(new Level2AuthLink("1000012", "张经理")
                        .appendNext(new Level1AuthLink("1000011", "段总")));

        Date date1 = dateFormat.parse("2023-06-21 00:00:00");
        Date date2 = dateFormat.parse("2023-06-18 00:00:00");

        log.info("测试结果：{}", JSON.toJSONString(authLink.doAuth("小傅哥", "1000998004813441", new Date())));

        // 模拟三级负责人审批
        AuthService.auth("1000013", "1000998004813441");
        log.info("测试结果：{}", "模拟三级负责人审批，王工");
        log.info("测试结果：{}", JSON.toJSONString(authLink.doAuth("小傅哥", "1000998004813441", new Date())));

        // 模拟二级负责人审批
        AuthService.auth("1000012", "1000998004813441");
        log.info("测试结果：{}", "模拟二级负责人审批，张经理");
        log.info("测试结果：{}", JSON.toJSONString(authLink.doAuth("小傅哥", "1000998004813441", date1)));

        // 模拟一级负责人审批
        AuthService.auth("1000011", "1000998004813441");
        log.info("测试结果：{}", "模拟一级负责人审批，段总");
        log.info("测试结果：{}", JSON.toJSONString(authLink.doAuth("小傅哥", "1000998004813441", date2)));
    }

}
