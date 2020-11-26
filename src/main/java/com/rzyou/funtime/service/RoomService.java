package com.rzyou.funtime.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.entity.FuntimeChatroom;
import com.rzyou.funtime.entity.FuntimeGift;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface RoomService {

    /**
     * 房间用户角色
     * @param roomId
     * @param userId
     * @return
     */
    Integer getUserRole2(Long roomId,Long userId);
    /**
     * 创建房间
     * @param userId
     * @param platform
     * @return
     */
    JSONObject roomCreate(Long userId, Integer platform);

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
     * @param priceId
     * @return
     */
    ResultMsg<Object> roomJoin(Long userId, Long roomId, String password, Integer type, Integer priceId);

    /**
     * 获取房间信息
     * @param roomId
     * @param userId
     * @return
     */
    Map<String, Object> getRoomInfo(Long roomId, Long userId);

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
     * 获取聊天室列表
     * @param startPage
     * @param pageSize
     * @param tagId
     * @return
     */
    PageInfo<Map<String,Object>> getRoomList2(Integer startPage, Integer pageSize, Integer tagId);

    /**
     * 获取聊天室用户信息
     * @param startPage
     * @param pageSize
     * @param roomId
     * @param nickname
     * @param userId
     * @return
     */
    PageInfo<Map<String, Object>> getRoomUserById(Integer startPage, Integer pageSize, Long roomId, String nickname, Long userId);
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
     * 获取聊天室用户信息
     * @param startPage
     * @param pageSize
     * @param roomId
     * @param userId
     * @return
     */
    PageInfo<Map<String, Object>> getRoomUserByIdAll2(Integer startPage, Integer pageSize, Long roomId, Long userId);

    /**
     * 获取聊天室对应腾讯聊天室的编号
     * @param roomId
     * @return
     */
    List<String> getRoomUserByRoomIdAll(Long roomId);

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
     * @param playLenth
     */
    void sendNotice(Long userId, String imgUrl, String msg, Long roomId, Integer type, Integer playLenth);

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
     * @param userId
     * @return
     */
    List<Long> getRoomUserByRoomId(Long roomId, Long userId);

    /**
     * 获取全部腾讯聊天室用户
     * @return
     */
    List<String> getAllRoomUser();

    /**
     * 获取符合等级的房间用户
     * @param level
     * @return
     */
    List<String>getAllRoomUserByLevel(Integer level);

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
     * 获取用户所在房间
     * @param userId
     * @return
     */
    Long getRoomIdByUserId(Long userId);

    /**
     * 用户是否在房间
     * @param userId
     * @return
     */
    Long checkUserIsInRoom(Long userId);

    /**
     * 用户是否在麦上
     * @param userId
     * @return
     */
    Long checkUserIsInMic(Long userId);

    /**
     * 房内麦上用户ID
     * @param roomId
     * @param userId
     * @return
     */
    List<Long> getMicUserIdByRoomId(Long roomId, Long userId);

    /**
     * 退出房间(定时任务)
     * @param userId
     */
    void roomExitTask(Long userId);

    /**
     * 下麦定时任务
     * @param userId
     */
    void roomMicLowerTask(Long userId);

    /**
     * 背景列表
     * @param startPage
     * @param pageSize
     * @param userId
     * @return
     */
    PageInfo<Map<String, Object>> getBackgroundList(Integer startPage, Integer pageSize, Long userId);

    /**
     * 购买背景图
     * @param backgroundId
     * @param userId
     * @param roomId
     * @return
     */
    ResultMsg<Object> buyBackground(Integer backgroundId, Long userId, Long roomId);

    /**
     * 游戏抽奖
     * @param backgroundId
     * @param userId
     */
    void drawBackground(Integer backgroundId, Long userId);

    /**
     * 设置背景
     * @param backgroundId
     * @param userId
     * @param roomId
     */
    void setBackground(Integer backgroundId, Long userId, Long roomId);

    /**
     * 定时设置过期资源
     */
    void setBackgroundTask();

    /**
     * 定时同步房间用户数量
     */
    void updateOnlineNumTask();


    /**
     * 发生麦位信息
     */
    void sendRoomMicInfoTask();

    /**
     * 发生麦位信息
     * @param roomId
     */
    void sendRoomInfoNotice(Long roomId);

    /**
     * 背景资源
     * @param id
     * @return
     */
    Map<String, Object> getBackgroundThumbnailById(Integer id);

    /**
     * 背景天数
     * @param id
     * @return
     */
    Integer getBackgroundDaysById(Integer id);

    /**
     * 礼物资源列表
     * @return
     */
    List<FuntimeGift> getGiftListInit();

    /**
     * 设置管理员
     * @param roomId
     * @param tagId
     * @param managerIds
     * @param userId
     */
    String setRoomManager(Long roomId, Integer tagId, String managerIds, Long userId);

    /**
     * 管理员列表
     * @param roomId
     * @param startPage
     * @param pageSize
     * @return
     */
    PageInfo<Map<String, Object>> getRoomManagerList(Long roomId, Integer startPage, Integer pageSize);

    /**
     * 房内管理员
     * @param roomId
     * @return
     */
    List<Long> getRoomManagerIds(Long roomId);

    /**
     * 时长配置
     * @return
     */
    List<Map<String, Object>> getDurationConfs();

    /**
     * 管理员过期
     */
    void deleteChatroomManagerTask();

    /**
     * 移除管理员
     * @param id
     */
    void delRoomManager(Long id);

    /**
     * 查询管理员
     * @param roomId
     * @param userId
     * @return
     */
    Long getChatroomManager(Long roomId, Long userId);

    /**
     * 开启音乐权限
     * @param roomId
     * @param micLocation
     */
    void startMusicAuth(Long roomId, Integer micLocation);

    /**
     * 关闭音乐权限
     * @param roomId
     * @param micLocation
     */
    void cancelMusicAuth(Long roomId, Integer micLocation);

    /**
     * 定时清理0人房间
     */
    void roomCloseTask();

    /**
     * 房内有人的麦位
     * @param roomId
     * @return
     */
    List<Map<String, Object>> getMicInfoByRoomId(Long roomId);

    void updateHotsPlus(Long roomId, int hots);

    void updateHotsSub(Long roomId, int hots);

    /**
     * 定时重置热度
     */
    void resetRoomHotsTask();

    /**
     * 房间榜单
     * @param dateType
     * @param type
     * @param curUserId
     * @param roomId
     * @return
     */
    Map<String, Object> getRoomRankingList(Integer dateType, Integer type, String curUserId,Long roomId);

    /**
     * 炫耀座驾
     * @param userId
     * @param roomId
     * @param carNumber
     */
    void showCar(Long userId, Long roomId, Integer carNumber);

    /**
     * 房间流水
     * @param startDate
     * @param endDate
     * @param roomId
     * @return
     */
    Map<String, Object> getRoomStatement(String startDate, String endDate, Long roomId);

    /**
     * 获取首页推荐房间
     * @return
     */
    Long getInvitationRoomId();

    /**
     * 打开公屏
     * @param roomId
     * @param userId
     */
    void openScreen(Long roomId, Long userId);

    /**
     * 关闭公屏
     * @param roomId
     * @param userId
     */
    void closeScreen(Long roomId, Long userId);

    /**
     * 推荐位
     * @return
     */
    Map<String,Object> getRecommendRoomList();

    /**
     * 打开房间排行榜
     * @param roomId
     * @param userId
     */
    void openRoomRank(Long roomId, Long userId);

    /**
     * 关闭房间排行
     * @param roomId
     * @param userId
     */
    void closeRoomRank(Long roomId, Long userId);

    /**
     * 房间内头像
     * @param roomId
     * @return
     */
    List<String> getRoomUserByRoomIdAll2(Long roomId);

    /**
     * 下单礼物
     * @return
     */
    List<FuntimeGift> getGiftListByOrder();

    /**
     * 获取匹配价格
     * @param userId
     */
    Map<String,Object> get1v1price(Long userId);

    /**
     * 随机匹配
     * @param userId
     * @param priceId
     * @return
     */
    ResultMsg<Object> doMatch(Long userId, Integer priceId);

    /**
     * 随机匹配任务
     */
    void doMatchTask();

    /**
     * 取消匹配
     * @param userId
     * @param recordId
     */
    void cancelMatch(Long userId, Long recordId);
}
