package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    GameService gameService;

    @Autowired
    FuntimeBackgroundMapper backgroundMapper;
    @Autowired
    FuntimeChatroomMapper chatroomMapper;
    @Autowired
    FuntimeChatroomMicMapper chatroomMicMapper;

    @Autowired
    FuntimeChatroomKickedRecordMapper chatroomKickedRecordMapper;
    @Autowired
    FuntimeGiftMapper giftMapper;
    @Autowired
    FuntimeTagMapper tagMapper;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public JSONObject roomCreate(Long userId, Integer platform) {
        JSONObject result = new JSONObject();
        FuntimeUser user = userService.queryUserById(userId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (user.getState() ==2){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeChatroom chatroom = chatroomMapper.getRoomByUserId(userId);
        Long roomId ;
        if (chatroom!=null&&chatroom.getId()!=null){
            roomId = chatroom.getId();
            roomJoin(userId, chatroom.getId(), null, null);
            result.put("roomId",roomId);
            return result;
        }
        //用户所在的房间
        Long roomId2 = getRoomIdByUserId(userId);
        if (roomId2!=null) {
            //用户已有房间
            log.info("************进入房间前已在别的房间,现在先退出别的房间*******************");
            //退房
            roomExit(userId,roomId2);
        }
        userService.updateCreateRoomPlus(userId);

        roomId = saveChatroom(userId,user.getNickname(),user.getPortraitAddress());

        saveMicBatch(roomId,10,userId);

        //saveChatroomUser(userId,roomId,UserRole.ROOM_CREATER.getValue());

        result.put("roomId",roomId);
        return result;
    }

    private Long saveChatroom(Long userId, String nickname, String avatarUrl) {

        FuntimeChatroom chatroom = new FuntimeChatroom();
        chatroom.setUserId(userId);
        chatroom.setName(nickname);
        chatroom.setAvatarUrl(avatarUrl);
        Integer tags = tagMapper.queryTagsByTypeAndName("game_list", "娱乐");
        chatroom.setTags(tags==null?null:tags.toString());
        chatroom.setBackgroundId(backgroundMapper.getBackgroundIdForType1());
        chatroom.setExamDesc("这个家伙很懒,什么都没有留下~");
        int k = chatroomMapper.insertSelective(chatroom);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        return chatroom.getId();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
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

    public Long getRoomIdByUserId(Long userId){
        FuntimeChatroomMic mic = chatroomMicMapper.getRoomUserInfoByUserId(userId);
        return mic==null?null:mic.getRoomId();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public JSONObject roomJoin(Long userId, Long roomId, String password, Integer type) {
        JSONObject result = new JSONObject();
        //查询房间信息
        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        //用户信息
        FuntimeUser user = userService.queryUserById(userId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (user.getState()==2){
            throw new BusinessException(ErrorMsgEnum.USER_IS_DELETE.getValue(),ErrorMsgEnum.USER_IS_DELETE.getDesc());
        }
        //房间已封禁
        if (chatroom.getIsBlock().intValue()==1){
            log.info("roomJoin==========> {}",ErrorMsgEnum.ROOM_IS_BLOCK.getDesc());
            throw new BusinessException(ErrorMsgEnum.ROOM_IS_BLOCK.getValue(),ErrorMsgEnum.ROOM_IS_BLOCK.getDesc());
        }
        if (!userId.equals(chatroom.getUserId())){
            //房间已停播
            if (chatroom.getState() == 2){
                log.info("roomJoin==========> {}",ErrorMsgEnum.ROOM_IS_CLOSE.getDesc());
                throw new BusinessException(ErrorMsgEnum.ROOM_IS_CLOSE.getValue(),ErrorMsgEnum.ROOM_IS_CLOSE.getDesc());
            }
            if (type == null||type !=1) {
                //上锁的房间
                if (chatroom.getIsLock().intValue() == 1) {
                    //不是房主
                    //校验密码
                    if (StringUtils.isEmpty(password) || StringUtils.isEmpty(chatroom.getPassword())) {
                        log.info("roomJoin==========> {}", ErrorMsgEnum.ROOM_JOIN_PASS_EMPTY.getDesc());
                        throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_PASS_EMPTY.getValue(), ErrorMsgEnum.ROOM_JOIN_PASS_EMPTY.getDesc());
                    }
                    if (!password.equals(chatroom.getPassword())) {
                        log.info("roomJoin==========> {}", ErrorMsgEnum.ROOM_JOIN_PASS_ERROR.getDesc());
                        throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_PASS_ERROR.getValue(), ErrorMsgEnum.ROOM_JOIN_PASS_ERROR.getDesc());
                    }
                }
            }
            //校验是否在踢出房间范围
            Integer count = chatroomKickedRecordMapper.checkUserIsKickedOrNot(roomId,userId);

            if (count>0){
                log.info("roomJoin==========> {}",ErrorMsgEnum.ROOM_JOIN_USER_BLOCKED.getDesc());
                throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_USER_BLOCKED.getValue(),ErrorMsgEnum.ROOM_JOIN_USER_BLOCKED.getDesc());
            }
        }
        //用户所在的房间
        Long roomId2 = getRoomIdByUserId(userId);
        if (roomId2!=null) {
            //用户已有房间
            if (roomId.equals(roomId2)) {
                result.put("isOwer",true);
                return result;
            } else {
                log.info("************进入房间前已在别的房间,现在先退出别的房间*******************");
                //退房
                roomExit(userId, roomId2);

            }
        }
        //房主进房
        if (userId.equals(chatroom.getUserId())){
            //直接上10号麦
            FuntimeChatroomMic chatroomMic = getMicLocationUser(roomId,10);
            chatroomMicMapper.upperWheat(chatroomMic.getId(),userId);
            if (chatroom.getState() == 2){
                chatroomMapper.updateChatroomState(chatroom.getId(),1);
            }
        }else {
            //用户角色

            int userRole = UserRole.ROOM_NORMAL.getValue();
            if (userId.equals(chatroom.getUserId())) {
                userRole = UserRole.ROOM_CREATER.getValue();
            }
            //进入房间记录
            saveRoomMic(roomId,userId, userRole);
        }

        //房间人数+1
        updateOnlineNumPlus(roomId);

        //用户进入房间日志
        saveUserRoomLog(1,userId,roomId,null);
        roomJoinNotice(roomId,userId,user.getNickname(),chatroom.getOnlineNum()+1);
        sendRoomInfoNotice(roomId);
        result.put("isOwer",chatroom.getUserId().equals(userId));
        return result;
    }

    public FuntimeChatroomMic getMicLocationUser(Long roomId,Integer micLocation){
        return chatroomMicMapper.getMicLocationUser(roomId,micLocation);
    }

    public void roomJoinNotice(Long roomId,Long userId,String nickname,Integer onlineNum){
        //全房消息
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>1) {
            //发送通知
            noticeService.notice12(roomId, userId, nickname, userIds);
            //人数通知
            //noticeService.notice20(roomId, userIds, onlineNum);
        }
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
    public Map<String, Object> getRoomInfo(Long roomId, Long userId) {

        FuntimeChatroom chatroom = chatroomMapper.getRoomInfoById(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("chatroom",chatroom);

        List<Map<String, Object>> micUser = getMicUserByRoomId(roomId);

        result.put("mic",micUser);
        result.put("isRedpacketShow",parameterService.getParameterValueByKey("is_redpacket_show"));
        result.put("isFishShow",2);
        result.put("roomGameTag","");
        result.put("roomGameIcon","");
        result.put("shareUrl",Constant.SHARE_URL);
        if (userId!=null) {
            boolean bool1 = gameService.getYaoyaoShowConf(1, userId);
            boolean bool2 = gameService.getYaoyaoShowConf(2, userId);
            result.put("isGoldShow",bool1);
            result.put("isBlueShow", bool2);
            if (bool1||bool2){
                result.put("isYaoyaoShow",true);
            }else{
                result.put("isYaoyaoShow",false);
            }
        }
        return result;
    }

    public List<Map<String, Object>> getMicUserByRoomId(Long roomId){
        return chatroomMicMapper.getMicUserByRoomId(roomId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void roomExit(Long userId, Long roomId) {

        //用户是否存在
        FuntimeUser user = userService.queryUserById(userId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        //房间信息
        FuntimeChatroom chatroom = chatroomMapper.selectByPrimaryKey(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        FuntimeChatroomMic chatroomMic = getInfoByRoomIdAndUser(roomId, userId);
        if (chatroomMic==null){
            log.info("roomExit：{}",ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
            throw new BusinessException(ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
        }
        //用户是否在麦上
        Integer mic = chatroomMic.getMicLocation();
        if (mic!=null) {
            //在麦上先下麦
            lowerWheat(roomId, mic, userId);
        }else{
            //删除用户
            if (chatroomMic.getUserRole() == UserRole.ROOM_NORMAL.getValue()) {
                int k = chatroomMicMapper.deleteByPrimaryKey(chatroomMic.getId());
                if (k != 1) {
                    throw new BusinessException(ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
                }
            }
        }
        //房间人数-1
        updateOnlineNumSub(roomId);

        sendRoomInfoNotice(roomId);
    }

    public void roomExitNotice(Long userId, Long roomId,String nickname,Integer onlineNum) {
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&!userIds.isEmpty()) {
            Integer mic = getMicLocation(roomId, userId);
            if (mic != null) {
                //下麦通知
                noticeService.notice2(mic, roomId, userId, nickname, userIds, 1);
            }
            //通知人数
            noticeService.notice20(roomId, userIds, onlineNum);
        }
    }


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void roomKicked(Long kickIdUserId, Long userId, Long roomId) {
        //校验用户
        FuntimeUser user = userService.queryUserById(kickIdUserId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        //校验操作用户
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
        FuntimeChatroomMic chatroomMic = getInfoByRoomIdAndUser(roomId, kickIdUserId);
        if (chatroomMic==null){
            log.info("roomKicked：{}",ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
            throw new BusinessException(ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
        }
        Integer micLocation = chatroomMic.getMicLocation();
        if (micLocation!=null) {
            //麦上用户需要先下麦
            if (micLocation > 0) {
                lowerWheat(roomId, micLocation, kickIdUserId);
            }
        }else {
            //删除用户
            if (chatroomMic.getUserRole() == UserRole.ROOM_NORMAL.getValue()) {
                int k = chatroomMicMapper.deleteByPrimaryKey(chatroomMic.getId());
                if (k != 1) {
                    throw new BusinessException(ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
                }
            }
        }

        //保存踢人记录
        saveChatroomKickedRecord(kickIdUserId,userId,roomId);

        //房间人数-1
        updateOnlineNumSub(roomId);
        //发送通知
        noticeService.notice16(micLocation, roomId, kickIdUserId);

        //roomKickedNotice(roomId,micLocation,kickIdUserId,user.getNickname(),chatroom.getOnlineNum()-1);
        sendRoomInfoNotice(roomId);

    }

    public void roomKickedNotice(Long roomId,Integer mic,Long kickIdUserId,String nickname,Integer onlineNum){
        //人数通知
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&!userIds.isEmpty()) {
            if (mic!=null&&mic>0) {
                noticeService.notice2(mic, roomId, kickIdUserId, nickname, userIds, 1);
            }
            noticeService.notice20(roomId, userIds, onlineNum);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
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
        FuntimeChatroomMic chatroomMic = getMicLocationUser(roomId,micLocation);
        if (chatroomMic==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        if (chatroomMic.getMicUserId()!=null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_EXIST.getDesc());
        }
        Long micLocationId = getMicLocationId(roomId, micUserId);
        if (micLocationId!=null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_IS_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_IS_EXIST.getDesc());
        }
        //发送通知
        noticeService.notice15(micLocation,roomId,micUserId);

    }

    public Long getMicLocationId(Long roomId, Long micUserId){
        FuntimeChatroomMic micInfo = chatroomMicMapper.getMicLocationByRoomIdAndUser(roomId, micUserId);
        return micInfo == null?null:micInfo.getId();
    }
    public Integer getMicLocation(Long roomId, Long micUserId){
        FuntimeChatroomMic micInfo = chatroomMicMapper.getMicLocationByRoomIdAndUser(roomId, micUserId);
        return micInfo == null?null:micInfo.getMicLocation();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
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
        FuntimeChatroomMic chatroomMic = getMicLocationUser(roomId,micLocation);
        if (chatroomMic==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        if (chatroomMic.getMicUserId()!=null){
            log.info("上麦接口 upperWheat 失败 ：{}",ErrorMsgEnum.ROOM_MIC_USER_EXIST.getDesc());
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_EXIST.getDesc());
        }

        FuntimeChatroomMic chatroomMic2 = getInfoByRoomIdAndUser(roomId, micUserId);
        if (chatroomMic2 == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
        }
        if (chatroomMic2.getMicLocation()!=null){
            chatroomMicMapper.lowerWheat(chatroomMic2.getId());
        }

        if (chatroomMic2.getMicLocation()==null){
            chatroomMicMapper.deleteByPrimaryKey(chatroomMic2.getId());
        }
        int k = chatroomMicMapper.upperWheat(chatroomMic.getId(),micUserId);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        sendRoomInfoNotice(roomId);

    }

    public void upperWheatNotice(Long roomId,Integer mic,Long micUserId,String nickname,String portraitAddress,Integer sex,String levelUrl){
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&!userIds.isEmpty()) {
            //发送通知
            noticeService.notice1(mic, roomId, micUserId, nickname, portraitAddress, userIds, sex, levelUrl);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
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

        Integer micLocation = getMicLocation(roomId, micUserId);
        if (micLocation == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }
        if (micLocation.intValue() == 10){
            return;
        }
        lowerWheat(roomId,micLocation,micUserId);
        saveRoomMic(roomId,micUserId,UserRole.ROOM_NORMAL.getValue());

        lowerWheatNotice(roomId,micLocation,micUserId,user.getNickname(),isMe);
        sendRoomInfoNotice(roomId);
    }

    public void lowerWheatNotice(Long roomId,Integer mic,Long micUserId,String nickname,int isMe){
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice2(mic, roomId, micUserId, nickname, userIds,isMe);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void stopWheat(Long userId, Long roomId, Integer micLocation) {
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_5.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }

        FuntimeChatroomMic micLocationUser = getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }

        int k = chatroomMicMapper.stopWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        if (micLocationUser.getMicUserId()!=null){
            saveRoomMic(roomId,micLocationUser.getMicUserId(),UserRole.ROOM_NORMAL.getValue());
        }
        /*
        if (micLocationUser.getMicUserId()!=null){
            Long id = chatroomUserMapper.checkUserIsExist(roomId, micLocationUser.getMicUserId());
            k = chatroomUserMapper.updateUserRoleById(id,UserRole.ROOM_NORMAL.getValue());
            if(k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
        }*/
        sendRoomInfoNotice(roomId);

    }

    public void stopWheatNotice(Long roomId,Integer mic){
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice3(mic, roomId,userIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void forbidWheat(Long roomId, Integer micLocation, Long userId){
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_6.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        FuntimeChatroomMic micLocationUser = getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        int k = chatroomMicMapper.forbidWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        sendRoomInfoNotice(roomId);
    }

    public void forbidWheatNotice(Long roomId,Integer mic){
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice5(mic, roomId,userIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void openWheat(Long userId, Long roomId, Integer micLocation) {
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_11.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        FuntimeChatroomMic micLocationUser = getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        int k = chatroomMicMapper.openWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        sendRoomInfoNotice(roomId);

    }
    public void openWheatNotice(Long roomId,Integer mic){
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice4(mic, roomId,userIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void releaseWheat(Long roomId, Integer micLocation, Long userId) {
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_12.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        FuntimeChatroomMic micLocationUser = getMicLocationUser(roomId, micLocation);
        if (micLocationUser==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }

        int k = chatroomMicMapper.releaseWheat(micLocationUser.getId());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        sendRoomInfoNotice(roomId);

    }
    public void releaseWheatNotice(Long roomId,Integer mic){
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice6(mic, roomId,userIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void roomClose(Long userId, Long roomId) {
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_2.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        if (chatroomMapper.checkRoomExists(roomId)==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<String> userIds = getRoomUserByRoomIdAll(roomId);

        chatroomMapper.deleteByRoomId(roomId);
        deleteByRoomId(roomId);
        //chatroomUserMapper.deleteByRoomId(roomId);

        if (userIds!=null&&!userIds.isEmpty()) {
            //发送通知
            noticeService.notice7(roomId,userIds);

        }


    }

    public void deleteByRoomId(Long roomId){
        chatroomMicMapper.deleteByRoomId(roomId);
        chatroomMicMapper.updateMicByRoomId(roomId);
    }


    public void blockUserForCloseRoom(Long roomId){
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        chatroomMapper.deleteByRoomId(roomId);
        deleteByRoomId(roomId);
        //chatroomUserMapper.deleteByRoomId(roomId);

        if (userIds!=null&&!userIds.isEmpty()) {
            //发送通知
            noticeService.notice30(roomId,userIds);

        }
    }

    @Override
    public PageInfo<Map<String, Object>> getRoomList(Integer startPage, Integer pageSize, Integer tagId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String,Object>> list = chatroomMapper.getRoomList(tagId);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{

            return new PageInfo<>(list);
        }
    }

    @Override
    public PageInfo<Map<String, Object>> getRoomUserById(Integer startPage,Integer pageSize,Long roomId,String nickname) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String, Object>> list = chatroomMicMapper.getRoomUserById(roomId, nickname);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else {

            return new PageInfo<>(list);
        }
    }

    @Override
    public PageInfo<Map<String, Object>> getRoomUserByIdAll(Integer startPage,Integer pageSize,Long roomId,String nickname) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String, Object>> list = chatroomMicMapper.getRoomUserByIdAll(roomId, nickname);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else {

            return new PageInfo<>(list);
        }
    }

    @Override
    public List<String> getRoomUserByRoomIdAll(Long roomId) {
        return chatroomMicMapper.getRoomUserByRoomIdAll(roomId);
    }

    @Override
    public FuntimeChatroom getChatroomById(Long roomId) {
        return chatroomMapper.selectByPrimaryKey(roomId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
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
        Integer micLocation = getMicLocation(roomId, micUserId);
        if (micLocation == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }
        FuntimeChatroomMic micLocationUser = getMicLocationUser(roomId, micLocation);
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

        List<String> userIds = getRoomUserByRoomIdAll(roomId);

        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice17(micLocation, roomId,userIds,micUserId,user.getNickname());
        }

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
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

        Integer micLocation = getMicLocation(roomId, micUserId);
        if (micLocation == null){
            throw  new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }

        FuntimeChatroomMic micLocationUser = getMicLocationUser(roomId, micLocation);
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


        List<String> userIds = getRoomUserByRoomIdAll(roomId);

        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice18(micLocation, roomId,userIds,micUserId,user.getNickname());
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
        Integer micLocation = getMicLocation(roomId, micUserId);
        if (micLocation == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice10(micLocation, roomId,userIds,micUserId,user.getNickname(),mic);
        }

        return mic;
    }

    @Override
    public void sendNotice(Long userId, String imgUrl, String msg, Long roomId, Integer type) {
        FuntimeUser user = userService.getUserBasicInfoById(userId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (user.getOnlineState() == 2){
            throw new BusinessException(ErrorMsgEnum.USER_IS_OFFLINE.getValue(),ErrorMsgEnum.USER_IS_OFFLINE.getDesc());
        }
        Integer userRole = getUserRole(roomId,userId);
        userRole = userRole == null?4:userRole;
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        noticeService.notice11Or14(userId,imgUrl,msg,roomId,type,userIds,userRole);
    }

    @Override
    public PageInfo<Map<String, Object>> getRoomLogList(Integer startPage, Integer pageSize, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String,Object>> list = chatroomMapper.getRoomLogList(userId);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{

            return new PageInfo<>(list);
        }
    }

    @Override
    public List<FuntimeGift> getGiftListByBestowed(Integer bestowed) {
        List<FuntimeGift> list = giftMapper.getGiftListByBestowed(bestowed);
        if (list==null||list.isEmpty()){
            return null;
        }
        return list;
    }

    @Override
    public Map<String, Object> getGiftList() {

        Map<String,Object> result = new HashMap<>();

        List<Map<String,Object>> list = giftMapper.getGiftList();

        if (list!=null&&!list.isEmpty()){

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
    public List<Long> getRoomUserByRoomId(Long roomId, Long userId) {
        return chatroomMicMapper.getRoomUserByRoomId(roomId,userId);
    }

    @Override
    public List<String> getAllRoomUser() {
        return chatroomMicMapper.getAllRoomUser();
    }

    public Integer getUserRole(Long roomId,Long userId){
        FuntimeChatroomMic micUser = chatroomMicMapper.getMicLocationByRoomIdAndUser(roomId, userId);
        return micUser == null?null:micUser.getUserRole();
    }

    @Override
    public FuntimeChatroom getRoomByUserId(Long userId) {
        return chatroomMapper.getRoomByUserId(userId);
    }



    @Override
    public Long checkUserIsInRoom(Long userId) {
        return getRoomIdByUserId(userId);
    }

    @Override
    public Long checkUserIsInMic(Long userId) {
        return chatroomMicMapper.checkUserIsInMic(userId);
    }

    @Override
    public List<Long> getMicUserIdByRoomId(Long roomId, Long userId) {
        return chatroomMicMapper.getMicUserIdByRoomId(roomId,userId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void roomExitTask(Long userId) {
        Long roomId = checkUserIsInMic(userId);
        if (roomId!=null) {
            log.info("offlineUserTask======>roomExitTask:userId:{}",userId);
            lowerWheat(null, roomId, userId);
            userService.updateImHeartSync(userId);
        }
    }

    @Override
    public PageInfo<Map<String, Object>> getBackgroundList(Integer startPage, Integer pageSize, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String,Object>> list = backgroundMapper.getBackgroundList(userId);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{

            return new PageInfo<>(list);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> buyBackground(Integer backgroundId, Long userId) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);
        if (userAccount==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        Map<String, Object> map = backgroundMapper.getBackgroundInfoById(backgroundId, userId);
        if (map == null||map.isEmpty()){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
        }
        BigDecimal price = new BigDecimal(map.get("price").toString());
        Integer type = Integer.parseInt(map.get("type").toString());
        Integer days = Integer.parseInt(map.get("days").toString());

        if (type == 1){
            throw new BusinessException(ErrorMsgEnum.ROOM_BACKGROUND_NOBUY.getValue(),ErrorMsgEnum.ROOM_BACKGROUND_NOBUY.getDesc());
        }

        if (userAccount.getBlueDiamond().subtract(price).doubleValue()<0){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            map = new HashMap<>();
            map.put("userBlueAmount",userAccount.getBlueDiamond().intValue());
            map.put("price",price.intValue());
            resultMsg.setData(map);
            return resultMsg;
        }
        FuntimeUserBackground userBackground = new FuntimeUserBackground();
        userBackground.setBackgroundId(backgroundId);
        userBackground.setUserId(userId);
        if (type==3){
            userBackground.setDays(days);
            userBackground.setEndTime(DateUtils.addDays(new Date(),days));
        }

        userBackground.setBackgroundType(type);
        userBackground.setPrice(price);
        int k ;
        if (map.get("ubId")==null){
            //没买过
            k = backgroundMapper.insertUserBackground(userBackground);
            if(k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
        }else{
            Long ubId = Long.parseLong(map.get("ubId").toString());
            userBackground.setId(ubId);
            k = backgroundMapper.updateUserBackground(userBackground);
            if(k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
            userBackground.setId(null);
        }
        k = backgroundMapper.insertUserBackgroundRecord(userBackground);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        userService.updateUserAccountForSub(userId,null,price,null);
        accountService.saveUserAccountBlueLog(userId,price,userBackground.getId(),OperationType.BUY_BACKGROUND.getAction(),OperationType.BUY_BACKGROUND.getOperationType());
        return resultMsg;
    }

    @Override
    public void drawBackground(Integer backgroundId, Long userId) {

        Map<String, Object> map = backgroundMapper.getBackgroundInfoById(backgroundId, userId);
        if (map == null||map.isEmpty()){
            return;
        }
        BigDecimal price = new BigDecimal(map.get("price").toString());
        Integer type = Integer.parseInt(map.get("type").toString());
        Integer days = Integer.parseInt(map.get("days").toString());

        if (type == 1){
            return;
        }

        FuntimeUserBackground userBackground = new FuntimeUserBackground();
        userBackground.setBackgroundId(backgroundId);
        userBackground.setUserId(userId);
        if (type==3){
            userBackground.setDays(days);
            userBackground.setEndTime(DateUtils.addDays(new Date(),days));
        }

        userBackground.setBackgroundType(type);
        userBackground.setPrice(price);
        int k ;
        if (map.get("ubId")==null){
            //没买过
            k = backgroundMapper.insertUserBackground(userBackground);
            if(k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
        }else{
            Long ubId = Long.parseLong(map.get("ubId").toString());
            userBackground.setId(ubId);
            k = backgroundMapper.updateUserBackground(userBackground);
            if(k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
            userBackground.setId(null);
        }
        k = backgroundMapper.insertUserBackgroundRecord(userBackground);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    public FuntimeChatroomMic getRoomUserInfoByUserId(Long userId){
        return chatroomMicMapper.getRoomUserInfoByUserId(userId);
    }



    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void setBackground(Integer backgroundId, Long userId, Long roomId) {
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_14.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }
        FuntimeChatroom chatroom = getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        Map<String, Object> map = backgroundMapper.getBackgroundUrlById(backgroundId, userId);
        if (map == null||map.isEmpty()){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
        }
        String backgroundUrl = map.get("backgroundUrl").toString();
        String backgroundUrl2 = map.get("backgroundUrl2").toString();
        Integer type = Integer.parseInt(map.get("type").toString());
        Integer isOwner = Integer.parseInt(map.get("isOwner").toString());
        if (type!=1&&isOwner == 0){
            throw new BusinessException(ErrorMsgEnum.ROOM_BACKGROUND_ERROR.getValue(),ErrorMsgEnum.ROOM_BACKGROUND_ERROR.getDesc());
        }

        if (chatroom.getBackgroundId()==null||!backgroundId.equals(chatroom.getBackgroundId())) {
            int k = chatroomMapper.updateChatroomBackgroundId(roomId, backgroundId);
            if (k != 1) {
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(), ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
            List<String> userIds = getRoomUserByRoomIdAll(roomId);
            //发送通知
            if (userIds!=null&&!userIds.isEmpty()) {
                noticeService.notice31(roomId,userId,backgroundUrl,userIds,backgroundUrl2);
            }

        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void setBackgroundTask() {
        List<Map<String,Long>> roomIdsMap = backgroundMapper.getBackgroundForExpiry();
        if (roomIdsMap!=null&&!roomIdsMap.isEmpty()){
            for (Map<String,Long> roomMap : roomIdsMap) {
                Long roomId = roomMap.get("id");
                Long userId = roomMap.get("userId");
                Long ubId = roomMap.get("ubId");
                Map<String, Object> map = backgroundMapper.getBackgroundUrlForType1();
                String backgroundUrl = map.get("backgroundUrl").toString();
                String backgroundUrl2 = map.get("backgroundUrl2").toString();
                chatroomMapper.updateChatroomBackgroundId(roomId, Integer.parseInt(map.get("id").toString()));
                backgroundMapper.deleteUserBackgroundById(ubId);
                List<String> userIds = getRoomUserByRoomIdAll(roomId);
                //发送通知
                if (userIds!=null&&!userIds.isEmpty()) {
                    noticeService.notice31(roomId, userId, backgroundUrl, userIds, backgroundUrl2);
                }
            }
        }
    }

    @Override
    public void updateOnlineNumTask() {
        chatroomMapper.updateOnlineNumTask();
    }



    @Override
    public void sendRoomMicInfoTask() {
        List<Long> allRoom = chatroomMapper.getAllRoom();
        if (allRoom!=null&&!allRoom.isEmpty()){
            List<Map<String, Object>> micUser;
            List<String> userIds;
            for (Long roomId : allRoom){
                userIds = getRoomUserByRoomIdAll(roomId);
                micUser = getMicUserByRoomId(roomId);
                if (userIds!=null&&!userIds.isEmpty()) {
                    noticeService.notice32(userIds, micUser, userIds.size());
                }
            }
        }

    }

    @Override
    public void sendRoomInfoNotice(Long roomId) {
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&!userIds.isEmpty()) {
            List<Map<String, Object>> micUser = getMicUserByRoomId(roomId);
            noticeService.notice32(userIds, micUser,userIds.size());
        }
    }

    @Override
    public String getBackgroundThumbnailById(Integer id) {
        return backgroundMapper.getBackgroundThumbnailById(id);
    }

    public FuntimeChatroomMic getInfoByRoomIdAndUser(Long roomId,Long userId){
        return chatroomMicMapper.getInfoByRoomIdAndUser(roomId,userId);
    }

    private void updateChatroomBlock(Long roomId, int isBlock) {
        int k = chatroomMapper.updateChatroomBlock(roomId,isBlock);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
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

        FuntimeChatroomMic micLocationUser = getMicLocationUser(roomId, micLocation);
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

    /*
    public void saveChatroomUser(long userId,long roomId,int userRole){
        FuntimeChatroomUser chatroomUser = new FuntimeChatroomUser();
        chatroomUser.setUserId(userId);
        chatroomUser.setRoomId(roomId);
        chatroomUser.setUserRole(userRole);
        int k = chatroomUserMapper.insertSelective(chatroomUser);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }*/



    public void updateChatroom(FuntimeChatroom chatroom){

        if (StringUtils.isEmpty(chatroom.getPassword())){
            chatroom.setIsLock(2);
        }else{
            chatroom.setIsLock(1);
        }

        int k = chatroomMapper.updateByPrimaryKeySelective(chatroom);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

    }

    public void saveMicBatch(Long roomId, int num, Long userId){
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
    public void saveRoomMic(Long roomId, Long userId,Integer userRole){
        FuntimeChatroomMic chatroomMic = new FuntimeChatroomMic();

        chatroomMic.setRoomId(roomId);
        chatroomMic.setMicUserId(userId);
        chatroomMic.setUserRole(userRole);

        chatroomMicMapper.insertSelective(chatroomMic);

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

}
