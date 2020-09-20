package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroomMic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeChatroomMicMapper {

    Integer checkMicChange(Long roomId);

    List<String> getRoomUserByRoomIdAll2(Long roomId);

    List<Map<String, Object>> getMicInfoByRoomId(Long roomId);

    Long checkUserIsInMic(Long userId);

    FuntimeChatroomMic getRoomUserInfoByUserId(Long userId);

    int deleteByPrimaryKey(Long id);

    int lowerWheat(Long id);

    int upperWheat(@Param("id") Long id,@Param("userId") Long userId);

    int stopWheat(Long id);

    int forbidWheat(Long id);

    int startMusicAuth(Long id);

    int cancelMusicAuth(Long id);
    /**
     * 获取全部腾讯聊天室
     * @return
     */
    List<String> getAllRoomUser();

    List<String>getAllRoomUserByLevel(Integer level);


    FuntimeChatroomMic getMicLocationByRoomIdAndUser(@Param("roomId") Long roomId, @Param("userId") Long userId);

    FuntimeChatroomMic getInfoByRoomIdAndUser(@Param("roomId") Long roomId, @Param("userId") Long userId);

    List<Long> getMicUserIdByRoomId(@Param("roomId") Long roomId,@Param("userId") Long userId);

    List<Long> getRoomUserByRoomId(@Param("roomId") Long roomId,@Param("userId") Long userId);

    List<Map<String, Object>> getRoomUserById(@Param("roomId") Long roomId, @Param("nickname") String nickname,@Param("userId") Long userId);

    List<Map<String, Object>> getRoomUserByIdAll(@Param("roomId") Long roomId,@Param("nickname") String nickname);

    List<Map<String, Object>> getRoomUserByIdAll2(@Param("roomId") Long roomId,@Param("userId") Long userId);

    FuntimeChatroomMic getMicLocationUser(@Param("roomId") Long roomId, @Param("micLocation") Integer micLocation);

    List<String> getRoomUserByRoomIdAll(Long roomId);

    List<String> getRoomManagerByRoomId(Long roomId);

    int insertBatch(@Param("mics") List<FuntimeChatroomMic> mics);

    List<Map<String, Object>> getMicUserByRoomId(Long roomId);

    int insertSelective(FuntimeChatroomMic record);

    int updateByPrimaryKeySelective(FuntimeChatroomMic record);

    int updateByPrimaryKey(FuntimeChatroomMic record);

    int openWheat(Long id);

    int releaseWheat(Long id);

    int deleteByRoomId(Long roomId);

    int updateMicByRoomId(Long roomId);

    int roomManage(Long id);

    int roomManageCancel(Long id);
}