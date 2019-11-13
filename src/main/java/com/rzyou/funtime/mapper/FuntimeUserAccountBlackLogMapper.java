package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountBlackLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserAccountBlackLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserAccountBlackLog record);

    int insertSelective(FuntimeUserAccountBlackLog record);

    FuntimeUserAccountBlackLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountBlackLog record);

    int updateByPrimaryKey(FuntimeUserAccountBlackLog record);
}