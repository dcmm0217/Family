package com.design.yes;

import com.design.ActivityInfoService;
import com.design.StatusEnum;
import com.design.no.Result;

/**
 * 活动状态；活动开启
 *
 * @author huangwei
 * @date 2023-05-03
 */
public class OpenState extends State {
    @Override
    public Result arraignment(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "活动开启不可提审");
    }

    @Override
    public Result checkPass(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "活动开启不可审核通过");
    }

    @Override
    public Result checkRefuse(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "活动开启不可审核拒绝");
    }

    @Override
    public Result checkRevoke(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "活动开启不可撤销审核");
    }

    @Override
    public Result close(String activityId, Enum<StatusEnum> currentStatus) {
        ActivityInfoService.execStatus(activityId, currentStatus, StatusEnum.CLOSE);
        return new Result("0000", "活动关闭完成");
    }

    @Override
    public Result open(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "活动不可重复开启");
    }

    @Override
    public Result doing(String activityId, Enum<StatusEnum> currentStatus) {
        ActivityInfoService.execStatus(activityId, currentStatus, StatusEnum.DOING);
        return new Result("0000", "活动变更活动中完成");
    }
}
