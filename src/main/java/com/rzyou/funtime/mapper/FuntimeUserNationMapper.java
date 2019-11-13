package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserNation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserNationMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserNation record);

    int insertSelective(FuntimeUserNation record);

    FuntimeUserNation selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserNation record);

    int updateByPrimaryKey(FuntimeUserNation record);
}