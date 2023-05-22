package com.design.yes;

import com.design.no.ceiling.LevelTowCeiling;
import com.design.no.coat.DoluxCoat;
import com.design.no.coat.LiBangCoat;
import com.design.no.floor.ShengXiangFloor;
import com.design.no.tile.DongPengTile;
import com.design.no.tile.MarcoPoloTile;

/**
 * 套餐包装,方便之处在于 随意组装，不影响上层逻辑
 *
 * @author huangwei
 * @date 2023-05-22
 */
public class Builder {

    public IMenu levelOne(Double area) {
        return new DecorationPackageMenu(area, "豪华欧式")
                .appendCeiling(new LevelTowCeiling())
                .appendCoat(new DoluxCoat())
                .appendFloor(new ShengXiangFloor());
    }

    public IMenu levelTow(Double area) {
        return new DecorationPackageMenu(area, "轻奢田园")
                .appendCeiling(new LevelTowCeiling())
                .appendCoat(new LiBangCoat())
                .appendTile(new MarcoPoloTile());
    }

    public IMenu levelThree(Double area) {
        return new DecorationPackageMenu(area, "现代简约")
                .appendCeiling(new LevelTowCeiling())
                .appendCoat(new LiBangCoat())
                .appendTile(new DongPengTile());
    }

}
