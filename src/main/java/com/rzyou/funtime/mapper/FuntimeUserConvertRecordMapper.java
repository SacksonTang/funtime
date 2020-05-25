package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserConvertRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeUserConvertRecordMapper {

    List<Map<String,Object>> getGoldConvertConf(Integer id);

    int deleteByPrimaryKey(Long id);

    List<FuntimeUserConvertRecord> getUserConvertRecordForPage(@Param("convertType") Integer convertType,@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("userId") Long userId);

    int insertSelective(FuntimeUserConvertRecord record);

    FuntimeUserConvertRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserConvertRecord record);


}