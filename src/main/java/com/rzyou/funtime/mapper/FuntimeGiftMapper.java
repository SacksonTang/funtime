package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeGift;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeGiftMapper {
    int deleteByPrimaryKey(Integer id);


    List<Map<String,Object>> getGiftByUserId(Long userId);

    int insertSelective(FuntimeGift record);

    FuntimeGift selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FuntimeGift record);

}