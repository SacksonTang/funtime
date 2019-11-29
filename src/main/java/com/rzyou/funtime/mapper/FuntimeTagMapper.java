package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeTagMapper {


    List<Integer> queryTagsByUserId(Long userId);

    List<FuntimeTag> queryTagsByType(String tagType);

    int deleteByPrimaryKey(Integer id);

    int insertSelective(FuntimeTag record);

    FuntimeTag selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FuntimeTag record);

}