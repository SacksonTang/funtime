package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroom;
import org.apache.ibatis.annotations.Mapper;

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

    int updateByPrimaryKey(FuntimeChatroom record);

    int updateOnlineNumPlus(Long id);

    int updateOnlineNumSub(Long id);

    int insertUserRoomLog(Map<String, Object> map);

    List<Map<String, Object>> getRoomList(Integer tagId);

    List<Map<String, Object>> getRoomLogList(Long userId);



}