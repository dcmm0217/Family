package com.design;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huangw
 * @date 2023/4/27 23:28
 */
public class ActivityInfoService {

    private static Map<String, Enum<StatusEnum>> statusEnumMap = new ConcurrentHashMap<>();

    public static void init(String activityId, Enum<StatusEnum> statusEnumEnum) {
        // 模拟数据库，初始活动信息
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.setActivityId(activityId);
        activityInfo.setActivityName("早起学习打卡领奖活动");
        activityInfo.setStatus(statusEnumEnum);
        activityInfo.setBeginTime(new Date());
        activityInfo.setEndTime(new Date());
        statusEnumMap.put(activityId, statusEnumEnum);
    }

    /**
     * 查询活动信息
     *
     * @param activityId 活动ID
     * @return 查询结果
     */
    public static ActivityInfo queryActivityInfo(String activityId) {
        // 模拟查询活动信息
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.setActivityId(activityId);
        activityInfo.setActivityName("早起学习打卡领奖活动");
        activityInfo.setStatus(statusEnumMap.get(activityId));
        activityInfo.setBeginTime(new Date());
        activityInfo.setEndTime(new Date());
        return activityInfo;
    }

    /**
     * 查询活动状态
     *
     * @param activityId 活动ID
     * @return 查询结果
     */
    public static Enum<StatusEnum> queryActivityStatus(String activityId) {
        return statusEnumMap.get(activityId);
    }

    /**
     * 执行状态变更
     *
     * @param activityId   活动ID
     * @param beforeStatus 修改前状态
     * @param afterStatus  修改后状态
     */
    public static synchronized void execStatus(String activityId, Enum<StatusEnum> beforeStatus, Enum<StatusEnum> afterStatus) {
        if (!beforeStatus.equals(statusEnumMap.get(activityId))) {
            return;
        }
        statusEnumMap.put(activityId, afterStatus);
    }

}
