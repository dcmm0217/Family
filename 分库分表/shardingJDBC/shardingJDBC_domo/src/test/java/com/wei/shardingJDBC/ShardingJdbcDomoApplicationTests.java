package com.wei.shardingJDBC;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wei.shardingJDBC.entity.TbDevice;
import com.wei.shardingJDBC.entity.TbDeviceInfo;
import com.wei.shardingJDBC.entity.TbDeviceType;
import com.wei.shardingJDBC.mapper.DeviceInfoMapper;
import com.wei.shardingJDBC.mapper.DeviceMapper;
import com.wei.shardingJDBC.mapper.DeviceTypeMapper;
import org.apache.shardingsphere.api.hint.HintManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class ShardingJdbcDomoApplicationTests {

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    @Resource
    private DeviceTypeMapper deviceTypeMapper;

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
        lambdaQueryWrapper.eq(TbDevice::getDeviceId, 1L);
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

    @Test
    void testDeviceRangeAndType() {
        LambdaQueryWrapper<TbDevice> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.between(TbDevice::getDeviceId, 1L, 10L);
        lambdaQueryWrapper.eq(TbDevice::getDeviceType, 5);
        List<TbDevice> tbDeviceList = deviceMapper.selectList(lambdaQueryWrapper);
        System.out.println(tbDeviceList);
    }

    @Test
    void testHint() {
        HintManager hintManager = HintManager.getInstance();
        // 强制路由 tb_device_0表
        hintManager.addTableShardingValue("tb_device", 0);
        List<TbDevice> tbDeviceList = deviceMapper.selectList(null);
        System.out.println(tbDeviceList);
    }

    @Test
    void testInsertType() {
        for (int i = 0; i < 10; i++) {
            TbDevice tbDevice = new TbDevice();
            tbDevice.setDeviceId((long) i);
            tbDevice.setDeviceType(i);
            deviceMapper.insert(tbDevice);
            TbDeviceInfo tbDeviceInfo = new TbDeviceInfo();
            tbDeviceInfo.setDeviceId((long) i);
            tbDeviceInfo.setDeviceIntro(i + "");
            deviceInfoMapper.insert(tbDeviceInfo);
        }
    }

    @Test
    void testJoinSelect() {
        List<TbDeviceInfo> tbDeviceInfos = deviceInfoMapper.queryDeviceInfo();
        tbDeviceInfos.forEach(System.out::println);
    }

    @Test
    void testInsertTbDeviceType(){
        TbDeviceType deviceType1 = new TbDeviceType();
        deviceType1.setTypeId(1);
        deviceType1.setTypeName("⼈脸考勤");
        deviceTypeMapper.insert(deviceType1);
        TbDeviceType deviceType2 = new TbDeviceType();
        deviceType2.setTypeId(2);
        deviceType2.setTypeName("⼈脸通道");
        deviceTypeMapper.insert(deviceType2);
    }


}
