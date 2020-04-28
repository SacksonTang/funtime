package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 2020/2/28
 * LLP-LX
 */
@Mapper
public interface FuntimeGameMapper {

    int updateYaoyaoPoolTask();

    int insertYaoyaoPoolHisotry(@Param("startDate") String startDate,@Param("endDate") String endDate);

    Integer getGameShowConf(@Param("type") Integer type,@Param("gameCode") Integer gameCode);

    Integer getGameShowConf2(@Param("myLevel") Integer myLevel,@Param("gameCode") Integer gameCode);

    List<Map<String,Object>> getGameList(@Param("myLevel") Integer myLevel,@Param("location") Integer location);

    int insertYaoyaoRecord(FuntimeUserAccountYaoyaoRecord record);

    List<FuntimeGameYaoyaoConf> getYaoyaoConf(Integer id);

    List<FuntimeGameYaoyaoPool> getYaoyaoPool(Integer type);

    FuntimeGameYaoyaoPool getPoolInfoById(Integer id);

    int updateActualPoolForPlus(@Param("id") Integer id, @Param("amount") Integer amount);

    int updateActualPoolForSub(@Param("id") Integer id, @Param("amount") Integer amount);

    List<FuntimeGameSmashEggConf> getSmashEggConfs(Integer type);

    int insertSmashEggRecord(FuntimeUserAccountSmashEggRecord record);

    int insertCircleRecord(FuntimeUserAccountCircleRecord record);

    List<FuntimeGameCircleConf> getCircleConfs();
}
