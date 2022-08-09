package com.wei.shardingJDBC.algorithm.table;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * 精确分表策略
 *
 * @author huangw
 * @date 2022/8/7 23:24
 */
public class MyTableStandardPreciseAlgorithm implements PreciseShardingAlgorithm<Long> {


    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        Long value = preciseShardingValue.getValue();
        String tableName = preciseShardingValue.getLogicTableName() + "_" + (value % 2);
        if (!collection.contains(tableName)) {
            throw new UnsupportedOperationException("表:" + tableName + "不存在");
        }
        return tableName;
    }
}
