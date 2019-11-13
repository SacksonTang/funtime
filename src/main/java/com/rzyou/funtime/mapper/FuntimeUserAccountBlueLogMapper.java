package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountBlueLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserAccountBlueLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserAccountBlueLog record);

    int insertSelective(FuntimeUserAccountBlueLog record);

    FuntimeUserAccountBlueLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountBlueLog record);

    int updateByPrimaryKey(FuntimeUserAccountBlueLog record);
}