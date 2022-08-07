package com.wei.shardingjdbc_domo;

import com.wei.shardingjdbc_domo.entity.TbDevice;
import com.wei.shardingjdbc_domo.mapper.DeviceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

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

}
