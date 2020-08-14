package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeUserAccountCarRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeCarMapper {

    Integer getUserCarIdByUserId(Long userId);

    Map<String,Object> getCarInfoById(Integer id);

    Map<String,Object> getCarInfoByCarId(Integer carId);

    int insertUserCar(Map<String, Object> map);

    int updateUserCar(Map<String, Object> map);

    Long getUserCarById(@Param("userId") Long userId,@Param("carId") Integer carId);

    Integer getShowCountsById(@Param("userId") Long userId,@Param("carId") Integer carId);

    int insertShowcarRecord(@Param("userId") Long userId,@Param("carId") Integer carId);

    List<Map<String,Object>> getCarInfoForExpire();

    int deleteUserCarById(Long id);

    List<Map<String, Object>> getUserCarByUserId(Long userId);

    List<Map<String, Object>> getCarList(Long userId);

    List<Map<String, Object>> getPriceTagByCarNumber(Integer carNumber);

    int insertCarRecord(FuntimeUserAccountCarRecord record);

}
