package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeWithdrawalConf;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FuntimeWithdrawalConfMapper {

    BigDecimal getServiceAmount(Integer rmbAmount);

    FuntimeWithdrawalConf selectByPrimaryKey(Integer id);

    List<FuntimeWithdrawalConf> getWithdralConf();

}