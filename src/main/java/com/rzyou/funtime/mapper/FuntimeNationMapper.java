package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeNation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeNationMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FuntimeNation record);

    int insertSelective(FuntimeNation record);

    FuntimeNation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FuntimeNation record);

    int updateByPrimaryKey(FuntimeNation record);
}