package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroomMic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeChatroomMicMapper {
    Long checkUserIsInMic(Long userId);

    int deleteByPrimaryKey(Long id);

    int lowerWheat(Long id);

    int lowerWheatWithRole(Long id);

    int upperWheat(@Param("id") Long id,@Param("userId") Long userId);

    int stopWheat(Long id);

    int forbidWheat(Long id);

    Integer getMicLocationUserRole(@Param("roomId") Long roomId, @Param("userId") Long userId);

    Long getMicLocationId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 获取用户麦位
     * @param roomId
     * @param userId
     * @return
     */
    Integer getMicLocation(@Param("roomId") Long roomId, @Param("userId") Long userId);

    FuntimeChatroomMic getMicLocationUser(@Param("roomId") Long roomId, @Param("micLocation") Integer micLocation);

    int insertBatch(@Param("mics") List<FuntimeChatroomMic> mics);

    List<Map<String, Object>> getMicUserByRoomId(Long roomId);

    int insertSelective(FuntimeChatroomMic record);

    FuntimeChatroomMic selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuntimeChatroomMic record);

    int updateByPrimaryKey(FuntimeChatroomMic record);

    int openWheat(Long id);

    int releaseWheat(Long id);

    int deleteByRoomId(Long roomId);

    int roomManage(Long id);

    int roomManageCancel(Long id);
}