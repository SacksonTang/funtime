package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeNotice;
import com.rzyou.funtime.entity.RoomGiftNotice;

import java.util.List;
import java.util.Map;

public interface NoticeService {

    List<FuntimeNotice> getFailNotice(int sendType);

    /**
     * 群发
     * @param userSig
     * @param data
     * @param id
     */
    void sendGroupNotice(String userSig, String data, Long id);

    /**
     * 单发
     * @param userSig
     * @param data
     * @param id
     */
    void sendSingleNotice(String userSig, String data, Long id);

    /**
     * 单聊
     * @param userSig
     * @param data
     * @param id
     */
    void sendMsgNotice(String userSig, String data, Long id);
    /**
     * 全服发送
     * @param userSig
     * @param data
     * @param id
     */
    void sendAllAppNotice(String userSig, String data, Long id);

    /**
     * 全服房間发送
     * @param userSig
     * @param data
     * @param id
     */
    void snedAllRoomAppNotice(String userSig, String data, Long id);

    /**
     * 抱麦通知
     * @param micLocation
     * @param roomId
     * @param userId
     */
    void notice15(Integer micLocation,Long roomId,Long userId);

    /**
     * 上麦通知
     * @param micLocation
     * @param roomId
     * @param micUserId
     * @param nickname
     * @param portraitAddress
     * @param userIds
     * @param sex
     * @param levelUrl
     */
    void notice1(Integer micLocation, Long roomId, Long micUserId, String nickname, String portraitAddress, List<String> userIds, Integer sex, String levelUrl);

    /**
     * 下麦通知
     * @param micLocation
     * @param roomId
     * @param micUserId
     * @param nickname
     * @param userIds
     * @param isMe
     */
    void notice2(Integer micLocation, Long roomId, Long micUserId, String nickname, List<String> userIds, int isMe);

    /**
     * 封麦
     * @param micLocation
     * @param roomId
     * @param userIds
     */
    void notice3(Integer micLocation, Long roomId, List<String> userIds);

    /**
     * 解封
     * @param micLocation
     * @param roomId
     * @param userIds
     */
    void notice4(Integer micLocation, Long roomId, List<String> userIds);

    /**
     * 禁麦
     * @param micLocation
     * @param roomId
     * @param userIds
     */
    void notice5(Integer micLocation, Long roomId, List<String> userIds);

    /**
     * 解禁
     * @param micLocation
     * @param roomId
     * @param userIds
     */
    void notice6(Integer micLocation, Long roomId, List<String> userIds);

    /**
     * 解散房间
     * @param roomId
     * @param userIds
     */
    void notice7(Long roomId, List<String> userIds);

    /**
     * 房间送礼物
     * @param notice
     * @param userIds
     */
    void notice8(RoomGiftNotice notice, List<String> userIds);

    /**
     *全服房间通知礼物
     * @param notice
     */
    void notice9(RoomGiftNotice notice);

    /**
     * 进入房间
     * @param roomId
     * @param userId
     * @param nickname
     * @param userIds
     */
    void notice12(Long roomId, Long userId, String nickname, List<String> userIds);

    /**
     * 发红包
     * @param roomId
     * @param userIds
     * @param nickname
     */
    void notice13(Long roomId, List<String> userIds, String nickname);

    /**
     * 踢人
     * @param micLocation
     * @param roomId
     * @param kickIdUserId
     */
    void notice16(Integer micLocation, Long roomId, Long kickIdUserId);

    /**
     * 设为主持
     * @param micLocation
     * @param roomId
     * @param userIds
     * @param micUserId
     * @param nickname
     */
    void notice17(Integer micLocation, Long roomId, List<String> userIds, Long micUserId, String nickname);

    /**
     * 取消主持
     * @param micLocation
     * @param roomId
     * @param userIds
     * @param micUserId
     * @param nickname
     */
    void notice18(Integer micLocation, Long roomId, List<String> userIds, Long micUserId, String nickname);

    /**
     * 抽麦序
     * @param micLocation
     * @param roomId
     * @param userIds
     * @param micUserId
     * @param nickname
     * @param mic
     */
    void notice10(Integer micLocation, Long roomId, List<String> userIds, Long micUserId, String nickname, int mic);

    /**
     * 发生房间公屏消息
     * @param userId
     * @param imgUrl
     * @param msg
     * @param roomId
     * @param type
     * @param userRole
     */
    void notice11Or14(Long userId, String imgUrl, String msg, Long roomId, Integer type, List<String> userIds, Integer userRole);

    /**
     * 全房送普通礼物通知
     * @param notice
     * @param userIds
     */
    void notice19(RoomGiftNotice notice, List<String> userIds);

    /**
     * 更新房间人数通知
     * @param roomId
     * @param userIds
     */
    void notice20(Long roomId,List<String> userIds,Integer roomUserCount);

    /**
     * 全房送超级大礼通知
     * @param notice
     */
    void notice21(RoomGiftNotice notice);


    /**
     * 大喇叭全服发送
     * @param content
     * @param userId
     * @param roomId
     */
    void notice10001(String content, Long userId, Long roomId);

    /**
     * 用户等级变更
     * @param userId
     * @param roomId
     * @param levelUrl
     * @param nickname
     * @param portraitAddress
     * @param userIds
     */
    void notice25(Long userId, Long roomId, String levelUrl, String nickname, String portraitAddress, List<String> userIds);

    /**
     * 封禁用户
     * @param userId
     */
    void notice24(Long userId);

    /**
     * 封禁用户
     * @param roomId
     * @param userIds
     */
    void notice30(Long roomId, List<String> userIds);


    /**
     * 封禁房间
     * @param roomId
     * @param userIds
     */
    void notice23(Long roomId, List<String> userIds);

    /**
     * 红包开启
     */
    void notice26();

    /**
     * 红包关闭
     */
    void notice27();

    /**
     * 摇摇乐开启
     */
    void notice28();

    /**
     * 摇摇乐关闭
     */
    void notice29();

    /**
     * 设置背景
     * @param roomId
     * @param userId
     * @param backgroundUrl
     * @param userIds
     * @param backgroundUrl2
     */
    void notice31(Long roomId, Long userId, String backgroundUrl, List<String> userIds, String backgroundUrl2);

    /**
     * 刷新麦位信息
     * @param userIds
     * @param micUser
     * @param roomUserCount
     */
    void notice32(List<String> userIds, List<Map<String, Object>> micUser, int roomUserCount);

    /**
     * 系统通知
     * @param startPage
     * @param pageSie
     * @param userId
     * @return
     */
    Map<String, Object> getSystemNoticeList(Integer startPage, Integer pageSie, Long userId);

    /**
     * 通知已读
     * @param userId
     */
    void readNotice(Long userId);
}
