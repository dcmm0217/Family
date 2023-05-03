package com.design.yes;

import com.design.ActivityInfoService;
import com.design.StatusEnum;
import com.design.no.Result;

/**
 * 活动状态；待审核
 *
 * @author huangwei
 * @date 2023-05-03
 */
public class CheckState extends State {
    @Override
    public Result arraignment(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "待审核状态不可重复提审");
    }

    @Override
    public Result checkPass(String activityId, Enum<StatusEnum> currentStatus) {
        ActivityInfoService.execStatus(activityId, currentStatus, StatusEnum.PASS);
        return new Result("0000", "活动审核通过完成");
    }

    @Override
    public Result checkRefuse(String activityId, Enum<StatusEnum> currentStatus) {
        ActivityInfoService.execStatus(activityId, currentStatus, StatusEnum.REFUSE);
        return new Result("0000", "活动审核拒绝完成");
    }

    @Override
    public Result checkRevoke(String activityId, Enum<StatusEnum> currentStatus) {
        ActivityInfoService.execStatus(activityId, currentStatus, StatusEnum.EDITING);
        return new Result("0000", "活动审核撤销回到编辑中");
    }

    @Override
    public Result close(String activityId, Enum<StatusEnum> currentStatus) {
        ActivityInfoService.execStatus(activityId, currentStatus, StatusEnum.CLOSE);
        return new Result("0000", "活动审核关闭完成");
    }

    @Override
    public Result open(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "非关闭活动不可开启");
    }

    @Override
    public Result doing(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "待审核活动不可执行活动中变更");
    }
}
