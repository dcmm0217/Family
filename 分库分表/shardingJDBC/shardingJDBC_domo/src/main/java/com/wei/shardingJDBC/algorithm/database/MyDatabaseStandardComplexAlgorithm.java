package com.wei.shardingJDBC.algorithm.database;

import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 复合分片分库策略
 *
 * @author huangw
 * @date 2022/8/7 23:24
 */
public class MyDatabaseStandardComplexAlgorithm implements ComplexKeysShardingAlgorithm<Integer> {

    /**
     * 复合分片
     *
     * @param collection
     * @param complexKeysShardingValue
     * @return 返回是这一次要查找的数据节点（数据库）集合
     */
    @Override
    public Collection<String> doSharding(Collection<String> collection, ComplexKeysShardingValue<Integer> complexKeysShardingValue) {
        Map<String, Collection<Integer>> shardingValuesMap = complexKeysShardingValue.getColumnNameAndShardingValuesMap();
        Collection<Integer> deviceTypeValues = shardingValuesMap.get("device_type");
        Collection<String> databases = new ArrayList<>();
        for (Integer deviceTypeValue : deviceTypeValues) {
            String databaseName = "ds" + (deviceTypeValue % 2);
            databases.add(databaseName);
        }
        return databases;
    }
}
