package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeSubscriptionRatioConf;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeSubscriptionRatioConfMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FuntimeSubscriptionRatioConf record);

    int insertSelective(FuntimeSubscriptionRatioConf record);

    FuntimeSubscriptionRatioConf selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FuntimeSubscriptionRatioConf record);

    int updateByPrimaryKey(FuntimeSubscriptionRatioConf record);
}