package com.wei.shardingJDBC;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wei.shardingJDBC.entity.TbDevice;
import com.wei.shardingJDBC.mapper.DeviceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class ShardingJdbcDomoApplicationTests {

    @Resource
    private DeviceMapper deviceMapper;

    @Test
    void initData() {
        for (int i = 0; i < 10; i++) {
            TbDevice device = new TbDevice();
            device.setDeviceId((long) i);
            device.setDeviceType(i);
            deviceMapper.insert(device);
        }
    }

    @Test
    void testQueryById() {
        LambdaQueryWrapper<TbDevice> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TbDevice::getDeviceId, 1);
        List<TbDevice> tbDeviceList = deviceMapper.selectList(lambdaQueryWrapper);
        System.out.println(tbDeviceList);
    }

    @Test
    void testQueryRange() {
        LambdaQueryWrapper<TbDevice> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.between(TbDevice::getDeviceId, 1L, 10L);
        // Inline strategy cannot support this type sharding:RangeRouteValue
        List<TbDevice> tbDeviceList = deviceMapper.selectList(lambdaQueryWrapper);
        System.out.println(tbDeviceList);
    }


}
