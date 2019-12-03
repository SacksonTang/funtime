package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.UserRole;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.FuntimeChatroomKickedRecordMapper;
import com.rzyou.funtime.mapper.FuntimeChatroomMapper;
import com.rzyou.funtime.mapper.FuntimeChatroomMicMapper;
import com.rzyou.funtime.mapper.FuntimeChatroomUserMapper;
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
    FuntimeChatroomMapper chatroomMapper;
    @Autowired
    FuntimeChatroomMicMapper chatroomMicMapper;
    @Autowired
    FuntimeChatroomUserMapper chatroomUserMapper;
    @Autowired
    FuntimeChatroomKickedRecordMapper chatroomKickedRecordMapper;

    @Override
    @Transactional
    public Long roomCreate(Long userId) {
        FuntimeUser user = userService.queryUserById(userId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        userService.updateCreateRoomPlus(userId);

        Long roomId = saveChatroom(userId,user.getNickname());

        saveMic(roomId,10,userId);

        String roomNo = UUID.randomUUID().toString().replaceAll("-","");

        saveChatroomUser(userId,roomId,UserRole.ROOM_CREATER.getValue(),roomNo,1);

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

    private Long saveChatroom(Long userId, String nickname) {

        FuntimeChatroom chatroom = new FuntimeChatroom();
        chatroom.setUserId(userId);
        chatroom.setName(nickname);
        int k = chatroomMapper.insertSelective(chatroom);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        return chatroom.getId();
    }

    @Override
    @Transactional
    public void roomUpdate(FuntimeChatroom chatroom) {
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
            if (StringUtils.isBlank(password)||StringUtils.isBlank(chatroom.getPassword())){
                throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_PASS_EMPTY.getValue(),ErrorMsgEnum.ROOM_JOIN_PASS_EMPTY.getDesc());
            }
            if (!password.equals(chatroom.getPassword())){
                throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_PASS_ERROR.getValue(),ErrorMsgEnum.ROOM_JOIN_PASS_ERROR.getDesc());
            }
        }
        if (userService.queryUserById(userId)==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        Integer count = chatroomKickedRecordMapper.checkUserIsKickedOrNot(roomId,userId);

        if (count>0){
            throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_USER_BLOCKED.getValue(),ErrorMsgEnum.ROOM_JOIN_USER_BLOCKED.getDesc());
        }

        Long id = chatroomUserMapper.checkUserIsExist(roomId,userId);

        if (id!=null){
            throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_USER_EXISTS.getValue(),ErrorMsgEnum.ROOM_JOIN_USER_EXISTS.getDesc());
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
                if (Integer.parseInt(map.get("roomNoCount").toString())<3){
                    roomNo = map.get("roomNo").toString();
                }
            }
        }
        if (roomNo == null){
            roomNo = UUID.randomUUID().toString().replaceAll("-","");
            createRoomForTencent(userId,roomNo);
        }
        saveChatroomUserForRoomJoin(userId,roomId,userRole,roomNo,2);

        updateOnlineNumPlus(roomId);

        return chatroom.getUserId().equals(userId);
    }

    @Override
    public Map<String, Object> getRoomInfo(Long roomId) {

        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("chatroom",chatroom);

        result.put("mic",chatroomMicMapper.getMicUserByRoomId(roomId));

        return result;
    }

    @Override
    @Transactional
    public void roomExit(Long userId, Long roomId, Integer micLocation) {
        if (userService.queryUserById(userId)==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }

        if (chatroom.getIsBlock().intValue()==1){
            throw new BusinessException(ErrorMsgEnum.ROOM_IS_BLOCK.getValue(),ErrorMsgEnum.ROOM_IS_BLOCK.getDesc());
        }

        if (micLocation.intValue()>0) {
            lowerWheat(roomId, micLocation, userId);
        }

        deleteChatroomUser(roomId,userId);

        updateOnlineNumSub(roomId);
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
    public void roomKicked(Long kickIdUserId, Long userId, Long roomId, Integer micLocation) {
        if (userService.queryUserById(userId)==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }

        if (chatroom.getIsBlock().intValue()==1){
            throw new BusinessException(ErrorMsgEnum.ROOM_IS_BLOCK.getValue(),ErrorMsgEnum.ROOM_IS_BLOCK.getDesc());
        }

        Integer count = chatroomKickedRecordMapper.checkUserIsKickedOrNot(roomId,userId);

        if (count>0){
            throw new BusinessException(ErrorMsgEnum.ROOM_KICKED_USER_EXIST.getValue(),ErrorMsgEnum.ROOM_KICKED_USER_EXIST.getDesc());
        }

        if (micLocation>0) {
            lowerWheat(roomId, micLocation, userId);
        }

        Long id = chatroomUserMapper.checkUserIsExist(roomId,kickIdUserId);

        if (id!=null){
            chatroomUserMapper.deleteFlagById(id);
        }

        saveChatroomKickedRecord(kickIdUserId,userId,roomId);

        updateOnlineNumSub(roomId);
    }

    @Override
    @Transactional
    public void upperWheat(Long userId, Long roomId, Integer micLocation) {
        if (userService.queryUserById(userId)==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        FuntimeChatroomMic chatroomMic = chatroomMicMapper.getMicLocationUser(roomId,micLocation);
        if (chatroomMic==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        if (chatroomMic.getMicUserId()!=null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_EXIST.getDesc());
        }

        int k = chatroomMicMapper.upperWheat(chatroomMic.getId(),userId);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void lowerWheat(Long userId, Long roomId, Integer micLocation) {
        if (userService.queryUserById(userId)==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }

        lowerWheat(roomId,micLocation,userId);
    }

    @Override
    public void stopWheat(Long userId, Long roomId, Integer micLocation) {
        Integer userRole = getUserRole(roomId, userId);
        if (UserRole.ROOM_CREATER.getValue()!=userRole&&userRole != UserRole.ROOM_CHAIR.getValue()){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_MIC_NO_AUTH.getDesc());
        }
        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        int k = chatroomMicMapper.stopWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void forbidWheat(Long roomId, Integer micLocation){
        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        int k = chatroomMicMapper.forbidWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void openWheat(Long userId, Long roomId, Integer micLocation) {
        Integer userRole = getUserRole(roomId, userId);
        if (UserRole.ROOM_CREATER.getValue()!=userRole&&userRole != UserRole.ROOM_CHAIR.getValue()){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_MIC_NO_AUTH.getDesc());
        }
        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        int k = chatroomMicMapper.openWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void releaseWheat(Long roomId, Integer micLocation) {
        FuntimeChatroomMic micLocationUser = chatroomMicMapper.getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        int k ;
        if(micLocationUser.getMicUserId()==null){
            k = chatroomMicMapper.releaseWheat(micLocationUser.getId(),1);
        }else{
            k = chatroomMicMapper.releaseWheat(micLocationUser.getId(),2);
        }
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    @Transactional
    public void roomClose(Long userId, Long roomId) {
        Integer userRole = getUserRole(roomId, userId);
        if (userRole!=UserRole.ROOM_CREATER.getValue()){
            throw new BusinessException(ErrorMsgEnum.ROOM_CLOSE_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_CLOSE_NO_AUTH.getDesc());
        }
        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<String> roomNos = chatroomUserMapper.getRoomNoByRoomIdAll(roomId);

        chatroomMapper.deleteByPrimaryKey(roomId);
        chatroomMicMapper.deleteByRoomId(roomId);
        chatroomUserMapper.deleteByRoomId(roomId);
        chatroomKickedRecordMapper.deleteByRoomId(roomId);
        if (roomNos!=null&&roomNos.size()>0) {
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            for (String roomNo:roomNos) {
                TencentUtil.destroyGroup(userSig, roomNo);
            }
        }
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

        int k = chatroomMicMapper.lowerWheat(micLocationUser.getId());
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
        else if (mic == 9){
            return UserRole.ROOM_CHAIR.getValue();
        }
        else if (mic>=1&&mic<=8){
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
