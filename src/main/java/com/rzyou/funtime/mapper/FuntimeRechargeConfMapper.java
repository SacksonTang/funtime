package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeRechargeConf;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FuntimeRechargeConfMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FuntimeRechargeConf record);

    int insertSelective(FuntimeRechargeConf record);

    FuntimeRechargeConf selectByPrimaryKey(Integer id);

    List<FuntimeRechargeConf> getRechargeConf();

    int updateByPrimaryKeySelective(FuntimeRechargeConf record);

}