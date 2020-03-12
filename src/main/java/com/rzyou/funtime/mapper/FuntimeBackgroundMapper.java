package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserBackground;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeBackgroundMapper {

    List<Map<String, Object>> getBackgroundList(Long userId);

    Map<String, Object> getBackgroundInfoById(@Param("id") Integer id,@Param("userId") Long userId);

    int insertUserBackgroundRecord(FuntimeUserBackground userBackground);

    int insertUserBackground(FuntimeUserBackground userBackground);

    int updateUserBackground(FuntimeUserBackground userBackground);
}