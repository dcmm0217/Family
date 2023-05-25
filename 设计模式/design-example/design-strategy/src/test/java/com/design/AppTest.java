package com.design;


import com.design.yes.Context;
import com.design.yes.impl.MJCouponDiscount;
import com.design.yes.impl.NYGCouponDiscount;
import com.design.yes.impl.ZJCouponDiscount;
import com.design.yes.impl.ZKCouponDiscount;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class AppTest {

    private final Logger logger = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void test_zj() {
        // 直减 100 - 10
        Context<Double> context = new Context<>(new ZJCouponDiscount());
        BigDecimal discountAmount = context.discountAmount(10D, new BigDecimal(100));
        logger.info("测试结果：直减优惠后金额 {}", discountAmount);
    }

    @Test
    public void test_mj() {
        // 满减100 -10
        Context<Map<String, String>> context = new Context<>(new MJCouponDiscount());
        Map<String, String> map = new HashMap<>();
        map.put("x", "100");
        map.put("n", "10");
        BigDecimal discountAmount = context.discountAmount(map, new BigDecimal(100));
        logger.info("测试结果：满减优惠后金额 {}", discountAmount);
    }

    @Test
    public void test_N() {
        Context<Double> context = new Context<>(new NYGCouponDiscount());
        BigDecimal discountAmount = context.discountAmount(10D, new BigDecimal(100));
        logger.info("测试结果：直减优惠后金额 {}", discountAmount);
    }

    @Test
    public void test_zk() {
        // 折扣9折，商品100元
        Context<Double> context = new Context<Double>(new ZKCouponDiscount());
        BigDecimal discountAmount = context.discountAmount(0.9D, new BigDecimal(100));
        logger.info("测试结果：折扣9折后金额 {}", discountAmount);
    }
}
