package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeChatroom;

import java.util.Map;

public interface RoomService {
    Long roomCreate(Long userId);

    void roomUpdate(FuntimeChatroom chatroom);

    boolean roomJoin(Long userId, Long roomId,String password);

    Map<String, Object> getRoomInfo(Long roomId);

    void roomExit(Long userId, Long roomId, Integer micLocation);

    void roomKicked(Long kickIdUserId, Long userId, Long roomId, Integer micLocation);


    void upperWheat(Long userId, Long roomId, Integer micLocation);

    void lowerWheat(Long userId, Long roomId, Integer micLocation);
}
