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

    FuntimeRechargeConf getRechargeConfByProductId(String productId);

    List<FuntimeRechargeConf> getRechargeConf(Integer platform);

    int updateByPrimaryKeySelective(FuntimeRechargeConf record);

}