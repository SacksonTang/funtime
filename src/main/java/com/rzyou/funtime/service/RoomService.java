package com.rzyou.funtime.service;

import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.entity.FuntimeChatroom;
import com.rzyou.funtime.entity.FuntimeGift;

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
     * @param type
     * @return
     */
    boolean roomJoin(Long userId, Long roomId, String password, Integer type);

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
     */
    void roomExit(Long userId, Long roomId);

    /**
     * 踢人
     * @param kickIdUserId
     * @param userId
     * @param roomId
     */
    void roomKicked(Long kickIdUserId, Long userId, Long roomId);

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
     * @param micUserId
     */
    void lowerWheat(Long userId, Long roomId, Long micUserId);

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
     * 获取聊天室用户信息
     * @param startPage
     * @param pageSize
     * @param roomId
     * @param nickname
     * @return
     */
    PageInfo<Map<String, Object>> getRoomUserByIdAll(Integer startPage,Integer pageSize,Long roomId,String nickname);

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
     * @param userId
     * @param micUserId
     */
    void roomManage(Long roomId, Long userId, Long micUserId);

    /**
     * 取消主持
     * @param roomId
     * @param userId
     * @param micUserId
     */
    void roomManageCancel(Long roomId,  Long userId, Long micUserId);

    /**
     * 抽麦序
     * @param roomId
     * @param userId
     * @param micUserId
     */
    int roomRandomMic(Long roomId, Long userId, Long micUserId);

    /**
     * 公屏消息
     * @param userId
     * @param imgUrl
     * @param msg
     * @param roomId
     * @param type
     */
    void sendNotice(Long userId, String imgUrl, String msg, Long roomId, Integer type);

    /**
     * 我的足迹
     * @param startPage
     * @param pageSize
     * @param userId
     * @return
     */
    PageInfo<Map<String, Object>> getRoomLogList(Integer startPage, Integer pageSize, Long userId);

    /**
     * 获取参与赠送的礼物
     * @param bestowed
     * @return
     */
    List<FuntimeGift> getGiftListByBestowed(Integer bestowed);

    /**
     * 获取所有礼物
     * @return
     */
    Map<String,Object> getGiftList();

    /**
     * 查询房间用户
     * @param roomId
     * @return
     */
    List<Long> getRoomUserByRoomId(Long roomId);

    /**
     * 获取全部腾讯聊天室用户
     * @return
     */
    List<String> getAllRoomUser();

    /**
     * 用戶房間角色
     * @param roomId
     * @param userId
     * @return
     */
    Integer getUserRole(Long roomId,Long userId);

    /**
     * 获取用户房间
     * @param userId
     * @return
     */
    FuntimeChatroom getRoomByUserId(Long userId);

    /**
     * 用户是否在房间
     * @param roomId
     * @param userId
     * @return
     */
    boolean checkUserIsExist(Long roomId,Long userId);

    /**
     *
     * @param userId
     * @return
     */
    Long checkUserIsInRoom(Long userId);
}
