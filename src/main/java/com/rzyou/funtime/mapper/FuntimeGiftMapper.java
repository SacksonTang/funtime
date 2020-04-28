package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeGift;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeGiftMapper {

    List<Map<String,Object>> getGiftList();

    List<FuntimeGift> getGiftListByBestowed(Integer bestowed);

    List<Map<String,Object>> getGiftByUserId(Long userId);

    FuntimeGift selectByPrimaryKey(Integer id);

    List<Map<String,Object>> getGiftByKnapsack(Long userId);

}