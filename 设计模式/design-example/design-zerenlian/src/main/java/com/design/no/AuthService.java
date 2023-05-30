package com.design.no;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟审核服务
 *
 * @author huangwei
 * @date 2023-05-30
 */
public class AuthService {

    private static Map<String, Date> authMap = new ConcurrentHashMap<>();


    /**
     * 查询审核时间
     *
     * @param uId
     * @param orderId
     * @return
     */
    public static Date queryAuthInfo(String uId, String orderId) {
        return authMap.get(uId.concat(orderId));
    }

    /**
     * 审核操作
     *
     * @param uId
     * @param orderId
     */
    public static void auth(String uId, String orderId) {
        authMap.put(uId.concat(orderId), new Date());
    }

}
