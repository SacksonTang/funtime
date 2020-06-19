package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserValid;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserValidMapper {

    Long checkValidExist(FuntimeUserValid record);

    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeUserValid record);

    FuntimeUserValid selectByUserId(Long userId);

    int updateByPrimaryKeySelective(FuntimeUserValid record);

}