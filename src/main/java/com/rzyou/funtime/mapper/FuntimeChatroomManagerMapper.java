package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeChatroomManager;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FuntimeChatroomManagerMapper {

    Long getChatroomManager(@Param("roomId") Long roomId, @Param("userId") Long userId);

    List<Map<String,Object>> getDurationConfs();

    Integer getDurationConfById(Integer id);

    FuntimeChatroomManager getChatroomManagerById(Long id);

    List<FuntimeChatroomManager> getChatroomManagerTask();

    int insertChatroomManager(FuntimeChatroomManager chatroomManager);

    int updateChatroomManager(FuntimeChatroomManager chatroomManager);

    int insertChatroomManagerRecord(FuntimeChatroomManager chatroomManager);

    int deleteChatroomManagerTask(Long id);


    List<Map<String, Object>> getRoomManagerList(Long roomId);

    int delRoomManager(Long id);
}
