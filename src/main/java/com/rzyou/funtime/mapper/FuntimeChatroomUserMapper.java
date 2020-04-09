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

    FuntimeChatroomUser selectByPrimaryKey(Long id);

    FuntimeChatroomUser getRoomUserInfoByUserId(Long userId);

    /**
     * 获取全部腾讯聊天室
     * @return
     */
    List<String> getAllRoomUser();

    /**
     * 获取用户已有的房间
     * @param userId
     * @return
     */
    Long getRoomByUserId(Long userId);


    Long checkUserIsExist(@Param("roomId") Long roomId, @Param("userId") Long userId);

    List<String> getRoomUserByRoomIdAll(Long roomId);

    int updateUserRoleById(@Param("id") Long id,@Param("userRole") Integer userRole);

    int deleteByRoomId(Long roomId);

    List<Map<String, Object>> getRoomUserById(@Param("roomId") Long roomId,@Param("nickname") String nickname);

    List<Map<String, Object>> getRoomUserByIdAll(@Param("roomId") Long roomId,@Param("nickname") String nickname);

    /**
     * 查询房间用户
     * @param roomId
     * @param userId
     * @return
     */
    List<Long> getRoomUserByRoomId(@Param("roomId") Long roomId, @Param("userId") Long userId);
}