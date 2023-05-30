package com.wei.shardingJDBC.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wei.shardingJDBC.entity.TbDeviceType;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author huangw
 * @date 2022/8/15 11:51
 */
@Mapper
public interface DeviceTypeMapper extends BaseMapper<TbDeviceType> {
}
