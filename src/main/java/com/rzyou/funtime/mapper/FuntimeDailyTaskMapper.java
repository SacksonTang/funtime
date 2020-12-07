package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeDailyTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeDailyTaskMapper {

    List<FuntimeDailyTask> getDailyTaskList(Long userId);

    FuntimeDailyTask getTaskInfoById(@Param("userId") Long userId,@Param("taskId") Integer taskId);

    int saveUserDailytask(FuntimeDailyTask task);

    int updateTaskState(Long id);

    int updateCompleteCount(@Param("id") Long id,@Param("completeCount") Integer completeCount);
}
