package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountLevelWealthLog;
import com.rzyou.funtime.entity.FuntimeUserAccountLevelWealthRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserAccountLevelWealthLogMapper {

    int insert(FuntimeUserAccountLevelWealthLog record);

    int insertRecord(FuntimeUserAccountLevelWealthRecord record);

}