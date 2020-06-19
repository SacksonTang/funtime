package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeRoomGame21;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeRoomGame21Mapper {

    Long getUserByRoomAndMic(@Param("roomId") Long roomId,@Param("mic") Integer mic);

    Map<String,Object> getGameInfo(Long roomId);

    Integer getRoundsByRoomId(Long roomId);

    List<FuntimeRoomGame21> getTaskRoomId(long time);

    List<Map<String,Object>> getGameWinUserByRoomId(Long roomId);

    List<FuntimeRoomGame21> getGameInfoByRoomId(Long roomId);

    List<FuntimeRoomGame21> getGameInfoForStateByRoomId(Long roomId);

    FuntimeRoomGame21 getGameInfoByRoomIdAndMic(@Param("roomId") Long roomId,@Param("mic") Integer mic);

    int insertGame(@Param("roomId") Long roomId,@Param("taskTime") long taskTime);

    int updateGame(@Param("roomId") Long roomId, @Param("taskTime") long taskTime ,@Param("rounds") Integer rounds);

    int insertBatch(@Param("mics") List<FuntimeRoomGame21> mics);

    int deleteGame(Long roomId);

    int deleteGameById(Long id);

    int deleteGame2(Long roomId);

    int updateGameInfo(FuntimeRoomGame21 roomGame21);

}
