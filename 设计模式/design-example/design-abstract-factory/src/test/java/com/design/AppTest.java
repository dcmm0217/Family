package com.design;


import com.design.yes.CacheService;
import com.design.yes.CacheServiceImpl;
import com.design.yes.factory.EGMCacheAdapter;
import com.design.yes.factory.IIRCacheAdapter;
import com.design.yes.factory.JDKProxy;
import org.junit.jupiter.api.Test;

public class AppTest {

//    @Test
//    public void test_no() {
//        CacheService cacheService = new CacheServiceImpl();
//        cacheService.set("user_name_01", "test", 2);
//        String userName01 = cacheService.get("user_name_01", 2);
//        System.out.println("测试结果：" + userName01);
//    }

    @Test
    public void test_yes() throws Exception {
        CacheService poxyEGM = JDKProxy.getProxy(CacheServiceImpl.class, new EGMCacheAdapter());
        poxyEGM.set("user_name_01", "小傅哥");
        String val01 = poxyEGM.get("user_name_01");
        System.out.println("测试结果：" + val01);

        CacheService proxyIIR = JDKProxy.getProxy(CacheServiceImpl.class, new IIRCacheAdapter());
        proxyIIR.set("user_name_01", "小傅哥");
        String val02 = proxyIIR.get("user_name_01");
        System.out.println("测试结果：" + val02);
    }
}
