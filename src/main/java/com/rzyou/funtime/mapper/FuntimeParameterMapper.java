package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeParameter;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeParameterMapper {

    String getParameterValueByKey(String key);

    int deleteByPrimaryKey(Integer id);

    int insert(FuntimeParameter record);

    int insertSelective(FuntimeParameter record);

    FuntimeParameter selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FuntimeParameter record);

    int updateByPrimaryKey(FuntimeParameter record);
}