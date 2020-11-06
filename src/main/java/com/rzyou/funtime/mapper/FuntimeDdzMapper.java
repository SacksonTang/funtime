package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeDdz;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeDdzMapper {

    int insertDdzRecord(FuntimeDdz ddz);

    List<Map<String,Object>> getRankList(@Param("startDate") String startDate, @Param("endDate") String endDate,@Param("counts") Integer counts);
}
