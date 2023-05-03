package com.design.yes;

import com.design.ActivityInfoService;
import com.design.StatusEnum;
import com.design.no.Result;

/**
 * 活动状态；活动编辑中
 *
 * @author huangwei
 * @date 2023-05-03
 */
public class EditingState extends State {
    @Override
    public Result arraignment(String activityId, Enum<StatusEnum> currentStatus) {
        ActivityInfoService.execStatus(activityId, currentStatus, StatusEnum.CHECK);
        return new Result("0000", "活动提审成功");
    }

    @Override
    public Result checkPass(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "活动编辑中不能审核通过");
    }

    @Override
    public Result checkRefuse(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "活动编辑中不能审核拒绝");
    }

    @Override
    public Result checkRevoke(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "编辑中不可撤销审核");
    }

    @Override
    public Result close(String activityId, Enum<StatusEnum> currentStatus) {
        ActivityInfoService.execStatus(activityId, currentStatus, StatusEnum.CLOSE);
        return new Result("0000", "活动关闭成功");
    }

    @Override
    public Result open(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "非关闭活动不可开启");
    }

    @Override
    public Result doing(String activityId, Enum<StatusEnum> currentStatus) {
        return new Result("0001", "编辑中活动不可执行活动中变更");
    }
}
