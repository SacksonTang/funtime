package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.cos.CosUtil;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.NoticeService;
import com.rzyou.funtime.service.RoomService;
import com.rzyou.funtime.service.UserService;

import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    UserService userService;
    @Autowired
    NoticeService noticeService;

    @Autowired
    FuntimeChatroomMapper chatroomMapper;
    @Autowired
    FuntimeChatroomMicMapper chatroomMicMapper;
    @Autowired
    FuntimeChatroomUserMapper chatroomUserMapper;
    @Autowired
    FuntimeChatroomKickedRecordMapper chatroomKickedRecordMapper;
    @Autowired
    FuntimeGiftMapper giftMapper;
    @Autowired
    FuntimeTagMapper tagMapper;

    @Override
    @Transactional
    public Long roomCreate(Long userId) {
        FuntimeUser user = userService.queryUserById(userId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeChatroom chatroom = chatroomMapper.getRoomByUserId(userId);
        if (chatroom!= null){
            return  chatroom.getId();
        }
        userService.updateCreateRoomPlus(userId);

        Long roomId = saveChatroom(userId,user.getNickname());

        saveMic(roomId,10,userId);

        String roomNo = UUID.randomUUID().toString().replaceAll("-","");

        saveChatroomUser(userId,roomId,UserRole.ROOM_CREATER.getValue(),roomNo,1);

        //调用腾讯创建聊天室接口
        createRoomForTencent(userId,roomNo);

        return roomId;
    }

    private void createRoomForTencent(Long userId,String roomNo){
        List<Map<String,String>> mapList = new ArrayList<>();
        Map<String,String> member = new HashMap<>();
        member.put("Member_Account",String.valueOf(userId));

        mapList.add(member);
        boolean flag = TencentUtil.createGroup(UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER),mapList,roomNo);

        if (!flag){
            throw new BusinessException(ErrorMsgEnum.ROOM_CREATE_TENCENT_ERROR.getValue(),ErrorMsgEnum.ROOM_CREATE_TENCENT_ERROR.getDesc());
        }
    }

    private void enterRoomForTencent(Long userId,String roomNo){
        List<Map<String, String>> memberList = new ArrayList<>();
        Map<String, String> member = new HashMap<>();
        member.put("Member_Account",String.valueOf(userId));
        memberList.add(member);
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        JSONArray array = TencentUtil.addGroupMember(userSig, roomNo, memberList);

        for (int i = 0;i<array.size();i++){
            JSONObject obj = array.getJSONObject(i);
            Integer result = obj.getInteger("Result");
            if (result == 0) {
                log.info("用户加入群组失败,ID:{}", obj.getString("Member_Account"));
                throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_TENCENT_ERROR.getValue(),ErrorMsgEnum.ROOM_JOIN_TENCENT_ERROR.getDesc());
            }
        }

    }

    private Long saveChatroom(Long userId, String nickname) {

        FuntimeChatroom chatroom = new FuntimeChatroom();
        chatroom.setUserId(userId);
        chatroom.setName(nickname);
        chatroom.setExamDesc("这个家伙很懒,什么都没有留下~");
        int k = chatroomMapper.insertSelective(chatroom);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        return chatroom.getId();
    }

    @Override
    @Transactional
    public void roomUpdate(FuntimeChatroom chatroom) {
        Integer userRole = getUserRole(chatroom.getId(), chatroom.getUserId());
        if (userRole==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if(!userService.checkAuthorityForUserRole(userRole, UserRoleAuthority.A_1.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        FuntimeChatroom chatroom1 = chatroomMapper.selectByPrimaryKey(chatroom.getId());
        if (!chatroom1.getUserId().equals(chatroom.getUserId())){
            throw new BusinessException(ErrorMsgEnum.ROOM_CREATER_ERROR.getValue(),ErrorMsgEnum.ROOM_CREATER_ERROR.getDesc());
        }

        updateChatroom(chatroom);

    }

    @Override
    @Transactional
    public boolean roomJoin(Long userId, Long roomId,String password) {
        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }

        if (chatroom.getIsBlock().intValue()==1){
            throw new BusinessException(ErrorMsgEnum.ROOM_IS_BLOCK.getValue(),ErrorMsgEnum.ROOM_IS_BLOCK.getDesc());
        }

        if (chatroom.getIsLock().intValue()==1){
            if (!chatroom.getUserId().equals(userId)) {
                if (StringUtils.isBlank(password) || StringUtils.isBlank(chatroom.getPassword())) {
                    throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_PASS_EMPTY.getValue(), ErrorMsgEnum.ROOM_JOIN_PASS_EMPTY.getDesc());
                }
                if (!password.equals(chatroom.getPassword())) {
                    throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_PASS_ERROR.getValue(), ErrorMsgEnum.ROOM_JOIN_PASS_ERROR.getDesc());
                }
            }
        }
        FuntimeUser user = userService.queryUserById(userId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        if (userId.equals(chatroom.getUserId())){
            FuntimeChatroomMic chatroomMic = chatroomMicMapper.getMicLocationUser(roomId,10);
            chatroomMicMapper.upperWheat(chatroomMic.getId(),userId);
        }

        Integer count = chatroomKickedRecordMapper.checkUserIsKickedOrNot(roomId,userId);

        if (count>0){
            throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_USER_BLOCKED.getValue(),ErrorMsgEnum.ROOM_JOIN_USER_BLOCKED.getDesc());
        }

        Long id = chatroomUserMapper.checkUserIsExist(roomId,userId);

        if (id!=null){
            return true;
        }
        int userRole = UserRole.ROOM_NORMAL.getValue();
        if (userId.equals(chatroom.getUserId())){
            userRole = UserRole.ROOM_CREATER.getValue();
        }
        List<Map<String, Object>> roomMaps = chatroomUserMapper.getRoomNoByRoomId(roomId);
        String roomNo = null;
        if (roomMaps==null||roomMaps.isEmpty()){
            throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_USER_EXISTS.getValue(),ErrorMsgEnum.ROOM_JOIN_USER_EXISTS.getDesc());
        }else if (roomMaps.size()==1){
            if (Integer.parseInt(roomMaps.get(0).get("roomNoCount").toString())<3){
                roomNo = roomMaps.get(0).get("roomNo").toString();
            }

        }else{
            for (Map<String,Object> map : roomMaps){
                if (Integer.parseInt(map.get("roomNoCount").toString())<200){
                    roomNo = map.get("roomNo").toString();
                }
            }
        }
        if (roomNo == null){
            roomNo = UUID.randomUUID().toString().replaceAll("-","");
            createRoomForTencent(userId,roomNo);
        }
        saveChatroomUserForRoomJoin(userId,roomId,userRole,roomNo,1);

        updateOnlineNumPlus(roomId);
        saveUserRoomLog(1,userId,roomId,null);
        enterRoomForTencent(userId,roomNo);
        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);
        //发送通知
        for (String roomNo1 : roomNos) {
            noticeService.notice12(roomId, userId, user.getNickname(), roomNo1);
        }

        noticeService.notice20(roomId,roomNos,chatroom.getOnlineNum()+1);

        return chatroom.getUserId().equals(userId);
    }

    private void saveUserRoomLog(int type, Long userId, Long roomId, Long toUserId) {
        Map<String,Object> map = new HashMap<>();
        map.put("type",type);
        map.put("userId",userId);
        map.put("roomId",roomId);
        map.put("toUserId",toUserId);
        chatroomMapper.insertUserRoomLog(map);
    }

    @Override
    public Map<String, Object> getRoomInfo(Long roomId) {

        FuntimeChatroom chatroom = chatroomMapper.getRoomInfoById(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        chatroom.setPassword(null);
        if (StringUtils.isNotBlank((chatroom.getPortraitAddress()))&&!chatroom.getPortraitAddress().startsWith("http")){
            chatroom.setPortraitAddress(CosUtil.generatePresignedUrl(chatroom.getPortraitAddress()));
        }
        if (StringUtils.isNotBlank((chatroom.getAvatarUrl()))){
            chatroom.setAvatarUrl(CosUtil.generatePresignedUrl(chatroom.getAvatarUrl()));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("chatroom",chatroom);

        List<Map<String, Object>> micUser = chatroomMicMapper.getMicUserByRoomId(roomId);
        if (micUser!=null&&!micUser.isEmpty()){
            for (Map<String, Object> map : micUser){
                if (map.get("portraitAddress")!=null&&!map.get("portraitAddress").toString().startsWith("http")){
                    map.put("portraitAddress",CosUtil.generatePresignedUrl(map.get("portraitAddress").toString()));
                }
            }
        }

        result.put("mic",micUser);

        return result;
    }

    @Override
    @Transactional
    public void roomExit(Long userId, Long roomId) {
        if (!userService.checkUserExists(userId)){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }

        if (chatroom.getIsBlock().intValue()==1){
            throw new BusinessException(ErrorMsgEnum.ROOM_IS_BLOCK.getValue(),ErrorMsgEnum.ROOM_IS_BLOCK.getDesc());
        }

        Integer mic = chatroomMicMapper.getMicLocation(roomId, userId);

        if (mic!=null) {
            lowerWheat(roomId, mic, userId);
        }
        deleteChatroomUser(roomId, userId);
        updateOnlineNumSub(roomId);

        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);
        noticeService.notice20(roomId,roomNos,chatroom.getOnlineNum()-1);

    }

    private void deleteChatroomUser(Long roomId, Long userId) {
        Long id = chatroomUserMapper.checkUserIsExist(roomId,userId);
        if (id==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
        }
        int k = chatroomUserMapper.deleteFlagById(id);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    @Transactional
    public void roomKicked(Long kickIdUserId, Long userId, Long roomId) {
        //校验用户
        FuntimeUser user = userService.queryUserById(kickIdUserId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (!userService.checkUserExists(userId)){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        //校验房间
        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        //校验用户权限
        Integer userRole = getUserRole(roomId, userId);
        if (userRole==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if(!userService.checkAuthorityForUserRole(userRole, UserRoleAuthority.A_8.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }

        //房间被封
        if (chatroom.getIsBlock().intValue()==1){
            throw new BusinessException(ErrorMsgEnum.ROOM_IS_BLOCK.getValue(),ErrorMsgEnum.ROOM_IS_BLOCK.getDesc());
        }

        //用户已被踢
        Integer count = chatroomKickedRecordMapper.checkUserIsKickedOrNot(roomId,userId);

        if (count>0){
            throw new BusinessException(ErrorMsgEnum.ROOM_KICKED_USER_EXIST.getValue(),ErrorMsgEnum.ROOM_KICKED_USER_EXIST.getDesc());
        }

        Integer micLocation = chatroomMicMapper.getMicLocation(roomId, kickIdUserId);
        if (micLocation!=null) {

            //麦上用户需要先下麦
            if (micLocation > 0) {
                lowerWheat(roomId, micLocation, kickIdUserId);
                //下麦通知
                List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);
                for (String roomNo : roomNos) {
                    noticeService.notice2(micLocation, roomId, kickIdUserId, user.getNickname(), roomNo, 1);
                }
            }
        }else{
            micLocation = 0;
        }

        String roomNo = chatroomUserMapper.getRoomNoByRoomIdAndUser(roomId,kickIdUserId);

        Long id = chatroomUserMapper.checkUserIsExist(roomId,kickIdUserId);

        if (id!=null){
            chatroomUserMapper.deleteFlagById(id);
        }

        saveChatroomKickedRecord(kickIdUserId,userId,roomId);

        updateOnlineNumSub(roomId);


        //发送通知
        noticeService.notice16(micLocation, roomId, kickIdUserId, roomNo);

        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);
        noticeService.notice20(roomId,roomNos,chatroom.getOnlineNum()-1);


    }

    @Override
    @Transactional
    public void holdWheat(Long userId, Long roomId, Integer micLocation, Long micUserId) {


        //抱麦
        Integer userRole = getUserRole(roomId,userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_4.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }


        if (!userService.checkUserExists(micUserId)){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (chatroomMapper.checkRoomExists(roomId)==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        FuntimeChatroomMic chatroomMic = chatroomMicMapper.getMicLocationUser(roomId,micLocation);
        if (chatroomMic==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        if (chatroomMic.getMicUserId()!=null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_EXIST.getDesc());
        }
        String roomNo = chatroomUserMapper.getRoomNoByRoomIdAndUser(roomId,micUserId);

        //发送通知
        noticeService.notice15(micLocation,roomId,micUserId,roomNo);



    }

    @Override
    @Transactional
    public void upperWheat(Long userId, Long roomId, Integer micLocation, Long micUserId) {

        if (userId!=null){
            //抱麦
            Integer userRole = getUserRole(roomId,userId);
            if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_4.getValue())){
                throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
            }
        }

        FuntimeUser user = userService.queryUserById(micUserId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (chatroomMapper.checkRoomExists(roomId)==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        FuntimeChatroomMic chatroomMic = chatroomMicMapper.getMicLocationUser(roomId,micLocation);
        if (chatroomMic==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        if (chatroomMic.getMicUserId()!=null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_EXIST.getDesc());
        }

        Long micLocationId = chatroomMicMapper.getMicLocationId(roomId, micUserId);
        if (micLocationId!=null){
            chatroomMicMapper.lowerWheat(micLocationId, micLocation);
        }

        int k = chatroomMicMapper.upperWheat(chatroomMic.getId(),micUserId);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);
        //发送通知
        for (String roomNo : roomNos) {
            noticeService.notice1(micLocation, roomId, micUserId, user.getNickname(), user.getPortraitAddress(), roomNo);
        }


    }

    @Override
    @Transactional
    public void lowerWheat(Long userId, Long roomId, Long micUserId) {

        int isMe = 1;
        if (userId!=null){
            isMe = 0;
            //被下麦
            Integer userRole = getUserRole(roomId,userId);
            if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_7.getValue())){
                throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
            }
        }

        FuntimeUser user = userService.queryUserById(micUserId);

        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (chatroomMapper.checkRoomExists(roomId)==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }

        Integer micLocation = chatroomMicMapper.getMicLocation(roomId, micUserId);
        if (micLocation == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }
        lowerWheat(roomId,micLocation,micUserId);
        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);


        //发送通知
        for (String roomNo : roomNos) {
            noticeService.notice2(micLocation, roomId, micUserId, user.getNickname(), roomNo,isMe);
        }


    }

    @Override
    @Transactional
    public void stopWheat(Long userId, Long roomId, Integer micLocation) {
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_5.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }

        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        int k = chatroomMicMapper.stopWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);

        //发送通知
        for (String roomNo : roomNos) {
            noticeService.notice3(micLocation, roomId,roomNo);
        }


    }

    @Override
    @Transactional
    public void forbidWheat(Long roomId, Integer micLocation, Long userId){
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_6.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        int k = chatroomMicMapper.forbidWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);

        //发送通知
        for (String roomNo : roomNos) {
            noticeService.notice5(micLocation, roomId,roomNo);
        }

    }

    @Override
    @Transactional
    public void openWheat(Long userId, Long roomId, Integer micLocation) {
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_11.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        int k = chatroomMicMapper.openWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);

        //发送通知
        for (String roomNo : roomNos) {
            noticeService.notice4(micLocation, roomId,roomNo);
        }


    }

    @Override
    @Transactional
    public void releaseWheat(Long roomId, Integer micLocation, Long userId) {
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_12.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }

        int k = chatroomMicMapper.releaseWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);

        //发送通知
        for (String roomNo : roomNos) {
            noticeService.notice6(micLocation, roomId,roomNo);
        }


    }

    @Override
    @Transactional
    public void roomClose(Long userId, Long roomId) {

        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_2.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        if (chatroomMapper.checkRoomExists(roomId)==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);

        chatroomMapper.deleteByRoomId(roomId);
        chatroomMicMapper.deleteByRoomId(roomId);
        chatroomUserMapper.deleteByRoomId(roomId);
        if (roomNos!=null&&roomNos.size()>0) {
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            for (String roomNo:roomNos) {
                TencentUtil.destroyGroup(userSig, roomNo);
            }
        }
        String roomNo = UUID.randomUUID().toString().replaceAll("-","");

        chatroomUserMapper.updateRoomNoByRoomId(roomNo,userId);

        createRoomForTencent(userId,roomNo);


        //发送通知
        for (String roomNo1 : roomNos) {
            noticeService.notice7(roomId,roomNo1);
        }


    }

    @Override
    public PageInfo<Map<String, Object>> getRoomList(Integer startPage, Integer pageSize, Integer tagId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String,Object>> list = chatroomMapper.getRoomList(tagId);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            for (Map<String, Object> map : list){
                if (map.get("examUrl")!=null){
                    map.put("examUrl",CosUtil.generatePresignedUrl(map.get("examUrl").toString()));
                }
                if (map.get("avatarUrl")!=null){
                    map.put("avatarUrl",CosUtil.generatePresignedUrl(map.get("avatarUrl").toString()));
                }
            }
            return new PageInfo<>(list);
        }
    }

    @Override
    public PageInfo<Map<String, Object>> getRoomUserById(Integer startPage,Integer pageSize,Long roomId,String nickname) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String, Object>> list = chatroomUserMapper.getRoomUserById(roomId, nickname);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else {
            for (Map<String, Object> map : list){
                if (map.get("portraitAddress")!=null&&!map.get("portraitAddress").toString().startsWith("http")){
                    map.put("portraitAddress",CosUtil.generatePresignedUrl(map.get("portraitAddress").toString()));
                }
            }

            return new PageInfo<>(list);
        }
    }

    @Override
    public List<String> getRoomNoByRoomIdAll(Long roomId) {
        return chatroomUserMapper.getRoomNoByRoomIdAll(roomId);
    }

    @Override
    public FuntimeChatroom getChatroomById(Long roomId) {
        return chatroomMapper.selectByPrimaryKey(roomId);
    }

    @Override
    @Transactional
    public void roomManage(Long roomId,  Long userId, Long micUserId) {

        FuntimeUser user = userService.queryUserById(micUserId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        Integer userRole = getUserRole(roomId, userId);
        if (userRole==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_9.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        Integer micLocation = chatroomMicMapper.getMicLocation(roomId, micUserId);
        if (micLocation == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }
        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (!micUserId.equals(micLocationUser.getMicUserId())){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }
        if (micLocationUser.getUserRole() !=3){
            return;
        }
        if (micLocationUser.getState() == 2){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_IS_STOP.getValue(),ErrorMsgEnum.ROOM_MIC_IS_STOP.getDesc());
        }

        int k = chatroomMicMapper.roomManage(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);

        //发送通知
        for (String roomNo : roomNos) {
            noticeService.notice17(micLocation, roomId,roomNo,micUserId,user.getNickname());
        }

    }

    @Override
    @Transactional
    public void roomManageCancel(Long roomId, Long userId, Long micUserId) {
        FuntimeUser user = userService.queryUserById(micUserId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        Integer userRole = getUserRole(roomId, userId);
        if (userRole==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_13.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }

        Integer micLocation = chatroomMicMapper.getMicLocation(roomId, micUserId);
        if (micLocation == null){
            throw  new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }

        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (!micUserId.equals(micLocationUser.getMicUserId())){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }
        if (micLocationUser.getUserRole() != 2){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_IS_NOT_MANAGE.getValue(),ErrorMsgEnum.ROOM_MIC_IS_NOT_MANAGE.getDesc());
        }
        if (micLocationUser.getState() == 2){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_IS_STOP.getValue(),ErrorMsgEnum.ROOM_MIC_IS_STOP.getDesc());
        }

        int k = chatroomMicMapper.roomManageCancel(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);

        //发送通知
        for (String roomNo : roomNos) {
            noticeService.notice18(micLocation, roomId,roomNo,micUserId,user.getNickname());
        }
    }

    @Override
    public int roomRandomMic(Long roomId,  Long userId, Long micUserId) {
        int mic=(int)(Math.random()*9+1);

        FuntimeUser user = userService.queryUserById(micUserId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_10.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        Integer micLocation = chatroomMicMapper.getMicLocation(roomId, micUserId);
        if (micLocation == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }
        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);
        //发送通知
        for (String roomNo : roomNos) {
            noticeService.notice10(micLocation, roomId,roomNo,micUserId,user.getNickname(),mic);
        }

        return mic;
    }

    @Override
    public void sendNotice(Long userId, String imgUrl, String msg, Long roomId, Integer type) {
        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);
        noticeService.notice11Or14(userId,imgUrl,msg,roomId,type,roomNos);
    }

    @Override
    public PageInfo<Map<String, Object>> getRoomLogList(Integer startPage, Integer pageSize, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String,Object>> list = chatroomMapper.getRoomLogList(userId);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            for (Map<String, Object> map : list){
                if (map.get("examUrl")!=null){
                    map.put("examUrl",CosUtil.generatePresignedUrl(map.get("examUrl").toString()));
                }
                if (map.get("avatarUrl")!=null){
                    map.put("avatarUrl",CosUtil.generatePresignedUrl(map.get("avatarUrl").toString()));
                }
            }
            return new PageInfo<>(list);
        }
    }

    @Override
    public List<FuntimeGift> getGiftListByBestowed(Integer bestowed) {
        List<FuntimeGift> list = giftMapper.getGiftListByBestowed(bestowed);
        if (list==null||list.isEmpty()){
            return null;
        }else{
            for (FuntimeGift gift:list){
                gift.setAnimationUrl(CosUtil.generatePresignedUrl(gift.getAnimationUrl()));
                gift.setImageUrl(CosUtil.generatePresignedUrl(gift.getImageUrl()));
            }
        }
        return list;
    }

    @Override
    public Map<String, Object> getGiftList() {

        Map<String,Object> result = new HashMap<>();

        List<Map<String,Object>> list = giftMapper.getGiftList();

        if (list!=null&&!list.isEmpty()){
            for (Map<String, Object> map : list){
                if (map.get("animationUrl")!=null){
                    map.put("animationUrl",CosUtil.generatePresignedUrl(map.get("animationUrl").toString()));
                }
                if (map.get("imageUrl")!=null){
                    map.put("imageUrl",CosUtil.generatePresignedUrl(map.get("imageUrl").toString()));
                }

            }
            List<Map<String,Object>> tags = tagMapper.queryTagsByType("gift_type");
            if (tags == null){
                return null;
            }

            result.put("tags",tags);

            result.put("gifts",list);
        }
        return result;
    }

    @Override
    public List<Long> getRoomUserByRoomId(Long roomId) {
        return chatroomUserMapper.getRoomUserByRoomId(roomId);
    }

    @Override
    public List<String> getAllRoomUser() {
        return chatroomUserMapper.getAllRoomUser();
    }

    public Integer getUserRole(Long roomId,Long userId){
        return chatroomMicMapper.getMicLocationUserRole(roomId, userId);
    }


    public void saveChatroomKickedRecord(Long kickIdUserId, Long userId, Long roomId){
        FuntimeChatroomKickedRecord record = new FuntimeChatroomKickedRecord();
        record.setRoomId(roomId);
        record.setKickedUserId(kickIdUserId);
        record.setUserId(userId);
        int k = chatroomKickedRecordMapper.insertSelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    public void lowerWheat(Long roomId,int micLocation,Long userId){

        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        if (micLocationUser.getMicUserId()==null||!micLocationUser.getMicUserId().equals(userId)){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }

        int k = chatroomMicMapper.lowerWheat(micLocationUser.getId(),micLocation);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    public void updateOnlineNumPlus(Long id){
        int k = chatroomMapper.updateOnlineNumPlus(id);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    public void updateOnlineNumSub(Long id){
        int k = chatroomMapper.updateOnlineNumSub(id);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    public void saveChatroomUser(long userId,long roomId,int userRole,String roomNo,Integer isSync){
        FuntimeChatroomUser chatroomUser = new FuntimeChatroomUser();
        chatroomUser.setUserId(userId);
        chatroomUser.setRoomId(roomId);
        chatroomUser.setUserRole(userRole);
        chatroomUser.setRoomNo(roomNo);
        chatroomUser.setIsSync(isSync);
        int k = chatroomUserMapper.insertSelective(chatroomUser);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    public void saveChatroomUserForRoomJoin(long userId,long roomId,int userRole,String roomNo,Integer isSync){
        FuntimeChatroomUser chatroomUser = new FuntimeChatroomUser();
        chatroomUser.setUserId(userId);
        chatroomUser.setRoomId(roomId);
        chatroomUser.setUserRole(userRole);
        chatroomUser.setRoomNo(roomNo);
        chatroomUser.setIsSync(isSync);
        int k = chatroomUserMapper.insertForRoomJoin(chatroomUser);
        if(k==0){
            throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_BUSY.getValue(),ErrorMsgEnum.ROOM_JOIN_BUSY.getDesc());
        }
    }

    public void updateChatroom(FuntimeChatroom chatroom){

        if (StringUtils.isNotBlank(chatroom.getPassword())){
            chatroom.setIsLock(1);
        }

        int k = chatroomMapper.updateByPrimaryKeySelective(chatroom);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

    }

    public void saveMic(Long roomId, int num, Long userId){
        List<FuntimeChatroomMic> mics = new ArrayList<>();
        FuntimeChatroomMic chatroomMic ;
        for (int i=0;i<num;i++){
            chatroomMic = new FuntimeChatroomMic();
            if (i == 9){
                chatroomMic.setMicUserId(userId);
            }
            chatroomMic.setRoomId(roomId);
            chatroomMic.setMicLocation(i+1);
            chatroomMic.setUserRole(getUserRole(i+1));
            mics.add(chatroomMic);
        }
        chatroomMicMapper.insertBatch(mics);

    }

    private int getUserRole(int mic){
        if (mic == 10){
            return UserRole.ROOM_CREATER.getValue();
        }
        else if (mic>=1&&mic<=9){
            return UserRole.ROOM_MIC.getValue();
        }
        else {
            return UserRole.ROOM_NORMAL.getValue();
        }
    }

    /***************************task*********************************/
    @Override
    public void syncTencent(String usersig){

        List<String> roomNos = chatroomUserMapper.getDeleteRoomUser();
        if (roomNos!=null&&roomNos.size()>0){
            for (String roomNo : roomNos){
                List<String> memberToDel_Account = chatroomUserMapper.getDeleteRoomUserByRoomNo(roomNo);
                if (memberToDel_Account!=null&&memberToDel_Account.size()>0){
                    boolean flag = TencentUtil.deleteGroupMember(usersig,roomNo,memberToDel_Account);
                    if (flag){
                        chatroomUserMapper.deleteRoomUser(roomNo);
                    }
                }
            }
        }

        roomNos = chatroomUserMapper.getJoinRoomUser();
        if (roomNos!=null&&roomNos.size()>0){
            for (String roomNo : roomNos){
                List<Long> members = chatroomUserMapper.getJoinRoomUserByRoomNo(roomNo);
                if (members!=null&&members.size()>0){
                    List<Map<String, String>> memberList = new ArrayList<>();
                    Map<String, String> member;
                    for (Long userId : members){
                        member = new HashMap<>();
                        member.put("Member_Account",String.valueOf(userId));
                        memberList.add(member);
                    }

                    JSONArray memberResult = TencentUtil.addGroupMember(usersig,roomNo,memberList);
                    if (memberResult!=null) {
                        for (int i = 0; i < memberResult.size(); i++) {
                            JSONObject obj = memberResult.getJSONObject(i);
                            Integer result = obj.getInteger("Result");
                            if (result == 0) {
                                log.info("用户加入群组失败,ID:{}", obj.getString("Member_Account"));
                            } else {
                                chatroomUserMapper.updateSyncByRoomNo(roomNo, obj.getLong("Member_Account"));
                            }
                        }
                    }

                }
            }
        }

    }




}
