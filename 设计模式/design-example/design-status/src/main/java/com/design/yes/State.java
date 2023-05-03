package com.design.yes;

import com.design.StatusEnum;
import com.design.no.Result;

/**
 * 通用状态处理方法
 * <p>
 * 抽象活动审批所有的工作
 *
 * @author huangwei
 * @date 2023-05-03
 */
public abstract class State {

    /**
     * 活动提审
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public abstract Result arraignment(String activityId, Enum<StatusEnum> currentStatus);

    /**
     * 审核通过
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public abstract Result checkPass(String activityId, Enum<StatusEnum> currentStatus);


    /**
     * 审核拒绝
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public abstract Result checkRefuse(String activityId, Enum<StatusEnum> currentStatus);

    /**
     * 撤审撤销
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public abstract Result checkRevoke(String activityId, Enum<StatusEnum> currentStatus);

    /**
     * 活动关闭
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public abstract Result close(String activityId, Enum<StatusEnum> currentStatus);

    /**
     * 活动开启
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public abstract Result open(String activityId, Enum<StatusEnum> currentStatus);

    /**
     * 活动执行
     *
     * @param activityId    活动ID
     * @param currentStatus 当前状态
     * @return 执行结果
     */
    public abstract Result doing(String activityId, Enum<StatusEnum> currentStatus);
}
