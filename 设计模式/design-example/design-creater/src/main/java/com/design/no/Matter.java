package com.design.no;

import java.math.BigDecimal;

/**
 * 装修物料接口
 *
 * @author huangwei
 * @date 2023-05-22
 */
public interface Matter {

    /**
     * 场景：地板、地砖、涂料、吊顶
     *
     * @return
     */
    String scene();

    /**
     * 品牌
     *
     * @return
     */
    String brand();

    /**
     * 型号
     *
     * @return
     */
    String model();

    /**
     * 每平米价格
     *
     * @return
     */
    BigDecimal price();

    /**
     * 描述
     *
     * @return
     */
    String desc();
}
