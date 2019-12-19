package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeAccusation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeAccusationMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeAccusation record);

    FuntimeAccusation selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeAccusation record);

}