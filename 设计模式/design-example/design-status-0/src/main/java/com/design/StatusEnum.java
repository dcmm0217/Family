package com.design;

/**
 * @author huangw
 * @date 2023/4/27 23:20
 */
public enum StatusEnum {

    /**
     * 活动策略枚举
     */
    EDITING,  //创建编辑
    CHECK,   //待审核
    PASS,   //审核通过
    REFUSE, // 审核拒绝
    DOING,//活动中
    CLOSE,//活动关闭
    OPEN;//活动开启
}
