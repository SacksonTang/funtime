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

    void syncTencent(String usersig);

    void stopWheat(Long userId, Long roomId, Integer micLocation);

    void forbidWheat(Long roomId, Integer micLocation);

    void openWheat(Long userId, Long roomId, Integer micLocation);

    void releaseWheat(Long roomId, Integer micLocation);

    void roomClose(Long userId, Long roomId);
}
