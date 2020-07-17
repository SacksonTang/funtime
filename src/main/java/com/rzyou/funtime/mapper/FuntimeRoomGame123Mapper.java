package com.rzyou.funtime.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeRoomGame123Mapper {

    Long getUserIfExist(Long userId);

    Long getUserByRoomId(Long roomId);

    List<Long> getExpireGame1();

    List<Long> getExpireGame2();

    int insertRoomGame123(@Param("roomId") Long roomId,@Param("userId")  Long userId, @Param("expireHours") Integer expireHours);

    int insertRoomGame123Val(@Param("roomId") Long roomId,@Param("userId")  Long userId, @Param("blueAmount") Integer blueAmount);

    int updateRoomGame123Val(@Param("userId")  Long userId, @Param("blueAmount") Integer blueAmount);

    int deleteGame(Long roomId);

    int deleteGameById(Long id);

    int deleteGame2(Long roomId);

    int updateExitTime(Long roomId);

    int updateExitTimeNull(Long roomId);
}
