package com.wei.shardingJDBC.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wei.shardingJDBC.entity.TbDeviceInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author huangw
 * @date 2022/8/15 10:48
 */
@Mapper
public interface DeviceInfoMapper extends BaseMapper<TbDeviceInfo> {


    @Select("select info.id,info.device_id,info.device_intro,device.device_type from tb_device_info info left join tb_device device on info.device_id = device.device_id")
    List<TbDeviceInfo> queryDeviceInfo();
}
