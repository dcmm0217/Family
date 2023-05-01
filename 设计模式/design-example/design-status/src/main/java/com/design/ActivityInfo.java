package com.design;

import lombok.Data;

import java.util.Date;

/**
 * 活动信息实体
 *
 * @author huangw
 * @date 2023/4/27 23:26
 */
@Data
public class ActivityInfo {

    private String activityId;    // 活动ID

    private String activityName;  // 活动名称

    private Enum<StatusEnum> status;  // 活动状态

    private Date beginTime;       // 开始时间

    private Date endTime;         // 结束时间
}
