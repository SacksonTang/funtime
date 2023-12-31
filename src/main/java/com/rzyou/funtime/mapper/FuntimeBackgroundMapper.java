package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserBackground;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeBackgroundMapper {

    Map<String, Object> getBackgroundThumbnailById(Integer id);

    List<Map<String, Long>> getBackgroundForExpiry();

    Integer getBackgroundIdForType1();

    Map<String, Object> getBackgroundUrlForType1();

    List<Map<String, Object>> getBackgroundList(Long userId);

    Map<String, Object> getBackgroundInfoById(@Param("id") Integer id,@Param("userId") Long userId);

    Map<String, Object> getBackgroundUrlById(@Param("id") Integer id,@Param("userId") Long userId);

    int insertUserBackgroundRecord(FuntimeUserBackground userBackground);

    int insertUserBackground(FuntimeUserBackground userBackground);

    int updateUserBackground(FuntimeUserBackground userBackground);

    int deleteUserBackgroundById(Long id);

    Integer getBackgroundDaysById(Integer id);
}