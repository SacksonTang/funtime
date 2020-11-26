package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.OperationType;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.FuntimeNoticeMapper;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.NoticeService;
import com.rzyou.funtime.service.RoomService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.JsonUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class NoticeServerImpl implements NoticeService {

    @Autowired
    FuntimeNoticeMapper noticeMapper;
    @Autowired
    UserService userService;
    @Autowired
    RoomService roomService;
    @Autowired
    AccountService accountService;



    @Override
    public void sendGroupNotice(String userSig, String data, Long id) {
        boolean flag = TencentUtil.sendGroupMsg(userSig,data);
        if (flag){
            noticeMapper.updateState(id,1);
        }else{
            noticeMapper.updateState(id,2);
        }

    }

    @Override
    public void sendSingleNotice(String userSig, String data, Long id) {

        boolean flag = TencentUtil.sendGroupSystemNotification(userSig,data);
        if (flag){
            noticeMapper.updateState(id,1);
        }else{
            noticeMapper.updateState(id,2);
        }
    }

    @Override
    public void sendMsgNotice(String userSig, String data, Long id) {
        boolean flag = TencentUtil.sendMsg(userSig,data);
        if (flag){
            noticeMapper.updateState(id,1);
        }else{
            noticeMapper.updateState(id,2);
        }
    }

    public void sendRoomUserNotice(String userSig, String data, List<String> toAccounts) {
        if (toAccounts==null||toAccounts.size()<1){
            return;
        }
        JSONArray array;
        if (toAccounts.size()<=300) {
            array = TencentUtil.batchsendmsg(userSig,toAccounts,data);
            if(array != null){
                List<String> users = getUserIds(array);
                if (!users.isEmpty()) {
                    TencentUtil.batchsendmsg(userSig, users, data);
                }
            }

        }else{
            int size = toAccounts.size();
            int fromIndex = 0;
            int toIndex = 300;
            int temp = 300;
            int k = size%toIndex == 0?size/toIndex:size/toIndex+1;
            for (int j = 1;j<k+1;j++){
                List<String> spList = toAccounts.subList(fromIndex,toIndex);
                fromIndex = toIndex;
                toIndex =  Math.min((j+1)*temp,size) ;
                array = TencentUtil.batchsendmsg(userSig,spList,data);
                if(array != null){
                    List<String> users = getUserIds(array);
                    if (!users.isEmpty()) {
                        TencentUtil.batchsendmsg(userSig, users, data);
                    }
                }
                if (toIndex>size){
                    break;
                }
            }
        }
    }

    public List<String> getUserIds(JSONArray array){
        JSONObject object;
        List<String> users = new ArrayList<>();
        for (int i = 0;i<array.size();i++){
            object = array.getJSONObject(i);
            users.add(object.getString("To_Account"));
        }
        return users;
    }

    @Override
    public void sendAllAppNotice(String userSig, String data, Long id) {
        List<String> list = userService.getAllUserId();
        JSONArray array;
        if (list!=null&&list.size()<=300) {
            array = TencentUtil.batchsendmsg(userSig,list,data);
            if(array == null){
                noticeMapper.updateState(id,1);
            }else{
                List<String> users = getUserIds(array);
                array = TencentUtil.batchsendmsg(userSig,users,data);
                if (array == null) {
                    noticeMapper.updateState(id, 1);
                }else{
                    noticeMapper.updateState(id, 4);
                }
            }

        }else{
            int size = list.size();
            int fromIndex = 0;
            int toIndex = 300;
            int temp = 300;
            int k = size%toIndex == 0?size/toIndex:size/toIndex+1;
            for (int j = 1;j<k+1;j++){
                List<String> spList = list.subList(fromIndex,toIndex);
                fromIndex = toIndex;
                toIndex =  Math.min((j+1)*temp,size) ;
                array = TencentUtil.batchsendmsg(userSig,spList,data);
                if(array == null){
                    noticeMapper.updateState(id,1);
                }else{
                    List<String> users = getUserIds(array);
                    array = TencentUtil.batchsendmsg(userSig,users,data);
                    if (array == null) {
                        noticeMapper.updateState(id, 1);
                    }else{
                        noticeMapper.updateState(id, 4);
                    }
                }
                if (toIndex>size){
                    break;
                }
            }
        }
    }

    @Override
    public void snedAllRoomAppNotice(String userSig, String data, Long id) {

        List<String> list = roomService.getAllRoomUser();
        JSONArray array;
        if (list!=null&&list.size()<=300) {
            array = TencentUtil.batchsendmsg(userSig,list,data);
            if(array == null){
                noticeMapper.updateState(id,1);
            }else{
                List<String> users = getUserIds(array);
                array = TencentUtil.batchsendmsg(userSig,users,data);
                if (array == null) {
                    noticeMapper.updateState(id, 1);
                }else{
                    noticeMapper.updateState(id, 4);
                }
            }

        }else{
            int size = list.size();
            int fromIndex = 0;
            int toIndex = 300;
            int temp = 300;
            int k = size%toIndex == 0?size/toIndex:size/toIndex+1;
            for (int j = 1;j<k+1;j++){
                List<String> spList = list.subList(fromIndex,toIndex);
                fromIndex = toIndex;
                toIndex =  Math.min((j+1)*temp,size) ;
                array = TencentUtil.batchsendmsg(userSig,spList,data);
                if(array == null){
                    noticeMapper.updateState(id,1);
                }else{
                    List<String> users = getUserIds(array);
                    array = TencentUtil.batchsendmsg(userSig,users,data);
                    if (array == null) {
                        noticeMapper.updateState(id, 1);
                    }else{
                        noticeMapper.updateState(id, 4);
                    }
                }
                if (toIndex>size){
                    break;
                }
            }
        }


    }


    @Override
    public List<FuntimeNotice> getFailNotice(int sendType) {
        if (sendType == 2){
            return noticeMapper.getSingleFailNotice();
        }else if (sendType == 3){
            return noticeMapper.getAllRoomFailNotice();
        }else if (sendType == 4){
            return noticeMapper.getAllFailNotice();
        }else{
            return noticeMapper.getSingleFailNoticeNoRoom();
        }

    }

    @Override
    public void notice15(Integer micLocation,Long roomId,Long userId) {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_MIC_HOLDING);
        List<String> toAccounts = new ArrayList<>();
        toAccounts.add(String.valueOf(userId));
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        sendRoomUserNotice(userSig,data,toAccounts);


    }

    @Override
    public void notice1(Integer micLocation, Long roomId, Long micUserId, String nickname, String portraitAddress, List<String> userIds, Integer sex, String levelUrl) {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("uid",micUserId);
        object.put("name",nickname);
        object.put("sex",sex);
        object.put("imgUrl",portraitAddress);
        object.put("levelUrl",levelUrl);
        object.put("type",Constant.ROOM_MIC_UPPER);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice2(Integer micLocation, Long roomId, Long micUserId, String nickname, List<String> userIds, int isMe) {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("uid",micUserId);
        object.put("isMe",isMe);
        object.put("name",nickname);
        object.put("type",Constant.ROOM_MIC_LOWER);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        sendRoomUserNotice(userSig,data,userIds);

    }

    @Override
    public void notice3(Integer micLocation, Long roomId, List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_MIC_STOP);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice4(Integer micLocation, Long roomId,List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_MIC_OPEN);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice5(Integer micLocation, Long roomId, List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_MIC_FORBID);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice6(Integer micLocation, Long roomId, List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_MIC_RELEASE);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice7(Long roomId, List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_CLOSE);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice8(RoomGiftNotice notice, List<String> userIds) {
        String object = JSONObject.toJSONString(notice);
        String data = StringEscapeUtils.unescapeJava(object);
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice9(RoomGiftNotice notice) {
        String object = JSONObject.toJSONString(notice);
        String parameterHandler = StringEscapeUtils.unescapeJava(object);
        saveNotice(Constant.ROOM_GIFT_SEND_ALL, parameterHandler,0);

    }

    @Override
    public void notice12(Long roomId, Long userId, String nickname, List<String> userIds, String carUrl, String msg, String animationType) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",nickname);
        object.put("carUrl",carUrl);
        object.put("msg",msg);
        object.put("animationType",animationType);
        object.put("type",Constant.ROOM_ENTER);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice13(Long roomId, List<String> userIds, String nickname) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_REDPACKET_SEND);
        object.put("name",nickname);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice16(Integer micLocation, Long roomId, Long kickIdUserId) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_KICKED);
        List<String> toAccounts = new ArrayList<>();
        toAccounts.add(String.valueOf(kickIdUserId));
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,toAccounts);
    }

    @Override
    public void notice17(Integer micLocation, Long roomId, List<String> userIds, Long micUserId, String nickname) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",micUserId);
        object.put("name",nickname);
        object.put("type",Constant.ROOM_MANAGE);
        object.put("pos",micLocation);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice18(Integer micLocation, Long roomId, List<String> userIds, Long micUserId, String nickname) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",micUserId);
        object.put("name",nickname);
        object.put("type",Constant.ROOM_MANAGE_CANCEL);
        object.put("pos",micLocation);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice10(Integer micLocation, Long roomId, List<String> userIds, Long micUserId, String nickname, int mic) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",micUserId);
        object.put("name",nickname);
        object.put("type",Constant.ROOM_MIC_RANDOM);
        object.put("randomImage",mic);
        object.put("pos",micLocation);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice11Or14(Long userId, String imgUrl, String msg, Long roomId, Integer type, List<String> userIds, Integer userRole, Integer playLenth) {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        FuntimeUser user = userService.queryUserById(userId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        JSONObject object = new JSONObject();
        object.put("type",type);
        object.put("rid",roomId);
        object.put("playLength",playLenth);
        object.put("uid",userId);
        object.put("userRole",userRole);
        object.put("name",user.getNickname());
        if (type == 11){
            object.put("msg",msg);
        }else{
            object.put("imgUrl",imgUrl);
        }
        String objectStr = JSONObject.toJSONString(object);
        //String data = StringEscapeUtils.escapeJava(objectStr);
        if (userIds!=null&&!userIds.isEmpty()){
            sendRoomUserNotice(userSig,objectStr,userIds);
        }

    }

    @Override
    public void notice19(RoomGiftNotice notice, List<String> userIds) {
        String object = JSONObject.toJSONString(notice);
        String data = StringEscapeUtils.unescapeJava(object);
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice20(Long roomId, List<String> userIds,Integer roomUserCount) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("roomUserCount",roomUserCount);
        object.put("type",Constant.ROOM_USER_COUNT);
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        sendRoomUserNotice(userSig,data,userIds);

    }

    @Override
    public void notice21(RoomGiftNotice notice) {

        String object = JSONObject.toJSONString(notice);
        String parameterHandler = StringEscapeUtils.unescapeJava(object);
        saveNotice(Constant.ROOM_GIFT_SEND_ROOM_ALL, parameterHandler,0);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void notice10001(String content, Long userId, Long roomId, String hornLength) {
        userService.checkSensitive(content);
        FuntimeUserAccount userAccountInfo = userService.getUserAccountInfoById(userId);
        if (userAccountInfo==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        roomId = roomId==null||roomId.intValue() == 0?null:roomId;

        if (userAccountInfo.getHornNumber()<1){
            if (userAccountInfo.getBlueDiamond().subtract(userAccountInfo.getHornPrice()).doubleValue()<0){
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            }
            userService.updateUserAccountForSub(userId,null,userAccountInfo.getHornPrice(),null);
            accountService.saveUserAccountBlueLog(userId,userAccountInfo.getHornPrice(),roomId
                    , OperationType.BUY_HORN.getAction(),OperationType.BUY_HORN.getOperationType(), roomId);

        }else {
            userService.updateUserAccountForSub(userId, null, null, 1);
            accountService.saveUserAccountHornLog(userId,1,roomId,OperationType.HORN_CONSUME.getAction(),OperationType.HORN_CONSUME.getOperationType());
        }
        FuntimeUser user = userService.queryUserById(userId);
        JSONObject object = new JSONObject();
        object.put("type",Constant.SERVICE_MSG);
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",user.getNickname());
        object.put("hornLength",hornLength);
        object.put("msg",content);
        object.put("sex",user.getSex());
        object.put("imgUrl", user.getPortraitAddress());
        String objectStr = JSONObject.toJSONString(object);
        String parameterHandler = StringEscapeUtils.unescapeJava(objectStr);
        saveNotice(Constant.SERVICE_MSG, parameterHandler,0);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void notice10002(String content, Long userId, Long roomId, String nickname, Integer sex, String portraitAddress, String giftName, Integer giftNum, String hornLength) {

        JSONObject object = new JSONObject();
        object.put("type",Constant.SERVICE_GIFT_MSG);
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",nickname);
        object.put("msg",content);
        object.put("sex",sex);
        object.put("giftName",giftName);
        object.put("hornLength",hornLength);
        object.put("giftNum",giftNum);
        object.put("imgUrl", portraitAddress);
        String objectStr = JSONObject.toJSONString(object);
        String parameterHandler = StringEscapeUtils.unescapeJava(objectStr);
        saveNotice(Constant.SERVICE_GIFT_MSG, parameterHandler,0);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void notice10003(String content, Long userId, Long roomId, String nickname, Integer sex, String portraitAddress, String hornLength) {

        JSONObject object = new JSONObject();
        object.put("type",Constant.SERVICE_REDPACKET_MSG);
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",nickname);
        object.put("hornLength",hornLength);
        object.put("msg",content);
        object.put("sex",sex);
        object.put("imgUrl", portraitAddress);
        String objectStr = JSONObject.toJSONString(object);
        String parameterHandler = StringEscapeUtils.unescapeJava(objectStr);
        saveNotice(Constant.SERVICE_REDPACKET_MSG, parameterHandler,0);
    }

    @Override
    public void notice25(Long userId, Long roomId, String levelUrl, String nickname, String portraitAddress, List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",nickname);
        object.put("imgUrl", portraitAddress);
        object.put("levelUrl",levelUrl);
        object.put("type",Constant.ROOM_MIC_USER_LEVEL_UPDATE);
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice24(Long userId) {
        JSONObject object = new JSONObject();
        object.put("uid",userId);
        object.put("type",Constant.BLOCK_USER);
        String objectStr = JSONObject.toJSONString(object);
        String parameterHandler = parameterHandler2(userId.toString(),StringEscapeUtils.unescapeJava(objectStr));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendMsg(userSig,parameterHandler)) {
            saveNotice(Constant.BLOCK_USER, parameterHandler,2);
        }
    }

    @Override
    public void notice30(Long roomId, List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("type",Constant.BLOCK_USER_ROOM);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }
    @Override
    public void notice23(Long roomId, List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("type",Constant.BLOCK_ROOM);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice26() {
        String object = JSONObject.toJSONString(JsonUtil.getMap("type",Constant.REDPACKET_SHOW_OPEN));
        String parameterHandler = StringEscapeUtils.unescapeJava(object);
        saveNotice(Constant.REDPACKET_SHOW_OPEN, parameterHandler,0);
    }

    @Override
    public void notice27() {
        String object = JSONObject.toJSONString(JsonUtil.getMap("type",Constant.REDPACKET_SHOW_CLOSE));
        String parameterHandler = StringEscapeUtils.unescapeJava(object);
        saveNotice(Constant.REDPACKET_SHOW_CLOSE, parameterHandler,0);
    }

    @Override
    public void notice28() {
        String object = JSONObject.toJSONString(JsonUtil.getMap("type",Constant.YAOYAO_SHOW_OPEN));
        String parameterHandler = StringEscapeUtils.unescapeJava(object);
        saveNotice(Constant.YAOYAO_SHOW_OPEN, parameterHandler,0);
    }

    @Override
    public void notice29() {
        String object = JSONObject.toJSONString(JsonUtil.getMap("type",Constant.YAOYAO_SHOW_CLOSE));
        String parameterHandler = StringEscapeUtils.unescapeJava(object);
        saveNotice(Constant.YAOYAO_SHOW_CLOSE, parameterHandler,0);
    }

    @Override
    public void notice31(Long roomId, Long userId, String backgroundUrl, List<String> userIds, String backgroundUrl2) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("bgUrl",backgroundUrl);
        object.put("bgUrl2",backgroundUrl2);
        object.put("type",Constant.SET_BACKGROUND);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice32(List<String> userIds, List<Map<String, Object>> micUser, int roomUserCount) {
        JSONObject object = new JSONObject();
        object.put("type",Constant.REFRESH_MICINFO);
        object.put("micUser",micUser);
        object.put("roomUserCount",roomUserCount);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice33(List<String> userIds,String imgUrl,String name,String msg,String nameColor,String toUrl) {
        JSONObject object = new JSONObject();
        object.put("imgUrl",imgUrl);
        object.put("name",name);
        object.put("msg",msg);
        object.put("nameColor",nameColor);
        object.put("toUrl",toUrl);
        object.put("type",Constant.ROOM_LIST_MSG_GAME);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice34(List<String> userIds,String imgUrl,String name,String msg,String nameColor,String toUrl) {
        JSONObject object = new JSONObject();
        object.put("imgUrl",imgUrl);
        object.put("name",name);
        object.put("msg",msg);
        object.put("nameColor",nameColor);
        object.put("toUrl",toUrl);
        object.put("type",Constant.ROOM_LIST_MSG_YYL);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice35(Long roomId,Long userId) {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("type",Constant.ROOM_SET_MANAGER);
        List<String> toAccounts = new ArrayList<>();
        toAccounts.add(String.valueOf(userId));
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        sendRoomUserNotice(userSig,data,toAccounts);


    }

    @Override
    public void notice36(Long roomId,Long userId) {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("type",Constant.ROOM_DEL_MANAGER);
        List<String> toAccounts = new ArrayList<>();
        toAccounts.add(String.valueOf(userId));
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        sendRoomUserNotice(userSig,data,toAccounts);


    }

    @Override
    public void notice37(Integer micLocation,Long roomId, List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_START_MUSIC);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);

    }

    @Override
    public void notice38(Integer micLocation,Long roomId, List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_CANCEL_MUSIC);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }


    @Override
    public Map<String, Object> getSystemNoticeList(Integer startPage, Integer pageSie, Long userId) {
        PageHelper.startPage(startPage,pageSie);
        List<Map<String, Object>> noticeList = noticeMapper.getSystemNoticeList();
        Integer read = noticeMapper.getIsReadByUserId(userId);
        read = read == null?1:2;
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isRead",read);
        resultMap.put("noticeList",new PageInfo<>(noticeList));
        return resultMap;
    }

    @Override
    public void readNotice(Long userId) {
        if (userService.checkUserExists(userId)){
            noticeMapper.saveUserSystemNotice(userId,2);
        }
    }

    @Override
    public void notice39(JSONObject noticeMap, List<String> userIds) {

        noticeMap.put("type",Constant.ROOM_BOX);
        String data = StringEscapeUtils.unescapeJava(noticeMap.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice20000(List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("type",Constant.GAME21_OPEN);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice30000(List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("type",Constant.GAME123_OPEN);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }
    @Override
    public void notice30001(List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("type",Constant.GAME123_CLOSE);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice30002(Long userId) {
        JSONObject object = new JSONObject();
        object.put("type",Constant.GAME123_RESET);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        List<String> userIds = new ArrayList<>();
        userIds.add(userId.toString());
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice40(Long roomId, Long userId, String nickname, List<String> userIds, String carUrl, String msg, String animationType) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",nickname);
        object.put("carUrl",carUrl);
        object.put("msg",msg);
        object.put("animationType",animationType);
        object.put("type",Constant.ROOM_SHOW_CAR);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);

    }

    @Override
    public void notice41(List<String> userIds, Long roomId, Long userId, String nickname, String msg) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",nickname);
        object.put("msg",msg);
        object.put("type",Constant.ROOM_OPEN_SCREEN);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice42(List<String> userIds, Long roomId, Long userId, String nickname, String msg) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",nickname);
        object.put("msg",msg);
        object.put("type",Constant.ROOM_CLOSE_SCREEN);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice43(List<String> userIds, Long roomId, Long userId) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("type",Constant.ROOM_OPEN_RANK);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice44(List<String> userIds, Long roomId, Long userId) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("type",Constant.ROOM_CLOSE_RANK);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice45(List<String> userIds, Long userId, Long roomId, Integer micCounts) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("micCounts",micCounts);
        object.put("type",Constant.ROOM_MIC_CHANGE);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice46(Long userId, Long roomId) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("type",Constant.USER_MATCH);
        List<String> toAccounts = new ArrayList<>();
        toAccounts.add(String.valueOf(userId));
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,toAccounts);
    }

    @Override
    public void notice20001(List<String> userIds, List<FuntimeRoomGame21> list, int timestamp, int rounds, long stamp, List<FuntimeRoomGame21> totalmics, String timeZone) {
        JSONObject object = new JSONObject();
        object.put("type",Constant.GAME21_START);
        object.put("timestamp",timestamp);
        object.put("stamp",stamp);
        object.put("timeZone",timeZone);
        object.put("mics",list);
        object.put("totalmics",totalmics);
        object.put("rounds",rounds);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }


    @Override
    public void notice20002(List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("type",Constant.GAME21_END);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice20003(List<String> userIds, Integer micLocation) {
        JSONObject object = new JSONObject();
        object.put("type",Constant.GAME21_GET_POKER);
        object.put("pos",micLocation);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice20004(List<String> userIds, Integer micLocation) {
        JSONObject object = new JSONObject();
        object.put("type",Constant.GAME21_STOP_POKER);
        object.put("pos",micLocation);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    @Override
    public void notice20005(List<String> userIds, List<Map<String, Object>> wins, List<FuntimeRoomGame21> totalmics, List<FuntimeRoomGame21> mics, int rounds) {
        JSONObject object = new JSONObject();
        object.put("type",Constant.GAME21_WIN);
        object.put("wins",wins);
        object.put("rounds",rounds);
        object.put("totalmics",totalmics);
        if (mics != null) {
            object.put("mics",mics);
        }
        log.info("notice20005  rounds:{}",rounds);
        String data = StringEscapeUtils.unescapeJava(object.toJSONString());
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        sendRoomUserNotice(userSig,data,userIds);
    }

    private String parameterHandler2(String toAccount,String data){
        JSONObject paramMap = new JSONObject();
        Random random = new Random();
        paramMap.put("SyncOtherMachine",2);
        paramMap.put("MsgLifeTime",0);
        paramMap.put("To_Account",toAccount);
        paramMap.put("MsgRandom",random.nextInt(100000000));
        Map<String,String> msgContent = new HashMap<>();
        msgContent.put("Data",data);
        msgContent.put("Desc","");
        msgContent.put("Ext","");
        msgContent.put("Sound","");
        Map<String,Object> elem = new HashMap<>();
        elem.put("MsgType","TIMCustomElem");
        elem.put("MsgContent",msgContent);
        List<Map<String,Object>> msgBody = new ArrayList<>();
        msgBody.add(elem);
        paramMap.put("MsgBody",msgBody);
        return paramMap.toJSONString();
    }


    private void saveNotice(int type,String data,int state){
        FuntimeNotice notice = new FuntimeNotice();
        notice.setData(data);
        notice.setState(state);
        notice.setNoticeType(type);
        noticeMapper.insertSelective(notice);
    }

}
