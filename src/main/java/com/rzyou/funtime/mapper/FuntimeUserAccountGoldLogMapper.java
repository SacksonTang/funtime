package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountGoldLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserAccountGoldLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserAccountGoldLog record);

    int insertSelective(FuntimeUserAccountGoldLog record);

    FuntimeUserAccountGoldLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountGoldLog record);

    int updateByPrimaryKey(FuntimeUserAccountGoldLog record);
}