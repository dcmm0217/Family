package com.design.no;

import com.design.no.ceiling.LevelOneCeiling;
import com.design.no.ceiling.LevelTowCeiling;
import com.design.no.coat.DoluxCoat;
import com.design.no.coat.LiBangCoat;
import com.design.no.floor.ShengXiangFloor;
import com.design.no.tile.DongPengTile;
import com.design.no.tile.MarcoPoloTile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 不使用建造者模式，获取装修方案的方法
 *
 * @author huangwei
 * @date 2023-05-22
 */
public class DecorationPackageController {

    public String getMatterList(BigDecimal area, Integer level) {
        // 装修清单
        List<Matter> list = new ArrayList<>();
        BigDecimal price = BigDecimal.ZERO;

        // 豪华欧式
        if (1 == level) {
            // 吊顶，二级顶
            LevelTowCeiling levelTowCeiling = new LevelTowCeiling();
            // 涂料，多乐士
            DoluxCoat doluxCoat = new DoluxCoat();
            // 地板，圣像
            ShengXiangFloor shengXiangFloor = new ShengXiangFloor();
            list.add(levelTowCeiling);
            list.add(doluxCoat);
            list.add(shengXiangFloor);

            price = price.add(area.multiply(new BigDecimal("0.2")).multiply(levelTowCeiling.price()));
            price = price.add(area.multiply(new BigDecimal("1.4")).multiply(doluxCoat.price()));
            price = price.add(area.multiply(shengXiangFloor.price()));
        }

        // 轻奢田园
        if (2 == level) {
            // 吊顶，二级顶
            LevelTowCeiling levelTowCeiling = new LevelTowCeiling();
            // 涂料，立邦
            LiBangCoat liBangCoat = new LiBangCoat();
            // 地砖，马可波罗
            MarcoPoloTile marcoPoloTile = new MarcoPoloTile();
            list.add(levelTowCeiling);
            list.add(liBangCoat);
            list.add(marcoPoloTile);
            price = price.add(area.multiply(new BigDecimal("0.2")).multiply(levelTowCeiling.price()));
            price = price.add(area.multiply(new BigDecimal("1.4")).multiply(liBangCoat.price()));
            price = price.add(area.multiply(marcoPoloTile.price()));
        }

        // 现在简约
        if (3 == level) {
            // 吊顶，一级顶
            LevelOneCeiling levelOneCeiling = new LevelOneCeiling();
            // 涂料，立邦
            LiBangCoat liBangCoat = new LiBangCoat();
            // 地砖，东鹏
            DongPengTile dongPengTile = new DongPengTile();

            list.add(levelOneCeiling);
            list.add(liBangCoat);
            list.add(dongPengTile);
            price = price.add(area.multiply(new BigDecimal("0.2")).multiply(levelOneCeiling.price()));
            price = price.add(area.multiply(new BigDecimal("1.4")).multiply(liBangCoat.price()));
            price = price.add(area.multiply(dongPengTile.price()));
        }
        StringBuilder detail = new StringBuilder("\r\n-------------------------------------------------------\r\n" +
                "装修清单" + "\r\n" +
                "套餐等级：" + level + "\r\n" +
                "套餐价格：" + price.setScale(2, RoundingMode.HALF_UP) + " 元\r\n" +
                "房屋面积：" + area.doubleValue() + " 平米\r\n" +
                "材料清单：\r\n");

        for (Matter matter : list) {
            detail.append(matter.scene()).append("：").append(matter.brand()).append("、").append(matter.model()).append("、平米价格：").append(matter.price()).append(" 元。\n");
        }

        return detail.toString();
    }
}
