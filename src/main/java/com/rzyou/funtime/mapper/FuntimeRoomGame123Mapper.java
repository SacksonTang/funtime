package com.rzyou.funtime.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FuntimeRoomGame123Mapper {

    Long getUserIfExist(Long userId);

    Long getUserByRoomId(Long roomId);

    Integer getStateByRoomId(Long roomId);

    List<Long> getExpireGame1();

    List<Long> getExpireGame2();

    int insertRoomGame123(@Param("roomId") Long roomId,@Param("userId")  Long userId, @Param("state")  Integer state);

    int insertRoomGame123Val(@Param("roomId") Long roomId,@Param("userId")  Long userId, @Param("blueAmount") Integer blueAmount);

    int updateRoomGame123Val(@Param("userId")  Long userId, @Param("blueAmount") Integer blueAmount);

    int deleteGame(Long roomId);

    int deleteGame2(Long roomId);

    int deleteGameByUserId(Long userId);


    int updateExitTime(Long roomId);

    int startGame(@Param("roomId") Long roomId,@Param("hours") Integer hours);

    int updateExitTimeNull(Long roomId);

    int updateState(@Param("roomId") Long roomId,Integer state);
}
