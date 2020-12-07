package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.entity.FuntimeDailyTask;
import com.rzyou.funtime.mapper.FuntimeDailyTaskMapper;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.DailyTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyTaskServiceImpl implements DailyTaskService {

    @Autowired
    AccountService accountService;
    @Autowired
    FuntimeDailyTaskMapper dailyTaskMapper;

    @Override
    public Map<String, Object> getDailyTaskList(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<FuntimeDailyTask> list = dailyTaskMapper.getDailyTaskList(userId);
        for (FuntimeDailyTask dailyTask : list){
            if (dailyTask.getTaskId().equals(5)&&dailyTask.getUserValid() == 1){
                dailyTask.setState(2);
            }
        }
        result.put("tasks",list);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> receiveAward(Long userId, Integer taskId) {
        FuntimeDailyTask taskInfo = dailyTaskMapper.getTaskInfoById(userId, taskId);
        if (taskInfo == null||taskInfo.getCompleteCount()<taskInfo.getTaskCount()){
            return new ResultMsg<>(ErrorMsgEnum.TASK_NOT_COMPLETE);
        }
        if (taskInfo.getState() == 2){
            return new ResultMsg<>(ErrorMsgEnum.TASK_IS_RECEIVED);
        }
        if (taskInfo.getUserValid() == 1&&taskId.equals(5)){
            return new ResultMsg<>(ErrorMsgEnum.TASK_IS_RECEIVED);
        }
        accountService.receiveAward(userId,taskInfo.getRewardType(),taskInfo.getReward(),taskInfo.getId(),taskInfo.getGiftId());

        dailyTaskMapper.updateTaskState(taskInfo.getId());
        return new ResultMsg<>();
    }

    @Override
    public void doDailyTask(Long userId,Integer taskId, Integer counts) {
        FuntimeDailyTask taskInfo = dailyTaskMapper.getTaskInfoById(userId, taskId);
        if (taskInfo == null){
            return;
        }
        if (taskInfo.getState() == 2){
            return;
        }
        if(taskInfo.getCompleteCount().equals(counts)||taskInfo.getCompleteCount().equals(taskInfo.getTaskCount())) {
            return;
        }
        if (taskId.equals(5)&&taskInfo.getUserValid() == 1){
            return;
        }
        Integer completeCount = counts+taskInfo.getCompleteCount()>taskInfo.getTaskCount()?taskInfo.getTaskCount():(counts+taskInfo.getCompleteCount());
        if (taskInfo.getId() == null){

            taskInfo = new FuntimeDailyTask();
            taskInfo.setUserId(userId);
            taskInfo.setCompleteCount(completeCount);
            taskInfo.setTaskId(taskId);
            if (taskId == 1){
                taskInfo.setState(2);
            }else{
                taskInfo.setState(1);
            }
            dailyTaskMapper.saveUserDailytask(taskInfo);
        }else{
            if (taskId!=1) {
                dailyTaskMapper.updateCompleteCount(taskInfo.getId(), counts);
            }
        }

    }
}
