package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeUserAccountRechargeRecordMapper {

    List<FuntimeUserAccountRechargeRecord> getRechargeDetailForPage(@Param("startDate") String startDate,@Param("endDate") String endDate, @Param("userId") Long userId, @Param("state") Integer state);

    Integer getRechargeRecordByUserId(Long userId);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccountRechargeRecord record);

    FuntimeUserAccountRechargeRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountRechargeRecord record);

}