package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeChatroomMapper {
    int deleteByPrimaryKey(Long id);

    FuntimeChatroom getRoomInfoById(Long id);

    Long checkRoomExists(Long id);

    int deleteByRoomId(Long id);

    int insertSelective(FuntimeChatroom record);

    FuntimeChatroom selectByPrimaryKey(Long id);

    FuntimeChatroom getRoomByUserId(Long userId);

    int updateByPrimaryKeySelective(FuntimeChatroom record);

    int updateChatroomState(@Param("id") Long id, @Param("state") Integer state);

    int updateChatroomBlock(@Param("id") Long id, @Param("isBlock") Integer isBlock);

    int updateChatroomBackgroundId(@Param("id") Long id, @Param("backgroundId") Integer backgroundId);

    int updateOnlineNumPlus(Long id);

    int updateOnlineNumSub(Long id);

    int updateOnlineNumTask();

    int insertUserRoomLog(Map<String, Object> map);

    List<Map<String, Object>> getRoomList(Integer tagId);

    List<Map<String, Object>> getRoomLogList(Long userId);



}