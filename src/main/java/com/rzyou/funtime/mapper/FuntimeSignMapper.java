package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeSignRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeSignMapper {

    Long getSignCheck(@Param("date") Integer date,@Param("userId") Long userId);

    int saveSignRecord(FuntimeSignRecord record);
}
