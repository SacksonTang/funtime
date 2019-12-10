package com.rzyou.funtime.service;

import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.entity.FuntimeChatroom;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface RoomService {
    /**
     * 创建房间
     * @param userId
     * @return
     */
    Long roomCreate(Long userId);

    /**
     * 设置房间
     * @param chatroom
     */
    void roomUpdate(FuntimeChatroom chatroom);

    /**
     * 加入房间
     * @param userId
     * @param roomId
     * @param password
     * @return
     */
    boolean roomJoin(Long userId, Long roomId,String password);

    /**
     * 获取房间信息
     * @param roomId
     * @return
     */
    Map<String, Object> getRoomInfo(Long roomId);

    /**
     * 推出房间
     * @param userId
     * @param roomId
     * @param micLocation
     */
    void roomExit(Long userId, Long roomId, Integer micLocation);

    /**
     * 踢人
     * @param kickIdUserId
     * @param userId
     * @param roomId
     * @param micLocation
     */
    void roomKicked(Long kickIdUserId, Long userId, Long roomId, Integer micLocation);

    /**
     * 抱麦
     * @param userId
     * @param roomId
     * @param micLocation
     * @param micUserId
     * @throws ExecutionException
     * @throws InterruptedException
     */
    void holdWheat(Long userId, Long roomId, Integer micLocation, Long micUserId) throws ExecutionException, InterruptedException;

    /**
     * 上麦
     * @param userId
     * @param roomId
     * @param micLocation
     * @param micUserId
     */
    void upperWheat(Long userId, Long roomId, Integer micLocation, Long micUserId);

    /**
     * 下麦
     * @param userId
     * @param roomId
     * @param micLocation
     * @param micUserId
     */
    void lowerWheat(Long userId, Long roomId, Integer micLocation, Long micUserId);

    /**
     * 同步腾讯
     * @param usersig
     */
    void syncTencent(String usersig);

    /**
     * 封麦
     * @param userId
     * @param roomId
     * @param micLocation
     */
    void stopWheat(Long userId, Long roomId, Integer micLocation);

    /**
     * 禁麦
     * @param roomId
     * @param micLocation
     * @param userId
     */
    void forbidWheat(Long roomId, Integer micLocation, Long userId);

    /**
     * 解封
     * @param userId
     * @param roomId
     * @param micLocation
     */
    void openWheat(Long userId, Long roomId, Integer micLocation);

    /**
     * 解禁
     * @param roomId
     * @param micLocation
     * @param userId
     */
    void releaseWheat(Long roomId, Integer micLocation, Long userId);

    /**
     * 关闭房间
     * @param userId
     * @param roomId
     */
    void roomClose(Long userId, Long roomId);

    /**
     * 获取聊天室列表
     * @param startPage
     * @param pageSize
     * @param tagId
     * @return
     */
    PageInfo<Map<String,Object>> getRoomList(Integer startPage, Integer pageSize, Integer tagId);

    /**
     * 获取聊天室用户信息
     * @param startPage
     * @param pageSize
     * @param roomId
     * @param nickname
     * @return
     */
    PageInfo<Map<String, Object>> getRoomUserById(Integer startPage,Integer pageSize,Long roomId,String nickname);

    /**
     * 获取聊天室对应腾讯聊天室的编号
     * @param roomId
     * @return
     */
    List<String> getRoomNoByRoomIdAll(Long roomId);

    /**
     * 获取聊天室信息
     * @param roomId
     * @return
     */
    FuntimeChatroom getChatroomById(Long roomId);

    /**
     * 设为主持
     * @param roomId
     * @param micLocation
     * @param userId
     * @param micUserId
     */
    void roomManage(Long roomId, Integer micLocation, Long userId, Long micUserId);

    /**
     * 取消主持
     * @param roomId
     * @param micLocation
     * @param userId
     * @param micUserId
     */
    void roomManageCancel(Long roomId, Integer micLocation, Long userId, Long micUserId);

    /**
     * 抽麦序
     * @param roomId
     * @param micLocation
     * @param userId
     * @param micUserId
     */
    int roomRandomMic(Long roomId, Integer micLocation, Long userId, Long micUserId);

    /**
     * 公屏消息
     * @param userId
     * @param imgUrl
     * @param msg
     * @param roomId
     * @param type
     */
    void sendNotice(Long userId, String imgUrl, String msg, Long roomId, Integer type);
}
