package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.entity.FuntimeChatroom;
import com.rzyou.funtime.entity.FuntimeChatroomMic;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.mapper.FuntimeChatroomMapper;
import com.rzyou.funtime.mapper.FuntimeChatroomMicMapper;
import com.rzyou.funtime.service.RoomService;
import com.rzyou.funtime.service.UserService;
import io.rong.RongCloud;
import io.rong.methods.chatroom.Chatroom;
import io.rong.models.chatroom.ChatroomModel;
import io.rong.models.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    UserService userService;

    @Autowired
    FuntimeChatroomMapper chatroomMapper;
    @Autowired
    FuntimeChatroomMicMapper chatroomMicMapper;

    @Override
    public void roomCreate(Long userId) {
        FuntimeUser user = userService.queryUserById(userId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        Long roomId = saveChatroom(userId);

        saveMic(roomId,8);

        ResponseResult result = saveRongyunRoom(roomId.toString(),user.getNickname());

        if (!result.getCode().equals(200)){
            throw new BusinessException(result.getCode().toString(),result.getErrorMessage());
        }
    }

    public Long saveChatroom(Long userId){

        FuntimeChatroom chatroom = new FuntimeChatroom();
        chatroom.setUserId(userId);
        int k = chatroomMapper.insertSelective(chatroom);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        return chatroom.getId();
    }

    public void saveMic(Long roomId,int num){
        List<FuntimeChatroomMic> mics = new ArrayList<>();
        FuntimeChatroomMic chatroomMic ;
        for (int i=0;i<num;i++){
            chatroomMic = new FuntimeChatroomMic();
            chatroomMic.setRoomId(roomId);
            chatroomMic.setMicLocation(i+1);
            mics.add(chatroomMic);
        }
        chatroomMicMapper.insertBatch(mics);

    }

    private ResponseResult saveRongyunRoom(String roomId,String roomName) {
        RongCloud rongCloud = RongCloud.getInstance(Constant.RONGYUN_APPKEY, Constant.RONGYUN_APPSECRET);
        //自定义 api地址方式
        //RongCloud rongCloud = RongCloud.getInstance(appKey, appSecret,api);

        Chatroom chatroom = rongCloud.chatroom;
        ChatroomModel[] chatrooms = {
                new ChatroomModel().setId(roomId).setName(roomName)
        };
        ResponseResult result ;
        try {
            result = chatroom.create(chatrooms);
        } catch (Exception e) {
            throw new BusinessException(ErrorMsgEnum.ROOM_RONGYUN_CREATE_ERROR.getValue(),ErrorMsgEnum.ROOM_RONGYUN_CREATE_ERROR.getDesc());
        }

        log.info("createRoom result:  " + result.toString());
        return result;
    }
}
