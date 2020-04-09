package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.OperationType;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.entity.FuntimeNotice;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserAccount;
import com.rzyou.funtime.entity.RoomGiftNotice;
import com.rzyou.funtime.mapper.FuntimeNoticeMapper;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.NoticeService;
import com.rzyou.funtime.service.RoomService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.JsonUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
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
        JSONArray array;
        if (toAccounts!=null&&toAccounts.size()<=500) {
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
            int toIndex = 500;
            int k = size%toIndex == 0?size/toIndex:size/toIndex+1;
            for (int j = 1;j<k+1;j++){
                List<String> spList = toAccounts.subList(fromIndex,toIndex);
                fromIndex = j*toIndex;
                toIndex =  Math.min((j+1)*toIndex,size) ;
                array = TencentUtil.batchsendmsg(userSig,spList,data);
                if(array != null){
                    List<String> users = getUserIds(array);
                    if (!users.isEmpty()) {
                        TencentUtil.batchsendmsg(userSig, users, data);
                    }
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
        if (list!=null&&list.size()<=500) {
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
            int toIndex = 500;
            int k = size%toIndex == 0?size/toIndex:size/toIndex+1;
            for (int j = 1;j<k+1;j++){
                List<String> spList = list.subList(fromIndex,toIndex);
                fromIndex = j*toIndex;
                toIndex =  Math.min((j+1)*toIndex,size) ;
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
            }
        }
    }

    @Override
    public void snedAllRoomAppNotice(String userSig, String data, Long id) {

        List<String> list = roomService.getAllRoomUser();
        JSONArray array;
        if (list!=null&&list.size()<=500) {
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
            int toIndex = 500;
            int k = size%toIndex == 0?size/toIndex:size/toIndex+1;
            for (int j = 1;j<k+1;j++){
                List<String> spList = list.subList(fromIndex,toIndex);
                fromIndex = j*toIndex;
                toIndex =  Math.min((j+1)*toIndex,size) ;
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
    public void notice12(Long roomId, Long userId, String nickname, List<String> userIds) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",nickname);
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
    public void notice16(Integer micLocation, Long roomId, Long kickIdUserId, List<String> userIds) {
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
    public void notice11Or14(Long userId, String imgUrl, String msg, Long roomId, Integer type, List<String> userIds, Integer userRole) {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        FuntimeUser user = userService.queryUserById(userId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        JSONObject object = new JSONObject();
        object.put("type",type);
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("userRole",userRole);
        object.put("name",user.getNickname());
        if (type == 11){
            object.put("msg",msg);
        }else{
            object.put("imgUrl",imgUrl);
        }
        String objectStr = JSONObject.toJSONString(object);
        String data = StringEscapeUtils.unescapeJava(objectStr);
        if (userIds!=null&&!userIds.isEmpty()){
            sendRoomUserNotice(userSig,data,userIds);
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
    public void notice10001(String content, Long userId, Long roomId) {
        FuntimeUserAccount userAccountInfo = userService.getUserAccountInfoById(userId);
        if (userAccountInfo==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (userAccountInfo.getHornNumber()<1){
            if (userAccountInfo.getBlueDiamond().subtract(userAccountInfo.getHornPrice()).doubleValue()<0){
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            }
            userService.updateUserAccountForSub(userId,null,userAccountInfo.getHornPrice(),null);
            accountService.saveUserAccountBlueLog(userId,userAccountInfo.getHornPrice(),null
                    , OperationType.BUY_HORN.getAction(),OperationType.BUY_HORN.getOperationType());

        }else {
            userService.updateUserAccountForSub(userId, null, null, 1);
            accountService.saveUserAccountHornLog(userId,1,null,OperationType.HORN_CONSUME.getAction(),OperationType.HORN_CONSUME.getOperationType());
        }
        FuntimeUser user = userService.queryUserById(userId);
        JSONObject object = new JSONObject();
        object.put("type",Constant.SERVICE_MSG);
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",user.getNickname());
        object.put("msg",content);
        object.put("sex",user.getSex());
        object.put("imgUrl", user.getPortraitAddress());
        String objectStr = JSONObject.toJSONString(object);
        String parameterHandler = StringEscapeUtils.unescapeJava(objectStr);
        saveNotice(Constant.SERVICE_MSG, parameterHandler,0);
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
