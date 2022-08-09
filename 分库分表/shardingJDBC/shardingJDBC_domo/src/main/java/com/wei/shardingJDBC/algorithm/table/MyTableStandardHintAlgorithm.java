package com.wei.shardingJDBC.algorithm.table;

import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;

import java.util.Arrays;
import java.util.Collection;

/**
 * 强制路由分表策略
 *
 * @author huangw
 * @date 2022/8/7 23:24
 */
public class MyTableStandardHintAlgorithm implements HintShardingAlgorithm<Long> {

    @Override
    public Collection<String> doSharding(Collection<String> collection, HintShardingValue<Long> hintShardingValue) {
        String logicTableName = hintShardingValue.getLogicTableName();
        String tableName = logicTableName + "_" + hintShardingValue.getValues().toArray()[0];
        if (!collection.contains(tableName)){
            throw new UnsupportedOperationException("表：" + tableName + "不存在");
        }
        return Arrays.asList(tableName);
    }
}
