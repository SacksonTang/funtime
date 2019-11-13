package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeSms;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FuntimeSmsMapper {

    FuntimeSms querySmsByMobile(@Param("mobileNumber") String mobileNumber, @Param("validateCode") String validateCode);

    int deleteByPrimaryKey(Long id);

    int insert(FuntimeSms record);

    int insertSelective(FuntimeSms record);

    FuntimeSms selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeSms record);

    int updateByPrimaryKey(FuntimeSms record);
}