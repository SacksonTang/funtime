package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserLoginRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserLoginRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserLoginRecord record);

    int insertSelective(FuntimeUserLoginRecord record);

    FuntimeUserLoginRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserLoginRecord record);

    int updateByPrimaryKey(FuntimeUserLoginRecord record);
}
