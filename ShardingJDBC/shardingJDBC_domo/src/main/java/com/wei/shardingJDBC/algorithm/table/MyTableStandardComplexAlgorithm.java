package com.wei.shardingJDBC.algorithm.table;

import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 精确分表策略
 *
 * @author huangw
 * @date 2022/8/7 23:24
 */
public class MyTableStandardComplexAlgorithm implements ComplexKeysShardingAlgorithm<Integer> {

    @Override
    public Collection<String> doSharding(Collection<String> collection, ComplexKeysShardingValue<Integer> complexKeysShardingValue) {
        Map<String, Collection<Integer>> shardingValuesMap = complexKeysShardingValue.getColumnNameAndShardingValuesMap();
        Collection<Integer> deviceTypeValues = shardingValuesMap.get("device_type");
        String logicTableName = complexKeysShardingValue.getLogicTableName();
        Collection<String> tableNames = new ArrayList<>();
        for (Integer deviceTypeValue : deviceTypeValues) {
            String tableName = logicTableName + "_" + (deviceTypeValue % 2);
            tableNames.add(tableName);
        }
        return tableNames;
    }
}
