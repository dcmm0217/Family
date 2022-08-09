package com.wei.shardingJDBC.algorithm.database;

import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.util.Collection;

/**
 * 范围分库策略
 *
 * @author huangw
 * @date 2022/8/7 23:24
 */
public class MyDatabaseStandardRangeAlgorithm implements RangeShardingAlgorithm<Long> {

    /**
     * 直接返回2个数据源
     *
     * @param collection
     * @param rangeShardingValue
     * @return
     */
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {

        // 直接返回2个数据源
        return collection;
    }
}
