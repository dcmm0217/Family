package com.design.yes;

import com.design.no.Matter;

/**
 * 装修菜单集合
 *
 * @author huangwei
 * @date 2023-05-22
 */
public interface IMenu {

    /**
     * 吊顶
     *
     * @param matter
     * @return
     */
    IMenu appendCeiling(Matter matter);

    /**
     * 涂料
     *
     * @param matter
     * @return
     */
    IMenu appendCoat(Matter matter);

    /**
     * 地板
     *
     * @param matter
     * @return
     */
    IMenu appendFloor(Matter matter);

    /**
     * 地砖
     *
     * @param matter
     * @return
     */
    IMenu appendTile(Matter matter);

    /**
     * 装修明细
     *
     * @return
     */
    String getDetail();
}
