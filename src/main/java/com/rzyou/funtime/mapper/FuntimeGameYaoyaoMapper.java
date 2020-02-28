package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeGameYaoyaoConf;
import com.rzyou.funtime.entity.FuntimeGameYaoyaoPool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 2020/2/28
 * LLP-LX
 */
@Mapper
public interface FuntimeGameYaoyaoMapper {

    List<FuntimeGameYaoyaoConf> getYaoyaoConf();

    List<FuntimeGameYaoyaoPool> getYaoyaoPool(Integer type);

    FuntimeGameYaoyaoPool getPoolInfoById(int id);

    int updateActualPoolForPlus(@Param("id") Integer id, @Param("amount") Integer amount);

    int updateActualPoolForSub(@Param("id") Integer id, @Param("amount") Integer amount);
}
