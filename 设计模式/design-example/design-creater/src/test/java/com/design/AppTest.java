package com.design;


import com.design.no.DecorationPackageController;
import com.design.yes.Builder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void test_DecorationPackageController() {
        DecorationPackageController decoration = new DecorationPackageController();

        // 豪华欧式
        System.out.println(decoration.getMatterList(new BigDecimal("100"), 1));

        // 轻奢田园
        System.out.println(decoration.getMatterList(new BigDecimal("158"), 2));

        // 现代简约
        System.out.println(decoration.getMatterList(new BigDecimal("88"), 3));
    }

    @Test
    public void test_Builder() {
        Builder builder = new Builder();
        // 豪华欧式
        System.out.println(builder.levelOne(100d).getDetail());

        // 轻奢田园
        System.out.println(builder.levelTow(158d).getDetail());

        // 现代简约
        System.out.println(builder.levelThree(88d).getDetail());
    }

}
