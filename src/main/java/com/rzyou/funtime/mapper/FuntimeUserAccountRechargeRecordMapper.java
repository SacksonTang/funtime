package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeUserAccountRechargeRecordMapper {

    List<FuntimeUserAccountRechargeRecord> getRechargeDetailForPage(@Param("queryDate") String queryDate, @Param("userId") Long userId, @Param("state") Integer state);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccountRechargeRecord record);

    FuntimeUserAccountRechargeRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountRechargeRecord record);

}