package com.design;


import com.design.no.DecorationPackageController;
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

}
