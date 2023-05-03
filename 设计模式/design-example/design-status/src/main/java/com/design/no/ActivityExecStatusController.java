package com.design.no;

import com.design.ActivityInfoService;
import com.design.StatusEnum;

/**
 * 活动状态管理执行器
 *
 * @author huangw
 * @date 2023/4/27 23:41
 */
public class ActivityExecStatusController {

    /**
     * 处理活动状态变更方法
     * <p>
     * 状态变更逻辑：
     * 1. 编辑中 -> 提审、关闭
     * 2. 审核通过 -> 拒绝、关闭、活动中
     * 3. 审核拒绝 -> 撤审、关闭
     * 4. 活动中 -> 关闭
     * 5. 活动关闭 -> 开启
     * 6. 活动开启 -> 关闭
     *
     * @param activityId   活动id
     * @param beforeStatus 变更前状态
     * @param afterStatus  变更后状态
     * @return 返回结果
     */
    public Result execStatus(String activityId, Enum<StatusEnum> beforeStatus, Enum<StatusEnum> afterStatus) {
        // 1. 编辑中 -> 提审、关闭
        if (StatusEnum.EDITING.equals(beforeStatus)) {
            if (StatusEnum.CHECK.equals(afterStatus) || StatusEnum.CLOSE.equals(afterStatus)) {
                ActivityInfoService.execStatus(activityId, beforeStatus, afterStatus);
                return new Result("0000", "变更状态成功");
            } else {
                return new Result("0001", "变更状态拒绝");
            }
        }

        // 2. 审核通过 -> 拒绝、关闭、活动中
        if (StatusEnum.PASS.equals(beforeStatus)) {
            if (StatusEnum.REFUSE.equals(afterStatus) || StatusEnum.CLOSE.equals(afterStatus) || StatusEnum.DOING.equals(afterStatus)) {
                ActivityInfoService.execStatus(activityId, beforeStatus, afterStatus);
                return new Result("0000", "变更状态成功");
            } else {
                return new Result("0001", "变更状态拒绝");
            }
        }

        // 3. 审核拒绝 -> 撤审、关闭
        if (StatusEnum.REFUSE.equals(beforeStatus)) {
            if (StatusEnum.EDITING.equals(afterStatus) || StatusEnum.CLOSE.equals(afterStatus)) {
                ActivityInfoService.execStatus(activityId, beforeStatus, afterStatus);
                return new Result("0000", "变更状态成功");
            } else {
                return new Result("0001", "变更状态拒绝");
            }
        }

        // 4. 活动中 -> 关闭
        if (StatusEnum.DOING.equals(beforeStatus)) {
            if (StatusEnum.CLOSE.equals(afterStatus)) {
                ActivityInfoService.execStatus(activityId, beforeStatus, afterStatus);
                return new Result("0000", "变更状态成功");
            } else {
                return new Result("0001", "变更状态拒绝");
            }
        }


        // 5. 活动关闭 -> 开启
        if (StatusEnum.CLOSE.equals(beforeStatus)) {
            if (StatusEnum.OPEN.equals(afterStatus)) {
                ActivityInfoService.execStatus(activityId, beforeStatus, afterStatus);
                return new Result("0000", "变更状态成功");
            } else {
                return new Result("0001", "变更状态拒绝");
            }
        }

        // 6. 活动开启 -> 关闭
        if (StatusEnum.OPEN.equals(beforeStatus)) {
            if (StatusEnum.CLOSE.equals(afterStatus)) {
                ActivityInfoService.execStatus(activityId, beforeStatus, afterStatus);
                return new Result("0000", "变更状态成功");
            } else {
                return new Result("0001", "变更状态拒绝");
            }
        }

        return new Result("0001", "非可处理的活动状态变更");
    }


}
