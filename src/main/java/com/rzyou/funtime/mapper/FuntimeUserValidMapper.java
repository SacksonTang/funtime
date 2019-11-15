package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserValid;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserValidMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserValid record);

    FuntimeUserValid selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserValid record);

}