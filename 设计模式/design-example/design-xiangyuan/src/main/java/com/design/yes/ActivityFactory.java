package com.design.yes;

import com.design.no.Activity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 活动享元工厂
 *
 * @author huangwei
 * @date 2023-05-22
 */
public class ActivityFactory {

    public static Map<Long, Activity> activityMap = new HashMap<>();

    public static Activity getActivity(Long id) {
        Activity activity = activityMap.get(id);
        if (activity == null) {
            activity = new Activity();
            activity.setId(10001L);
            activity.setName("图书嗨乐");
            activity.setDesc("图书优惠券分享激励分享活动第二期");
            activity.setStartTime(new Date());
            activity.setStopTime(new Date());
            activityMap.put(id, activity);
        }
        return activity;
    }
}
