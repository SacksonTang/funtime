package com.rzyou.funtime.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface FuntimeCarMapper {
    Map<String,Object> getCarInfoById(Integer id);

    int insertUserCar(Map<String, Object> map);

    int updateUserCar(Map<String, Object> map);

    Long getUserCarIdBy(@Param("userId") Long userId,@Param("carId") Integer carId);
}
