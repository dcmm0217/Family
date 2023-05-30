package com.design.yes;

import com.design.AuthInfo;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 审核链抽象类
 *
 * 审核规定；
 *  * 1. 601-610 三级审批 + 二级审批
 *  * 2. 611-620 三级审批 + 二级审批 + 一级审批
 *  * 3. 其他时间 三级审批
 *
 * @author huangwei
 * @date 2023-05-30
 */
@Slf4j
public abstract class AuthLink {
    protected SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 时间格式化

    /**
     * 级别人员ID
     */
    protected String levelUserId;

    /**
     * 级别人员名称
     */
    protected String levelUserName;

    /**
     * 责任链
     */
    private AuthLink next;

    public AuthLink(String levelUserId, String levelUserName) {
        this.levelUserId = levelUserId;
        this.levelUserName = levelUserName;
    }

    public AuthLink next() {
        return next;
    }

    public AuthLink appendNext(AuthLink next) {
        this.next = next;
        return this;
    }

    public abstract AuthInfo doAuth(String uId, String orderId, Date authDate);
}
