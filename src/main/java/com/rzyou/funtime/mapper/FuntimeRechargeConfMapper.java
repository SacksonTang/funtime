package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeRechargeConf;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeRechargeConfMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FuntimeRechargeConf record);

    int insertSelective(FuntimeRechargeConf record);

    FuntimeRechargeConf selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FuntimeRechargeConf record);

    int updateByPrimaryKey(FuntimeRechargeConf record);
}