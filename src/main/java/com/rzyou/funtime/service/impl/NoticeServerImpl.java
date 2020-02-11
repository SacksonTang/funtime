package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.entity.FuntimeNotice;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserAccount;
import com.rzyou.funtime.entity.RoomGiftNotice;
import com.rzyou.funtime.mapper.FuntimeNoticeMapper;
import com.rzyou.funtime.service.NoticeService;
import com.rzyou.funtime.service.RoomService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.UsersigUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NoticeServerImpl implements NoticeService {



    @Autowired
    FuntimeNoticeMapper noticeMapper;
    @Autowired
    UserService userService;
    @Autowired
    RoomService roomService;



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
    public void snedAllAppNotice(String userSig, String data, Long id) {
        List<String> list = userService.getAllUserId();
        JSONArray array;
        if (list!=null&&list.size()<=500) {
            array = TencentUtil.batchsendmsg(userSig,list,data);
            if(array == null){
                noticeMapper.updateState(id,1);
            }else{
                JSONObject object;
                List<String> users = new ArrayList<>();
                for (int i = 0;i<array.size();i++){
                    object = array.getJSONObject(i);
                    users.add(object.getString("To_Account"));
                }
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
                    JSONObject object;
                    List<String> users = new ArrayList<>();
                    for (int i = 0;i<array.size();i++){
                        object = array.getJSONObject(i);
                        users.add(object.getString("To_Account"));
                    }
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
                JSONObject object;
                List<String> users = new ArrayList<>();
                for (int i = 0;i<array.size();i++){
                    object = array.getJSONObject(i);
                    users.add(object.getString("To_Account"));
                }
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
                    JSONObject object;
                    List<String> users = new ArrayList<>();
                    for (int i = 0;i<array.size();i++){
                        object = array.getJSONObject(i);
                        users.add(object.getString("To_Account"));
                    }
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
        if (sendType == 1) {
            return noticeMapper.getGroupFailNotice();
        }else if (sendType == 2){
            return noticeMapper.getSingleFailNotice();
        }else if (sendType == 3){
            return noticeMapper.getAllRoomFailNotice();
        }else{
            return noticeMapper.getAllFailNotice();
        }

    }

    @Override
    public void notice15(Integer micLocation,Long roomId,Long userId,String roomNo) {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_MIC_HOLDING);
        List<String> memberList = new ArrayList<>();
        memberList.add(String.valueOf(userId));
        String parameterHandler = parameterHandler(StringEscapeUtils.unescapeJava(object.toJSONString()),roomNo,memberList);
        boolean flag = TencentUtil.sendGroupSystemNotification(userSig,parameterHandler);
        if (!flag){
            saveNotice(Constant.ROOM_MIC_HOLDING,parameterHandler,2);
        }


    }

    @Override
    public void notice1(Integer micLocation, Long roomId, Long micUserId, String nickname, String portraitAddress, String roomNo, Integer sex, String levelUrl) {
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
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_MIC_UPPER, parameterHandler,2);
        }
    }

    @Override
    public void notice2(Integer micLocation, Long roomId, Long micUserId, String nickname, String roomNo, int isMe) {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("uid",micUserId);
        object.put("isMe",isMe);
        object.put("name",nickname);
        object.put("type",Constant.ROOM_MIC_LOWER);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_MIC_LOWER, parameterHandler,2);
        }
    }

    @Override
    public void notice3(Integer micLocation, Long roomId, String roomNo) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_MIC_STOP);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_MIC_STOP, parameterHandler,2);
        }

    }

    @Override
    public void notice4(Integer micLocation, Long roomId, String roomNo) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_MIC_OPEN);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_MIC_OPEN, parameterHandler,2);
        }
    }

    @Override
    public void notice5(Integer micLocation, Long roomId, String roomNo) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_MIC_FORBID);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_MIC_FORBID, parameterHandler,2);
        }
    }

    @Override
    public void notice6(Integer micLocation, Long roomId, String roomNo) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_MIC_RELEASE);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_MIC_RELEASE, parameterHandler,2);
        }
    }

    @Override
    public void notice7(Long roomId, String roomNo1) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_CLOSE);
        String parameterHandler = parameterHandler(roomNo1, StringEscapeUtils.unescapeJava(object.toJSONString()));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_CLOSE, parameterHandler,2);
        }
    }

    @Override
    public void notice8(RoomGiftNotice notice, String roomNo) {
        String object = JSONObject.toJSONString(notice);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_GIFT_SEND, parameterHandler,2);
        }
    }

    @Override
    public void notice9(RoomGiftNotice notice) {
        String object = JSONObject.toJSONString(notice);
        String parameterHandler = StringEscapeUtils.unescapeJava(object);
        saveNotice(Constant.ROOM_GIFT_SEND_ALL, parameterHandler,0);

    }

    @Override
    public void notice12(Long roomId, Long userId, String nickname, String roomNo1) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",nickname);
        object.put("type",Constant.ROOM_ENTER);
        String parameterHandler = parameterHandler(roomNo1, StringEscapeUtils.unescapeJava(object.toJSONString()));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_ENTER, parameterHandler,2);
        }
    }

    @Override
    public void notice13(Long roomId, String roomNo, String nickname) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_REDPACKET_SEND);
        object.put("name",nickname);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_REDPACKET_SEND, parameterHandler,2);
        }
    }

    @Override
    public void notice16(Integer micLocation, Long roomId, Long kickIdUserId, String roomNo) {
        JSONObject object = new JSONObject();
        object.put("pos",micLocation);
        object.put("rid",roomId);
        object.put("type",Constant.ROOM_KICKED);
        List<String> memberList = new ArrayList<>();
        memberList.add(String.valueOf(kickIdUserId));
        String parameterHandler = parameterHandler(StringEscapeUtils.unescapeJava(object.toJSONString()),roomNo,memberList);
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupSystemNotification(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_KICKED, parameterHandler,2);
        }
    }

    @Override
    public void notice17(Integer micLocation, Long roomId, String roomNo, Long micUserId, String nickname) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",micUserId);
        object.put("name",nickname);
        object.put("type",Constant.ROOM_MANAGE);
        object.put("pos",micLocation);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_MANAGE, parameterHandler,2);
        }
    }

    @Override
    public void notice18(Integer micLocation, Long roomId, String roomNo, Long micUserId, String nickname) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",micUserId);
        object.put("name",nickname);
        object.put("type",Constant.ROOM_MANAGE_CANCEL);
        object.put("pos",micLocation);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_MANAGE_CANCEL, parameterHandler,2);
        }
    }

    @Override
    public void notice10(Integer micLocation, Long roomId, String roomNo, Long micUserId, String nickname, int mic) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",micUserId);
        object.put("name",nickname);
        object.put("type",Constant.ROOM_MIC_RANDOM);
        object.put("randomImage",mic);
        object.put("pos",micLocation);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_MIC_RANDOM, parameterHandler,2);
        }
    }

    @Override
    public void notice11Or14(Long userId, String imgUrl, String msg, Long roomId, Integer type, List<String> roomNos, Integer userRole) {
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
        for (String roomNo : roomNos) {
            String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));
            if (!TencentUtil.sendGroupMsg(userSig, parameterHandler)) {
                saveNotice(type, parameterHandler, 2);
            }
        }
    }

    @Override
    public void notice19(RoomGiftNotice notice, String roomNo) {
        String object = JSONObject.toJSONString(notice);
        String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object));
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        if (!TencentUtil.sendGroupMsg(userSig,parameterHandler)) {
            saveNotice(Constant.ROOM_GIFT_SEND_ROOM, parameterHandler,2);
        }
    }

    @Override
    public void notice20(Long roomId, List<String> roomNos,Integer roomUserCount) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("roomUserCount",roomUserCount);
        object.put("type",Constant.ROOM_USER_COUNT);
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        for (String roomNo : roomNos) {
            String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));

            if (!TencentUtil.sendGroupMsg(userSig, parameterHandler)) {
                saveNotice(Constant.ROOM_USER_COUNT, parameterHandler, 2);
            }
        }
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
        }else {
            userService.updateUserAccountForSub(userId, null, null, 1);
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
    public void notice25(Long userId, Long roomId, String levelUrl, String nickname, String portraitAddress, List<String> roomNos) {
        JSONObject object = new JSONObject();
        object.put("rid",roomId);
        object.put("uid",userId);
        object.put("name",nickname);
        object.put("imgUrl", portraitAddress);
        object.put("levelUrl",levelUrl);
        object.put("type",Constant.ROOM_MIC_USER_LEVEL_UPDATE);
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        for (String roomNo : roomNos) {
            String parameterHandler = parameterHandler(roomNo, StringEscapeUtils.unescapeJava(object.toJSONString()));

            if (!TencentUtil.sendGroupMsg(userSig, parameterHandler)) {
                saveNotice(Constant.ROOM_MIC_USER_LEVEL_UPDATE, parameterHandler, 2);
            }
        }
    }


    public String parameterHandler(String groupId,String data){
        JSONObject paramMap = new JSONObject();
        Random random = new Random();
        paramMap.put("OnlineOnlyFlag",1);
        paramMap.put("GroupId",groupId);
        paramMap.put("Random",random.nextInt(100000000));
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

    private String parameterHandler(String content,String groupId,List<String> memberList){
        JSONObject paramMap = new JSONObject();

        paramMap.put("Content",content);
        paramMap.put("GroupId",groupId);
        paramMap.put("ToMembers_Account",memberList);
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
