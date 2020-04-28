package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeRechargeConf;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FuntimeRechargeConfMapper {

    FuntimeRechargeConf selectByPrimaryKey(Integer id);

    FuntimeRechargeConf getRechargeConfByProductId(String productId);

    List<FuntimeRechargeConf> getRechargeConf(Integer platform);


}