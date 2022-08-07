package com.wei.shardingjdbc_domo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wei.shardingjdbc_domo.entity.TbDevice;
import org.apache.ibatis.annotations.Mapper;


/**
 * @author huangw
 * @date 2022/8/7 16:01
 */
@Mapper
public interface DeviceMapper extends BaseMapper<TbDevice> {
}
