package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserTag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeUserTagMapper {

    int deleteUserTags(Long userId);

    int deleteByPrimaryKey(Long id);

    int insert(FuntimeUserTag record);

    int insertSelective(FuntimeUserTag record);

    FuntimeUserTag selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeUserTag record);

    int updateByPrimaryKey(FuntimeUserTag record);
}