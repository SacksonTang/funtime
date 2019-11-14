package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeGift;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FuntimeGiftMapper {
    int deleteByPrimaryKey(Integer id);


    int insertSelective(FuntimeGift record);

    FuntimeGift selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FuntimeGift record);

}