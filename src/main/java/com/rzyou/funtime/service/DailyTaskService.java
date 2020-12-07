package com.rzyou.funtime.service;

import com.rzyou.funtime.common.ResultMsg;

import java.util.Map;

public interface DailyTaskService {

    /**
     * 日常任务初始化
     * @param userId
     * @return
     */
    Map<String,Object> getDailyTaskList(Long userId);

    /**
     * 领取奖励
     * @param userId
     * @param taskId
     * @return
     */
    ResultMsg<Object> receiveAward(Long userId, Integer taskId);

    /**
     * 做日常
     * @param userId
     * @param taskId
     * @param counts
     */
    void doDailyTask(Long userId,Integer taskId,Integer counts);
}
