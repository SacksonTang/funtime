package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.*;

import com.rzyou.funtime.utils.UsersigUtil;
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
    FuntimeChatroomUserMapper chatroomUserMapper;
    @Autowired
    FuntimeChatroomKickedRecordMapper chatroomKickedRecordMapper;
    @Autowired
    FuntimeGiftMapper giftMapper;
    @Autowired
    FuntimeTagMapper tagMapper;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Long roomCreate(Long userId, Integer platform) {
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
            if (platform == 0) {
                roomJoin(userId, chatroom.getId(), null, null);
            }
            return roomId;
        }
        //用户所在的房间
        Long roomId2 = chatroomUserMapper.getRoomByUserId(userId);

        //用户已有房间
        if (roomId2!=null){
            log.info("************进入房间前已在别的房间,现在先退出别的房间*******************");
            //退房
            roomExit(userId, roomId2);
        }

        userService.updateCreateRoomPlus(userId);

        roomId = saveChatroom(userId,user.getNickname(),user.getPortraitAddress());

        saveMic(roomId,10,userId);

        saveChatroomUser(userId,roomId,UserRole.ROOM_CREATER.getValue());

        return roomId;
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

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean roomJoin(Long userId, Long roomId, String password, Integer type) {
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
        Long roomId2 = chatroomUserMapper.getRoomByUserId(userId);
        if (roomId2!=null) {
            //用户已有房间
            if (roomId.equals(roomId2)) {
                return true;
            } else {
                log.info("************进入房间前已在别的房间,现在先退出别的房间*******************");
                //退房
                roomExit(userId, roomId2);
            }
        }
        //房主进房
        if (userId.equals(chatroom.getUserId())){
            //直接上10号麦
            FuntimeChatroomMic chatroomMic = chatroomMicMapper.getMicLocationUser(roomId,10);
            chatroomMicMapper.upperWheat(chatroomMic.getId(),userId);
            if (chatroom.getState() == 2){
                chatroomMapper.updateChatroomState(chatroom.getId(),1);
            }
        }
        //用户角色
        int userRole = UserRole.ROOM_NORMAL.getValue();
        if (userId.equals(chatroom.getUserId())){
            userRole = UserRole.ROOM_CREATER.getValue();
        }

        //进入房间记录
        saveChatroomUser(userId,roomId,userRole);

        //房间人数+1
        updateOnlineNumPlus(roomId);

        //用户进入房间日志
        saveUserRoomLog(1,userId,roomId,null);

        //全房消息
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>1) {
            //发送通知
            noticeService.notice12(roomId, userId, user.getNickname(), userIds);
            //人数通知
            noticeService.notice20(roomId, userIds, chatroom.getOnlineNum() + 1);
        }
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
    public Map<String, Object> getRoomInfo(Long roomId, Long userId) {

        FuntimeChatroom chatroom = chatroomMapper.getRoomInfoById(roomId);
        if (chatroom==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("chatroom",chatroom);

        List<Map<String, Object>> micUser = chatroomMicMapper.getMicUserByRoomId(roomId);

        result.put("mic",micUser);
        result.put("isRedpacketShow",parameterService.getParameterValueByKey("is_redpacket_show"));
        result.put("isFishShow",parameterService.getParameterValueByKey("is_fish_show"));
        result.put("roomGameTag",parameterService.getParameterValueByKey("room_game_tag"));
        result.put("roomGameIcon",parameterService.getParameterValueByKey("room_game_icon"));
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
        Long id = chatroomUserMapper.checkUserIsExist(roomId,userId);
        if (id==null){
            log.info("deleteChatroomUser：{}",ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
            throw new BusinessException(ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
        }

        //用户是否在麦上
        Integer mic = chatroomMicMapper.getMicLocation(roomId, userId);

        if (mic!=null) {
            //在麦上先下麦
            lowerWheat(roomId, mic, userId);
        }
        //房间人数-1
        updateOnlineNumSub(roomId);
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
        //删除用户
        int k = chatroomUserMapper.deleteByPrimaryKey(id);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
        }
        if (userIds!=null&&!userIds.isEmpty()) {
            if (mic != null) {
                //下麦通知
                noticeService.notice2(mic, roomId, userId, user.getNickname(), userIds, 1);
            }
            //通知人数
            noticeService.notice20(roomId, userIds, chatroom.getOnlineNum() - 1);
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

        Integer micLocation = chatroomMicMapper.getMicLocation(roomId, kickIdUserId);
        if (micLocation!=null) {

            //麦上用户需要先下麦
            if (micLocation > 0) {
                lowerWheat(roomId, micLocation, kickIdUserId);
                //下麦通知
                List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
                noticeService.notice2(micLocation, roomId, kickIdUserId, user.getNickname(), userIds, 1);
            }
        }

        //删除用户
        Long id = chatroomUserMapper.checkUserIsExist(roomId,kickIdUserId);

        if (id!=null){
            chatroomUserMapper.deleteByPrimaryKey(id);
        }

        //保存踢人记录
        saveChatroomKickedRecord(kickIdUserId,userId,roomId);

        //房间人数-1
        updateOnlineNumSub(roomId);

        //人数通知
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
        noticeService.notice20(roomId,userIds,chatroom.getOnlineNum()-1);
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
        FuntimeChatroomMic chatroomMic = chatroomMicMapper.getMicLocationUser(roomId,micLocation);
        if (chatroomMic==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        if (chatroomMic.getMicUserId()!=null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_EXIST.getDesc());
        }
        Long micLocationId = chatroomMicMapper.getMicLocationId(roomId, micUserId);
        if (micLocationId!=null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_IS_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_IS_EXIST.getDesc());
        }
        //发送通知
        noticeService.notice15(micLocation,roomId,micUserId);

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
        FuntimeChatroomMic chatroomMic = chatroomMicMapper.getMicLocationUser(roomId,micLocation);
        if (chatroomMic==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_LOCATION_NOT_EXIST.getDesc());
        }
        if (chatroomMic.getMicUserId()!=null){
            log.info("上麦接口 upperWheat 失败 ：{}",ErrorMsgEnum.ROOM_MIC_USER_EXIST.getDesc());
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_EXIST.getDesc());
        }

        Long micLocationId = chatroomMicMapper.getMicLocationId(roomId, micUserId);
        if (micLocationId!=null){
            chatroomMicMapper.lowerWheat(micLocationId);
        }

        int k = chatroomMicMapper.upperWheat(chatroomMic.getId(),micUserId);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        Long id = chatroomUserMapper.checkUserIsExist(roomId, micUserId);
        if (id == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_EXIT_USER_NOT_EXISTS.getDesc());
        }
        k = chatroomUserMapper.updateUserRoleById(id,UserRole.ROOM_MIC.getValue());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
        FuntimeUserAccount userAccount = userService.getUserAccountInfoById(micUserId);
        if (userIds!=null&&!userIds.isEmpty()) {
            //发送通知
            noticeService.notice1(micLocation, roomId, micUserId, user.getNickname(), user.getPortraitAddress(), userIds, user.getSex(), userAccount.getLevelUrl());
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

        Integer micLocation = chatroomMicMapper.getMicLocation(roomId, micUserId);
        if (micLocation == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }
        if (micLocation.intValue() == 10){
            return;
        }
        lowerWheat(roomId,micLocation,micUserId);
        int k = chatroomUserMapper.updateUserRoleById(chatroomUserMapper.checkUserIsExist(roomId,micUserId),UserRole.ROOM_NORMAL.getValue());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);

        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice2(micLocation, roomId, micUserId, user.getNickname(), userIds,isMe);
        }


    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
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
        if (micLocationUser.getMicUserId()!=null){
            Long id = chatroomUserMapper.checkUserIsExist(roomId, micLocationUser.getMicUserId());
            k = chatroomUserMapper.updateUserRoleById(id,UserRole.ROOM_NORMAL.getValue());
            if(k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
        }
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice3(micLocation, roomId,userIds);
        }


    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
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

        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);

        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice5(micLocation, roomId,userIds);
        }

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
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

        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);

        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice4(micLocation, roomId,userIds);
        }


    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
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
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);

        //发送通知
        if (userIds!=null&&!userIds.isEmpty()) {
            noticeService.notice6(micLocation, roomId,userIds);
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
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);

        chatroomMapper.deleteByRoomId(roomId);
        chatroomMicMapper.deleteByRoomId(roomId);
        chatroomUserMapper.deleteByRoomId(roomId);

        if (userIds!=null&&!userIds.isEmpty()) {
            //发送通知
            noticeService.notice7(roomId,userIds);

        }

    }

    public void blockUserForCloseRoom(Long roomId){
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
        chatroomMapper.deleteByRoomId(roomId);
        chatroomMicMapper.deleteByRoomId(roomId);
        chatroomUserMapper.deleteByRoomId(roomId);

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
        List<Map<String, Object>> list = chatroomUserMapper.getRoomUserById(roomId, nickname);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else {

            return new PageInfo<>(list);
        }
    }

    @Override
    public PageInfo<Map<String, Object>> getRoomUserByIdAll(Integer startPage,Integer pageSize,Long roomId,String nickname) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String, Object>> list = chatroomUserMapper.getRoomUserByIdAll(roomId, nickname);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else {

            return new PageInfo<>(list);
        }
    }

    @Override
    public List<String> getRoomUserByRoomIdAll(Long roomId) {
        return chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
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
        k = chatroomUserMapper.updateUserRoleById(chatroomUserMapper.checkUserIsExist(roomId, micUserId),UserRole.ROOM_CHAIR.getValue());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);

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
        k = chatroomUserMapper.updateUserRoleById(chatroomUserMapper.checkUserIsExist(roomId,micUserId),UserRole.ROOM_NORMAL.getValue());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);

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
        Integer micLocation = chatroomMicMapper.getMicLocation(roomId, micUserId);
        if (micLocation == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getValue(),ErrorMsgEnum.ROOM_MIC_USER_NOT_EXIST.getDesc());
        }
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
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
        List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
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
        return chatroomUserMapper.getRoomUserByRoomId(roomId,userId);
    }

    @Override
    public List<String> getAllRoomUser() {
        return chatroomUserMapper.getAllRoomUser();
    }

    public Integer getUserRole(Long roomId,Long userId){
        return chatroomMicMapper.getMicLocationUserRole(roomId, userId);
    }

    @Override
    public FuntimeChatroom getRoomByUserId(Long userId) {
        return chatroomMapper.getRoomByUserId(userId);
    }

    @Override
    public boolean checkUserIsExist(Long roomId, Long userId) {
        Long id = chatroomUserMapper.checkUserIsExist(roomId, userId);
        if (id == null){
            return false;
        }
        return true;
    }

    @Override
    public Long checkUserIsInRoom(Long userId) {
        return chatroomUserMapper.getRoomByUserId(userId);
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
            lowerWheat(null,roomId,userId);
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
    public void blockUserForRoom(Long userId) {
        FuntimeChatroomUser chatroomUser = chatroomUserMapper.getRoomUserInfoByUserId(userId);
        if (chatroomUser == null){
            noticeService.notice24(userId);
            return;
        }
        if (chatroomUser.getUserRole() == UserRole.ROOM_CREATER.getValue()){
            //房主需解散房间
            blockUserForCloseRoom(chatroomUser.getRoomId());
            return;
        }else{
            roomExit(userId,chatroomUser.getRoomId());
            noticeService.notice24(userId);
            return;
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void blockRoom(Long roomId) {
        FuntimeChatroom chatroom = getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        updateChatroomBlock(roomId,1);
        if (chatroom.getOnlineNum()>0) {
            List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
            chatroomMapper.deleteByRoomId(roomId);
            chatroomMicMapper.deleteByRoomId(roomId);
            chatroomUserMapper.deleteByRoomId(roomId);

            if (userIds!=null&&!userIds.isEmpty()) {
                //发送通知
                noticeService.notice23(roomId, userIds);
            }
        }
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
            List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
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
                List<String> userIds = chatroomUserMapper.getRoomUserByRoomIdAll(roomId);
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

    public void saveChatroomUser(long userId,long roomId,int userRole){
        FuntimeChatroomUser chatroomUser = new FuntimeChatroomUser();
        chatroomUser.setUserId(userId);
        chatroomUser.setRoomId(roomId);
        chatroomUser.setUserRole(userRole);
        int k = chatroomUserMapper.insertSelective(chatroomUser);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }



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

}
