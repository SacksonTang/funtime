package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeNotice;
import com.rzyou.funtime.entity.RoomGiftNotice;

import java.util.List;

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
     * 全服发送
     * @param userSig
     * @param data
     * @param id
     */
    void snedAllAppNotice(String userSig, String data, Long id);


    /**
     * 抱麦通知
     * @param micLocation
     * @param roomId
     * @param userId
     * @param roomNo
     */
    void notice15(Integer micLocation,Long roomId,Long userId,String roomNo);

    /**
     * 上麦通知
     * @param micLocation
     * @param roomId
     * @param micUserId
     * @param nickname
     * @param portraitAddress
     * @param roomNo
     */
    void notice1(Integer micLocation, Long roomId, Long micUserId, String nickname, String portraitAddress, String roomNo);

    /**
     * 下麦通知
     * @param micLocation
     * @param roomId
     * @param micUserId
     * @param nickname
     * @param roomNo
     * @param isMe
     */
    void notice2(Integer micLocation, Long roomId, Long micUserId, String nickname, String roomNo, int isMe);

    /**
     * 封麦
     * @param micLocation
     * @param roomId
     * @param roomNo
     */
    void notice3(Integer micLocation, Long roomId, String roomNo);

    /**
     * 解封
     * @param micLocation
     * @param roomId
     * @param roomNo
     */
    void notice4(Integer micLocation, Long roomId, String roomNo);

    /**
     * 禁麦
     * @param micLocation
     * @param roomId
     * @param roomNo
     */
    void notice5(Integer micLocation, Long roomId, String roomNo);

    /**
     * 解禁
     * @param micLocation
     * @param roomId
     * @param roomNo
     */
    void notice6(Integer micLocation, Long roomId, String roomNo);

    /**
     * 解散房间
     * @param roomId
     * @param roomNo1
     */
    void notice7(Long roomId, String roomNo1);

    /**
     * 房间送礼物
     * @param notice
     * @param roomNo
     */
    void notice8(RoomGiftNotice notice, String roomNo);

    /**
     *
     * @param notice
     */
    void notice9(RoomGiftNotice notice);

    /**
     * 进入房间
     * @param roomId
     * @param userId
     * @param nickname
     * @param roomNo1
     */
    void notice12(Long roomId, Long userId, String nickname, String roomNo1);

    /**
     * 发红包
     * @param roomId
     * @param roomNo
     */
    void notice13(Long roomId, String roomNo);

    /**
     * 踢人
     * @param micLocation
     * @param roomId
     * @param kickIdUserId
     * @param roomNo
     */
    void notice16(Integer micLocation, Long roomId, Long kickIdUserId, String roomNo);

    /**
     * 设为主持
     * @param micLocation
     * @param roomId
     * @param roomNo
     * @param micUserId
     * @param nickname
     */
    void notice17(Integer micLocation, Long roomId, String roomNo, Long micUserId, String nickname);

    /**
     * 取消主持
     * @param micLocation
     * @param roomId
     * @param roomNo
     * @param micUserId
     * @param nickname
     */
    void notice18(Integer micLocation, Long roomId, String roomNo, Long micUserId, String nickname);

    /**
     * 抽麦序
     * @param micLocation
     * @param roomId
     * @param roomNo
     * @param micUserId
     * @param nickname
     * @param mic
     */
    void notice10(Integer micLocation, Long roomId, String roomNo, Long micUserId, String nickname, int mic);

    /**
     * 发生房间公屏消息
     * @param userId
     * @param imgUrl
     * @param msg
     * @param roomId
     * @param type
     */
    void notice11Or14(Long userId, String imgUrl, String msg, Long roomId, Integer type,List<String> roomNos);
}
