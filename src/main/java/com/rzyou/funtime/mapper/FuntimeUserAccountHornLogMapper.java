package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountHornLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserAccountHornLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserAccountHornLog record);

    int insertSelective(FuntimeUserAccountHornLog record);

    FuntimeUserAccountHornLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserAccountHornLog record);

    int updateByPrimaryKey(FuntimeUserAccountHornLog record);
}