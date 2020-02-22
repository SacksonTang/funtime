package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountWithdrawalRecord;
import com.rzyou.funtime.entity.FuntimeUserRedpacket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FuntimeUserAccountWithdrawalRecordMapper {

    int getWithdrawalRecordCountBySucc(Long userId);

    int getWithdrawalRecordByUserId(Long userId);

    BigDecimal getSumAmountForDay(@Param("startDate") String startDate,@Param("endDate") String endDate, @Param("userId") Long userId);

    int getCountForMonth(@Param("startDate")String startDate,@Param("endDate") String endDate,@Param("userId") Long userId);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccountWithdrawalRecord record);

    FuntimeUserAccountWithdrawalRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountWithdrawalRecord record);

    List<FuntimeUserAccountWithdrawalRecord> getWithdrawalForPage(String startDate, String endDate, Long userId, Integer state);

}