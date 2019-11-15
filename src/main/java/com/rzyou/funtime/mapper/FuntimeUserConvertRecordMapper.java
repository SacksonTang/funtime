package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserConvertRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeUserConvertRecordMapper {
    int deleteByPrimaryKey(Long id);

    List<FuntimeUserConvertRecord> getUserConvertRecordForPage(@Param("convertType") Integer convertType,@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("userId") Long userId);

    int insertSelective(FuntimeUserConvertRecord record);

    FuntimeUserConvertRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserConvertRecord record);


}