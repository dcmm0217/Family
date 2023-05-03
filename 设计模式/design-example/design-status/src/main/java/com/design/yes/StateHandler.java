package com.design.yes;

import com.design.StatusEnum;
import com.design.no.Result;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 活动状态处理器
 *
 * @author huangwei
 * @date 2023-05-03
 */
public class StateHandler {

    private Map<Enum<StatusEnum>, State> stateMap = new ConcurrentHashMap<>();

    // map容器处理映射关系    什么样的状态对应什么样的动作
    public StateHandler() {
        stateMap.put(StatusEnum.CHECK, new CheckState());     // 待审核
        stateMap.put(StatusEnum.CLOSE, new CloseState());     // 已关闭
        stateMap.put(StatusEnum.DOING, new DoingState());     // 活动中
        stateMap.put(StatusEnum.EDITING, new EditingState()); // 编辑中
        stateMap.put(StatusEnum.OPEN, new OpenState());       // 已开启
        stateMap.put(StatusEnum.PASS, new PassState());       // 审核通过
        stateMap.put(StatusEnum.REFUSE, new RefuseState());   // 审核拒绝
    }

    /**
     * 活动提审
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public Result arraignment(String activityId, Enum<StatusEnum> currentStatus) {
        return stateMap.get(currentStatus).arraignment(activityId, currentStatus);
    }

    /**
     * 审核通过
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public Result checkPass(String activityId, Enum<StatusEnum> currentStatus) {
        return stateMap.get(currentStatus).arraignment(activityId, currentStatus);
    }

    /**
     * 审核拒绝
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public Result checkRefuse(String activityId, Enum<StatusEnum> currentStatus) {
        return stateMap.get(currentStatus).arraignment(activityId, currentStatus);
    }

    /**
     * 撤审撤销
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public Result checkRevoke(String activityId, Enum<StatusEnum> currentStatus) {
        return stateMap.get(currentStatus).arraignment(activityId, currentStatus);
    }

    /**
     * 活动关闭
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public Result close(String activityId, Enum<StatusEnum> currentStatus) {
        return stateMap.get(currentStatus).arraignment(activityId, currentStatus);
    }

    /**
     * 活动开启
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public Result open(String activityId, Enum<StatusEnum> currentStatus) {
        return stateMap.get(currentStatus).arraignment(activityId, currentStatus);
    }

    /**
     * 活动执行
     *
     * @param activityId    活动ID
     * @param currentStatus 当前活动状态
     * @return 执行结果
     */
    public Result doing(String activityId, Enum<StatusEnum> currentStatus) {
        return stateMap.get(currentStatus).arraignment(activityId, currentStatus);
    }
}
