package com.wei.shardingJDBC.algorithm.database;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * @author huangw
 * @date 2022/8/7 23:24
 */
public class MyDatabaseStandardPreciseAlgorithm implements PreciseShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        // 逻辑表的名称
        String logicTableName = preciseShardingValue.getLogicTableName();
        // 物理表的名称
        String columnName = preciseShardingValue.getColumnName();
        // 分片键的值
        Long value = preciseShardingValue.getValue();
        String databaseName = "ds" + (value%2);
        if (!collection.contains(databaseName)){
            throw new UnsupportedOperationException("数据源" + databaseName + "不存在");
        }
        return databaseName;
    }
}
