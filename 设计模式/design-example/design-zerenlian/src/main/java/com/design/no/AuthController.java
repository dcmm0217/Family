package com.design.no;

import com.design.AuthInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 模拟审核服务
 *
 * @author huangwei
 * @date 2023-05-30
 */
public class AuthController {
    private SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 时间格式化


    /**
     * 审核判断规则
     *
     * @param uId      用户id
     * @param orderId  订单id
     * @param authDate 审核时间
     * @return
     * @throws ParseException
     */
    public AuthInfo doAuth(String uId, String orderId, Date authDate) throws ParseException {

        // 三级审批
        Date date = AuthService.queryAuthInfo("1000013", orderId);
        if (date == null) {
            return new AuthInfo("0001", "单号：", orderId, "状态：待三级审批负责人 ", "王工");
        }

        // 二级审批
        if (authDate.after((f.parse("2023-05-10 00:00:00"))) && authDate.before(f.parse("2023-05-20 11:11:11"))) {
            date = AuthService.queryAuthInfo("1000012", orderId);
            if (date == null) {
                return new AuthInfo("0001", "单号：", orderId, "状态：待二级审批负责人 ", "张经理");
            }
        }
        // 一级审批
        if (authDate.after((f.parse("2023-05-10 00:00:00"))) && authDate.before(f.parse("2023-05-30 11:11:11"))) {
            date = AuthService.queryAuthInfo("1000011", orderId);
            if (date == null) {
                return new AuthInfo("0001", "单号：", orderId, "状态：待一级审批负责人 ", "黄总");
            }
        }
        return new AuthInfo("0001", "单号：", orderId, " 状态：审批完成");
    }

}
