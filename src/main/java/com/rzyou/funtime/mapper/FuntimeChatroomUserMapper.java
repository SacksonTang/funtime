package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroomUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeChatroomUserMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(FuntimeChatroomUser record);

    int insertForRoomJoin(FuntimeChatroomUser record);

    FuntimeChatroomUser selectByPrimaryKey(Long id);

    /**
     * 待加入腾讯聊天室的房间编号
     * @return
     */
    List<String> getJoinRoomUser();
    /**
     * 待删除腾讯聊天室用户的房间编号
     * @return
     */
    List<String> getDeleteRoomUser();

    /**
     * 待加入腾讯聊天室的用户
     * @param roomNo
     * @return
     */
    List<Long> getJoinRoomUserByRoomNo(String roomNo);

    /**
     * 待删除腾讯聊天室用户
     * @param roomNo
     * @return
     */
    List<String> getDeleteRoomUserByRoomNo(String roomNo);

    /**
     * 获取全部腾讯聊天室
     * @return
     */
    List<String> getAllRoomUser();


    Long checkUserIsExist(@Param("roomId") Long roomId, @Param("userId") Long userId);

    String getRoomNoByRoomIdAndUser(@Param("roomId") Long roomId, @Param("userId") Long userId);

    List<Map<String,Object>> getRoomNoByRoomId(Long roomId);

    List<String> getRoomNoByRoomIdAll(Long roomId);

    int deleteFlagById(Long id);

    int deleteRoomUser(String roomNo);

    int updateRoomNoByRoomId(@Param("roomNo") String roomNo,@Param("userId") Long userId);

    int updateSyncByRoomNo(@Param("roomNo") String roomNo,@Param("userId") Long userId);

    int deleteByRoomId(Long roomId);

    List<Map<String, Object>> getRoomUserById(@Param("roomId") Long roomId,@Param("nickname") String nickname);

    /**
     * 查询房间用户
     * @param roomId
     * @return
     */
    List<Long> getRoomUserByRoomId(Long roomId);
}