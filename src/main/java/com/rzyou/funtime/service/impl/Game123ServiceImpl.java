package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.entity.FuntimeChatroom;
import com.rzyou.funtime.mapper.FuntimeRoomGame123Mapper;
import com.rzyou.funtime.service.Game123Service;
import com.rzyou.funtime.service.NoticeService;
import com.rzyou.funtime.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class Game123ServiceImpl implements Game123Service {

    @Autowired
    FuntimeRoomGame123Mapper roomGame123Mapper;
    @Autowired
    RoomService roomService;
    @Autowired
    NoticeService noticeService;


    @Override
    public Long getUserByRoomId(Long roomId) {
        return roomGame123Mapper.getUserByRoomId(roomId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void startGame(Long userId, Long roomId) {
        Long createUserId = getUserByRoomId(roomId);
        if (createUserId!=null){
            throw new BusinessException(ErrorMsgEnum.ROOM_GAME123_EXISTS.getValue(),ErrorMsgEnum.ROOM_GAME123_EXISTS.getDesc());
        }
        int k = roomGame123Mapper.insertRoomGame123(roomId,userId,24);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void clearVal(Long userId, Long roomId) {
        Long createUserId = getUserByRoomId(roomId);
        if (createUserId!=null){
            throw new BusinessException(ErrorMsgEnum.ROOM_GAME123_EXISTS.getValue(),ErrorMsgEnum.ROOM_GAME123_EXISTS.getDesc());
        }
        int k = roomGame123Mapper.deleteGame2(roomId);
        if (k<1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice30002(userIds);
        }

    }

    public void clearVal(Long roomId){
        roomGame123Mapper.deleteGame2(roomId);

        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice30002(userIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void exitGame(Long roomId) {
        roomGame123Mapper.deleteGame(roomId);
        roomGame123Mapper.deleteGame2(roomId);
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice30001(userIds);
        }
    }

    @Override
    public void exitGameForRoomClose(Long roomId) {
        roomGame123Mapper.deleteGame(roomId);
        roomGame123Mapper.deleteGame2(roomId);
    }
    @Override
    public void setExitTimeByExit(Long userId, Long roomId){
        Long createUserId = getUserByRoomId(roomId);
        if (createUserId.equals(userId)){

            roomGame123Mapper.updateExitTime(roomId);
        }
    }
    @Override
    public void setExitTimeByJoin(Long userId, Long roomId){
        Long createUserId = getUserByRoomId(roomId);
        if (createUserId.equals(userId)){

            roomGame123Mapper.updateExitTimeNull(roomId);
        }
    }

    @Override
    public void saveGame123Val(List<Long> userIds,Long roomId,Integer blueAmount){
        Long createUserId = getUserByRoomId(roomId);
        if (createUserId!=null&&userIds.size()>0) {
            for (Long userId : userIds) {
                if (roomGame123Mapper.getUserIfExist(userId) == null) {
                    roomGame123Mapper.insertRoomGame123Val(roomId, userId, blueAmount);
                } else {
                    roomGame123Mapper.updateRoomGame123Val(userId, blueAmount);
                }
            }
        }
        roomService.sendRoomInfoNotice(roomId);
    }

    @Override
    public void saveGame123Val(Map<Long,Integer> userIdsMap, Long roomId){
        Long createUserId = getUserByRoomId(roomId);
        if (createUserId!=null&&!userIdsMap.isEmpty()) {
            for (Map.Entry<Long,Integer> entry : userIdsMap.entrySet()) {
                Long userId = entry.getKey();
                Integer blueAmount = entry.getValue();
                if (roomGame123Mapper.getUserIfExist(userId) == null) {
                    roomGame123Mapper.insertRoomGame123Val(roomId, userId, blueAmount);
                } else {
                    roomGame123Mapper.updateRoomGame123Val(userId, blueAmount);
                }
            }
        }
        roomService.sendRoomInfoNotice(roomId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void openGame(Long userId, Long roomId) {
        FuntimeChatroom chatroom = roomService.getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        if (roomService.getChatroomManager(roomId,userId)==null&&userId.equals(chatroom.getUserId())){
            throw new BusinessException(ErrorMsgEnum.ROOM_GAME123_AUTH.getValue(),ErrorMsgEnum.ROOM_GAME123_AUTH.getDesc());
        }
        if (userId.equals(chatroom.getUserId())){
            roomGame123Mapper.deleteGame(roomId);
            roomGame123Mapper.deleteGame2(roomId);
        }else{
            Long createUserId = getUserByRoomId(roomId);
            if (createUserId != null){
                if (createUserId.equals(userId)){
                    roomGame123Mapper.deleteGame(roomId);
                    roomGame123Mapper.deleteGame2(roomId);
                }else{
                    throw new BusinessException(ErrorMsgEnum.ROOM_GAME123_EXISTS.getValue(),ErrorMsgEnum.ROOM_GAME123_EXISTS.getDesc());
                }
            }
        }
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            noticeService.notice30000(userIds);
        }
    }

    @Override
    public void game123Task() {

        //24小时一轮的过期,清空数值
        List<Long> roomIds = roomGame123Mapper.getExpireGame1();

        if (roomIds!=null&&!roomIds.isEmpty()){
            for (Long roomId : roomIds){
                try {
                    clearVal(roomId);
                }catch (Exception e){
                    continue;
                }
            }
        }
        //3分钟内创建者退出过期,退出游戏
        roomIds = roomGame123Mapper.getExpireGame2();
        if (roomIds!=null&&!roomIds.isEmpty()){
            for (Long roomId : roomIds){
                try {
                    exitGame(roomId);
                }catch (Exception e){
                    continue;
                }
            }
        }


    }

}
