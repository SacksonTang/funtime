package com.rzyou.funtime.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FuntimeDailyTaskMapper {

    Integer checkUserDailytask(@Param("userId") Long userId, @Param("taskDay")Integer taskDay, @Param("taskId")Integer taskId);


}
