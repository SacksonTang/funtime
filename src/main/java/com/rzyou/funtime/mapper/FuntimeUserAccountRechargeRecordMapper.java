package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeAppleRefund;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeUserAccountRechargeRecordMapper {

    FuntimeUserAccountRechargeRecord checkTransactionIdRefund(String transactionId);

    Integer checkTransactionId(String transactionId);

    Integer checkIosRecharge(Long userId);

    Integer checkTransactionIdExist(String transactionId);

    List<FuntimeUserAccountRechargeRecord> getRechargeRecordByTask();

    List<FuntimeUserAccountRechargeRecord> getRechargeDetailForPage(@Param("startDate") String startDate,@Param("endDate") String endDate, @Param("userId") Long userId, @Param("state") Integer state);

    Integer getRechargeRecordByUserId(Long userId);

    Map<String,Object> getUserLevel(Integer amount);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserAccountRechargeRecord record);

    FuntimeUserAccountRechargeRecord selectByPrimaryKey(Long id);

    FuntimeUserAccountRechargeRecord getRechargeRecordByOrderNo(String orderNo);

    int updateByPrimaryKeySelective(FuntimeUserAccountRechargeRecord record);

    int insertAppleRefund(FuntimeAppleRefund appleRefund);

}