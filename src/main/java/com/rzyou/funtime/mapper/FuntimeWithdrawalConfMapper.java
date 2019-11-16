package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeWithdrawalConf;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

@Mapper
public interface FuntimeWithdrawalConfMapper {

    BigDecimal getServiceAmount(Integer blackAmount);

    int deleteByPrimaryKey(Integer id);

    int insertSelective(FuntimeWithdrawalConf record);

    FuntimeWithdrawalConf selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FuntimeWithdrawalConf record);

}