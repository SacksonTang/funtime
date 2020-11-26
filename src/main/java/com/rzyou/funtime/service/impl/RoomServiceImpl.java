package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.component.RedisUtil;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.*;

import com.rzyou.funtime.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
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
    Game21Service game21Service;
    @Autowired
    Game123Service game123Service;
    @Autowired
    RedisUtil redisUtil;

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
    @Autowired
    FuntimeChatroomManagerMapper chatroomManagerMapper;
    @Autowired
    Funtime1v1PrivateMapper privateMapper;

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
            roomJoin(userId, chatroom.getId(), null, null, null);
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
        chatroom.setHots(5);
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
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS);
        }
        userService.checkSensitive(chatroom.getName());
        userService.checkSensitive(chatroom.getExamDesc());
        /*
        if(!userService.checkAuthorityForUserRole(userRole, UserRoleAuthority.A_1.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/
        FuntimeChatroom chatroom1 = chatroomMapper.selectByPrimaryKey(chatroom.getId());
        if (!chatroom1.getUserId().equals(chatroom.getUserId())){
            throw new BusinessException(ErrorMsgEnum.ROOM_CREATER_ERROR);
        }
        if (chatroom.getPrivateState()!=null&&chatroom.getPrivateState() == 1){
            chatroom.setMicCounts(2);
            List<Long> users = getRoomUserByRoomId(chatroom.getId(), chatroom.getUserId());
            if (users!=null&&!users.isEmpty()){
                throw new BusinessException(ErrorMsgEnum.ROOM_PRIVATE_CHANGE_ERROR);
            }
            if (StringUtils.isNotBlank(chatroom.getPassword())){
                throw new BusinessException(ErrorMsgEnum.ROOM_UPDATE_MATCH_FAIL);
            }
        }

        if (chatroom.getMicCounts()!=null&&!chatroom1.getMicCounts().equals(chatroom.getMicCounts())){
            Integer counts = chatroomMicMapper.checkMicChange(chatroom.getId());
            if (counts>0){
                throw new BusinessException(ErrorMsgEnum.ROOM_MIC_CHANGE_ERROR);
            }
            List<String> userIds = getRoomUserByRoomIdAll(chatroom.getId());
            if (userIds!=null&&userIds.size()>0) {
                noticeService.notice45(userIds,chatroom.getUserId(),chatroom.getId(),chatroom.getMicCounts());
            }
        }

        updateChatroom(chatroom);

    }

    @Override
    public Long getRoomIdByUserId(Long userId){
        FuntimeChatroomMic mic = chatroomMicMapper.getRoomUserInfoByUserId(userId);
        return mic==null?null:mic.getRoomId();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> roomJoin(Long userId, Long roomId, String password, Integer type, Integer priceId) {
        ResultMsg<Object> resultObj = new ResultMsg<>();
        Map<String,Object> result = new HashMap<>();
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
        if (chatroom.getIsBlock()==1){
            log.info("roomJoin==========> {}",ErrorMsgEnum.ROOM_IS_BLOCK.getDesc());
            throw new BusinessException(ErrorMsgEnum.ROOM_IS_BLOCK.getValue(),ErrorMsgEnum.ROOM_IS_BLOCK.getDesc());
        }
        if (!userId.equals(chatroom.getUserId())){
            //房间已停播
            if (chatroom.getState() == 2){
                log.info("roomJoin==========> {}",ErrorMsgEnum.ROOM_IS_CLOSE.getDesc());
                throw new BusinessException(ErrorMsgEnum.ROOM_IS_CLOSE.getValue(),ErrorMsgEnum.ROOM_IS_CLOSE.getDesc());
            }
            //if (type == null||type !=1) {
                //上锁的房间
            if (chatroom.getIsLock() == 1) {
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
            //}
            //校验是否在踢出房间范围
            Integer count = chatroomKickedRecordMapper.checkUserIsKickedOrNot(roomId,userId);

            if (count>0){
                log.info("roomJoin==========> {}",ErrorMsgEnum.ROOM_JOIN_USER_BLOCKED.getDesc());
                throw new BusinessException(ErrorMsgEnum.ROOM_JOIN_USER_BLOCKED.getValue(),ErrorMsgEnum.ROOM_JOIN_USER_BLOCKED.getDesc());
            }

            //是否是匹配房
            if (chatroom.getPrivateState() == 1){
                Integer roomUserCounts = chatroomMicMapper.getRoomUserCounts(roomId);
                if (roomUserCounts>1){
                    resultObj = new ResultMsg<>(ErrorMsgEnum.ROOM_COUNTS_FULL);
                    return resultObj;
                }
                Map<String, Object> priceMap = privateMapper.get1v1priceById(priceId);
                if (priceId == null||priceMap==null){
                    resultObj = new ResultMsg<>(ErrorMsgEnum.ROOM_PRIVATE_PRICE_NOT_EXIST);
                    resultObj.setData(get1v1price(userId));
                    return resultObj;
                }
                FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);
                Integer priceType = Integer.parseInt(priceMap.get("priceType").toString());
                Integer price = Integer.parseInt(priceMap.get("amount").toString());
                BigDecimal amount;
                if (priceType == 1){
                    amount = userAccount.getGoldCoin();
                    if (amount.subtract(new BigDecimal(price)).intValue()<0){
                        resultObj = new ResultMsg<>(ErrorMsgEnum.USER_ACCOUNT_GOLD_NOT_EN);
                        Map<String,Object> map = new HashMap<>();
                        map.put("userBlueAmount",userAccount.getGoldCoin().intValue());
                        map.put("price",price);
                        resultObj.setData(map);
                        return resultObj;
                    }
                }else{
                    amount = userAccount.getBlueDiamond();
                    if (amount.subtract(new BigDecimal(price)).intValue()<0) {
                        resultObj = new ResultMsg<>(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN);
                        Map<String, Object> map = new HashMap<>();
                        map.put("userBlueAmount", userAccount.getBlueDiamond().intValue());
                        map.put("price", price);
                        resultObj.setData(map);
                        return resultObj;
                    }
                }
                join1v1Room(userId,priceType,price,chatroom.getUserId(),roomId);
            }
        }

        //用户所在的房间
        Long roomId2 = getRoomIdByUserId(userId);
        if (roomId2!=null) {
            //用户已有房间
            if (roomId.equals(roomId2)) {
                result.put("isOwer",true);
                resultObj.setData(result);
                return resultObj;
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
            //房间人数+1
            updateOnlineNumPlus(roomId,5);
        }else {
            //用户角色

            int userRole = UserRole.ROOM_NORMAL.getValue();
            if (userId.equals(chatroom.getUserId())) {
                userRole = UserRole.ROOM_CREATER.getValue();
            }
            //进入房间记录
            saveRoomMic(roomId,userId, userRole);
            //房间人数+1
            updateOnlineNumPlus(roomId,user.getSex() == 2?3:2);
        }

        //用户进入房间日志
        saveUserRoomLog(1,userId,roomId,null);
        String carUrl = null;
        String carName ;
        String msg = "进入房间";
        String animationType = null;
        if (user.getCarId()!=null){
            Map<String, Object> carInfoMap = accountService.getCarInfoByCarId(user.getCarId());
            if (carInfoMap !=null&&carInfoMap.get("carUrl")!=null){
                carUrl = carInfoMap.get("carUrl").toString();
                carName = carInfoMap.get("carName").toString();
                msg = "坐着"+carName+"进来了";
                animationType = carInfoMap.get("animationType").toString();
            }
        }

        game123Service.setExitTimeByJoin(userId,roomId);
        roomJoinNotice(roomId,userId,user.getNickname(),carUrl,msg,animationType);
        sendRoomInfoNotice(roomId);
        result.put("isOwer",chatroom.getUserId().equals(userId));
        resultObj.setData(result);
        return resultObj;
    }

    public void join1v1Room(Long userId,Integer priceType,Integer amount,Long toUserId,Long roomId){

        Funtime1v1Record record = new Funtime1v1Record();
        record.setState(2);
        record.setUserId(userId);
        record.setPriceType(priceType);
        record.setPrice(amount);
        record.setRoomId(roomId);
        privateMapper.save1V1Record(record);
        String val = parameterService.getParameterValueByKey("match_percent");
        BigDecimal matchPercent = new BigDecimal(val);
        if (priceType == 1){
            //金币
            userService.updateUserAccountGoldCoinSub(userId,amount);
            accountService.saveUserAccountGoldLog(userId,new BigDecimal(amount),record.getId(),OperationType.PRIVATE_MATCH_OUT.getAction(),OperationType.PRIVATE_MATCH_OUT.getOperationType());
            Integer gold = new BigDecimal(amount).multiply(matchPercent).intValue();
            userService.updateUserAccountGoldCoinPlus(toUserId,gold);
            accountService.saveUserAccountGoldLog(toUserId,new BigDecimal(gold),record.getId(),OperationType.PRIVATE_MATCH_IN.getAction(),OperationType.PRIVATE_MATCH_IN.getOperationType());
        }
        if (priceType == 2){
            //蓝钻
            userService.updateUserAccountForSub(userId,null,new BigDecimal(amount),null);
            accountService.saveUserAccountBlueLog(userId,new BigDecimal(amount),record.getId(),OperationType.PRIVATE_MATCH_OUT.getAction(),OperationType.PRIVATE_MATCH_OUT.getOperationType(),roomId);
            BigDecimal black = new BigDecimal(amount).multiply(matchPercent);
            userService.updateUserAccountForPlus(toUserId,new BigDecimal(amount),null,null);
            accountService.saveUserAccountBlackLog(toUserId,black,record.getId(),OperationType.PRIVATE_MATCH_IN.getAction(),OperationType.PRIVATE_MATCH_IN.getOperationType());
        }
    }




    public FuntimeChatroomMic getMicLocationUser(Long roomId,Integer micLocation){
        return chatroomMicMapper.getMicLocationUser(roomId,micLocation);
    }

    public void roomJoinNotice(Long roomId, Long userId, String nickname, String carUrl, String msg, String animationType){
        //全房消息
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&userIds.size()>0) {
            //发送通知
            noticeService.notice12(roomId, userId, nickname, userIds,carUrl,msg,animationType);
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
        FuntimeUser user = userService.queryUserById(userId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("chatroom",chatroom);

        List<Map<String, Object>> micUser = getMicUserByRoomId(roomId);

        result.put("mic",micUser);
        result.put("isRedpacketShow",parameterService.getParameterValueByKey("is_redpacket_show"));
        result.put("isFishShow",2);
        result.put("roomGameTag","");
        result.put("roomGameIcon","");
        if (user.getCarId()!=null){
            Map<String, Object> carInfoMap = accountService.getCarInfoByCarId(user.getCarId());
            String carUrl = null;
            String carName ;
            String msg = "进入房间";
            if (carInfoMap !=null&&carInfoMap.get("carUrl")!=null){
                carUrl = carInfoMap.get("carUrl").toString();
                carName = carInfoMap.get("carName").toString();
                msg = "坐着"+carName+"进来了";
            }
            result.put("carUrl",carUrl);
            result.put("msg",msg);
        }

        result.put("shareUrl",Constant.SHARE_URL);
        result.put("shareTitle",Constant.SHARE_Title.replace("#",user.getNickname()));
        result.put("shareText",Constant.share_Text);
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
        Long chatroomManagerId = chatroomManagerMapper.getChatroomManager(roomId, userId);
        result.put("isManager",chatroomManagerId != null);


        Long gameUserId = game123Service.getUserByRoomId(roomId);
        result.put("isValueGame",gameUserId != null);

        if (gameUserId!=null&&gameUserId.equals(userId)){

            Integer state = game123Service.getStateByRoomId(roomId);
            result.put("valueGameState",state);
        }
        result.put("ddzShow",parameterService.getParameterValueByKey("ddz_show"));
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
        int hots = userId.equals(chatroom.getUserId())?5:user.getSex() == 2?3:2;
        //房间人数-1
        updateOnlineNumSub(roomId,chatroom.getHots()>hots?hots:0);

        game123Service.setExitTimeByExit(userId,roomId);
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
        /*
        Integer userRole = getUserRole(roomId, userId);
        if (userRole==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        if(!userService.checkAuthorityForUserRole(userRole, UserRoleAuthority.A_8.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/

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
        int hots = userId.equals(chatroom.getUserId())?5:user.getSex() == 2?3:2;
        //房间人数-1
        updateOnlineNumSub(roomId,chatroom.getHots()>hots?hots:0);
        game123Service.setExitTimeByExit(userId,roomId);

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
        /*
        Integer userRole = getUserRole(roomId,userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_4.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/
        /*
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

         */
        upperWheat(userId, roomId, micLocation, micUserId);
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

        /*
        if (userId!=null){
            //抱麦

            Integer userRole = getUserRole(roomId,userId);
            if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_4.getValue())){
                throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
            }
        }*/

        FuntimeUser user = userService.queryUserById(micUserId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        userService.checkForbiddenWords(micUserId);
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
            if(game21Service.getUserByRoomAndMic(roomId,chatroomMic2.getMicLocation())!=null){
                throw new BusinessException(ErrorMsgEnum.ROOM_GAME21_UPPER_ERROR.getValue(),ErrorMsgEnum.ROOM_GAME21_UPPER_ERROR.getDesc());
            }
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
            /*
            Integer userRole = getUserRole(roomId,userId);
            if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_7.getValue())){
                throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
            }*/
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
        /*
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_5.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/

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
        /*
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_6.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/
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
        /*
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_11.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/
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
        /*
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_12.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/
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
        /*
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_2.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/
        if (chatroomMapper.checkRoomExists(roomId)==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<String> userIds = getRoomUserByRoomIdAll(roomId);

        chatroomMapper.deleteByRoomId(roomId);
        deleteByRoomId(roomId);
        game21Service.exitGameForRoomClose(roomId);

        game123Service.exitGameForRoomClose(roomId);
        if (userIds!=null&&!userIds.isEmpty()) {
            //发送通知
            noticeService.notice7(roomId,userIds);

        }


    }

    public void deleteByRoomId(Long roomId){
        chatroomMicMapper.deleteByRoomId(roomId);
        chatroomMicMapper.updateMicByRoomId(roomId);
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
    public PageInfo<Map<String, Object>> getRoomList2(Integer startPage, Integer pageSize, Integer tagId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String,Object>> list = chatroomMapper.getRoomList2(tagId);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            return new PageInfo<>(list);
        }
    }

    @Override
    public PageInfo<Map<String, Object>> getRoomUserById(Integer startPage, Integer pageSize, Long roomId, String nickname, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String, Object>> list = chatroomMicMapper.getRoomUserById(roomId, nickname,userId);
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
    public PageInfo<Map<String, Object>> getRoomUserByIdAll2(Integer startPage, Integer pageSize, Long roomId, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String, Object>> list = chatroomMicMapper.getRoomUserByIdAll2(roomId,userId);
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
        /*
        Integer userRole = getUserRole(roomId, userId);
        if (userRole==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_9.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/
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
        /*
        Integer userRole = getUserRole(roomId, userId);
        if (userRole==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_13.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/

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
        /*
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_10.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/
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
    public void sendNotice(Long userId, String imgUrl, String msg, Long roomId, Integer type, Integer playLenth) {
        userService.checkSensitive(msg);
        userService.checkForbiddenWords(userId);
        FuntimeUser user = userService.getUserBasicInfoById(userId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (user.getOnlineState() == 2){
            throw new BusinessException(ErrorMsgEnum.USER_IS_OFFLINE.getValue(),ErrorMsgEnum.USER_IS_OFFLINE.getDesc());
        }
        FuntimeChatroom chatroom = getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        if(chatroom.getScreenFlag() == 2){
            throw new BusinessException(ErrorMsgEnum.ROOM_SCREEN_CLOSE.getValue(),ErrorMsgEnum.ROOM_SCREEN_CLOSE.getDesc());
        }
        Integer userRole = getUserRole(roomId,userId);
        userRole = userRole == null?4:userRole;
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        //msg = "<font color='#FFDE00'>"+msg+"</font>";
        noticeService.notice11Or14(userId,imgUrl,msg,roomId,type,userIds,userRole,playLenth);
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

    @Override
    public List<String> getAllRoomUserByLevel(Integer level) {
        return chatroomMicMapper.getAllRoomUserByLevel(level);
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
        FuntimeChatroomMic roomMic = getRoomUserInfoByUserId(userId);
        if (roomMic!=null) {
            log.info("offlineUserTask======>roomExitTask:userId:{}",userId);
            roomExit(userId,roomMic.getRoomId());
        }
    }
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void roomMicLowerTask(Long userId) {
        Long roomId = checkUserIsInMic(userId);
        if (roomId!=null) {
            log.info("offlineUserTask======>roomMicLowerTask:userId:{}",userId);
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
    public ResultMsg<Object> buyBackground(Integer backgroundId, Long userId, Long roomId) {
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
        accountService.saveUserAccountBlueLog(userId,price,userBackground.getId(),OperationType.BUY_BACKGROUND.getAction(),OperationType.BUY_BACKGROUND.getOperationType(), roomId);
        setBackground(backgroundId,userId,roomId);
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
        /*
        Integer userRole = getUserRole(roomId, userId);
        if (!userService.checkAuthorityForUserRole(userRole,UserRoleAuthority.A_14.getValue())){
            throw new BusinessException(ErrorMsgEnum.ROOM_USER_NO_AUTH.getValue(),ErrorMsgEnum.ROOM_USER_NO_AUTH.getDesc());
        }*/
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

                backgroundMapper.deleteUserBackgroundById(ubId);
                if (roomId!=null) {
                    Map<String, Object> map = backgroundMapper.getBackgroundUrlForType1();
                    String backgroundUrl = map.get("backgroundUrl").toString();
                    String backgroundUrl2 = map.get("backgroundUrl2").toString();
                    chatroomMapper.updateChatroomBackgroundId(roomId, Integer.parseInt(map.get("id").toString()));
                    List<String> userIds = getRoomUserByRoomIdAll(roomId);
                    //发送通知
                    if (userIds != null && !userIds.isEmpty()) {
                        noticeService.notice31(roomId, userId, backgroundUrl, userIds, backgroundUrl2);
                    }
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
                Long time = (Long) redisUtil.get(Constant.REDIS_ROOM_MIC_PREFIX + roomId);
                if (time!=null&&System.currentTimeMillis()-time>5000) {
                    userIds = getRoomUserByRoomIdAll(roomId);
                    micUser = getMicUserByRoomId(roomId);
                    if (userIds != null && !userIds.isEmpty()) {
                        noticeService.notice32(userIds, micUser, userIds.size());
                    }
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
            redisUtil.set(Constant.REDIS_ROOM_MIC_PREFIX+roomId,System.currentTimeMillis(),60*60*12);
        }
    }

    @Override
    public Map<String, Object> getBackgroundThumbnailById(Integer id) {
        return backgroundMapper.getBackgroundThumbnailById(id);
    }

    @Override
    public Integer getBackgroundDaysById(Integer id) {
        return backgroundMapper.getBackgroundDaysById(id);
    }

    @Override
    public List<FuntimeGift> getGiftListInit() {
        return giftMapper.getGiftListInit();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public String setRoomManager(Long roomId, Integer tagId, String managerIds, Long userId) {

        Integer duration = chatroomManagerMapper.getDurationConfById(tagId);
        if (duration == null){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
        }
        if(chatroomMapper.checkRoomExists(roomId)==null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        String[] managerIdArr = managerIds.split(",");
        for(String managerIdStr:managerIdArr) {
            Long managerId = Long.parseLong(managerIdStr);
            if (!userService.checkUserExists(managerId)) {
                throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(), ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
            }
            if (userId.equals(managerId)){
                throw new BusinessException(ErrorMsgEnum.USER_MANAGER_ERROR.getValue(), ErrorMsgEnum.USER_MANAGER_ERROR.getDesc());
            }
            FuntimeChatroomManager chatroomManager = new FuntimeChatroomManager();
            chatroomManager.setDuration(duration);
            if (duration >0) {
                chatroomManager.setExpireTime(DateUtils.addHours(new Date(), duration));
            }
            chatroomManager.setRoomId(roomId);
            chatroomManager.setUserId(managerId);
            Long id = chatroomManagerMapper.getChatroomManager(roomId, managerId);
            int k ;
            if (id == null) {

                k = chatroomManagerMapper.insertChatroomManager(chatroomManager);
                if (k != 1) {
                    throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(), ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
                }

            } else {
                chatroomManager.setId(id);
                k = chatroomManagerMapper.updateChatroomManager(chatroomManager);
                if (k != 1) {
                    throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(), ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
                }
            }
            k = chatroomManagerMapper.insertChatroomManagerRecord(chatroomManager);
            if (k != 1) {
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(), ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
            noticeService.notice35(roomId,managerId);
        }

        return duration==0?"永久":(duration<10?"0"+duration:duration)+":00:00";


    }

    @Override
    public PageInfo<Map<String, Object>> getRoomManagerList(Long roomId, Integer startPage, Integer pageSize) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String, Object>> list = chatroomManagerMapper.getRoomManagerList(roomId);
        if (list == null){
            return new PageInfo<>();
        }
        return new PageInfo<>(list);
    }

    @Override
    public List<Long> getRoomManagerIds(Long roomId) {
        return chatroomManagerMapper.getRoomManagerIds(roomId);
    }

    @Override
    public List<Map<String, Object>> getDurationConfs() {
        return chatroomManagerMapper.getDurationConfs();
    }

    @Override
    public void deleteChatroomManagerTask() {
        List<FuntimeChatroomManager> managers = chatroomManagerMapper.getChatroomManagerTask();
        if (managers!=null&&!managers.isEmpty()) {
            for (FuntimeChatroomManager chatroomManager : managers){
                int k = chatroomManagerMapper.deleteChatroomManagerTask(chatroomManager.getId());
                if (k==1) {
                    noticeService.notice36(chatroomManager.getRoomId(), chatroomManager.getUserId());
                }else{
                    log.error("deleteChatroomManagerTask error:userId:{},roomId:{}",chatroomManager.getUserId(),chatroomManager.getRoomId());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void delRoomManager(Long id) {
        FuntimeChatroomManager manager = chatroomManagerMapper.getChatroomManagerById(id);
        if (manager != null){
            int k = chatroomManagerMapper.delRoomManager(id);
            if(k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
            noticeService.notice36(manager.getRoomId(),manager.getUserId());
        }

    }

    @Override
    public Long getChatroomManager(Long roomId, Long userId) {
        return chatroomManagerMapper.getChatroomManager(roomId, userId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void startMusicAuth(Long roomId, Integer micLocation) {
        FuntimeChatroomMic micLocationUser = getMicLocationUser(roomId, micLocation);
        if (micLocationUser!=null){
            int k = chatroomMicMapper.startMusicAuth(micLocationUser.getId());
            if(k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
            List<String> userIds = getRoomManagerByRoomId(roomId);
            List<Map<String, Object>> micUser = getMicUserByRoomId(roomId);
            if (userIds!=null&&!userIds.isEmpty()) {
                noticeService.notice32(userIds, micUser, userIds.size());
            }
        }
    }
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void cancelMusicAuth(Long roomId, Integer micLocation) {
        FuntimeChatroomMic micLocationUser = getMicLocationUser(roomId, micLocation);
        if (micLocationUser!=null){
            int k = chatroomMicMapper.cancelMusicAuth(micLocationUser.getId());
            if(k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
            List<String> userIds = getRoomManagerByRoomId(roomId);
            List<Map<String, Object>> micUser = getMicUserByRoomId(roomId);
            if (userIds!=null&&!userIds.isEmpty()) {
                noticeService.notice32(userIds, micUser, userIds.size());
            }
        }
    }

    @Override
    public void roomCloseTask() {
        List<FuntimeChatroom> chatrooms = chatroomMapper.getRoomCloseTask();
        for (FuntimeChatroom chatroom : chatrooms){
            try {
                roomClose(chatroom.getUserId(), chatroom.getId());
                redisUtil.del(Constant.REDIS_ROOM_MIC_PREFIX+chatroom.getId());
            }catch (Exception e){
                log.error("定时清理空房出错 房间ID:{}",chatroom.getId());
            }
        }

    }

    @Override
    public List<Map<String, Object>> getMicInfoByRoomId(Long roomId) {
        return chatroomMicMapper.getMicInfoByRoomId(roomId);
    }

    @Override
    public void updateHotsPlus(Long roomId, int hots) {
        int k = chatroomMapper.updateHotsPlus(roomId,hots);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void updateHotsSub(Long roomId, int hots) {
        int k = chatroomMapper.updateHotsSub(roomId,hots);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void resetRoomHotsTask() {

        chatroomMapper.resetRoomHotsTask();
    }

    @Override
    public Map<String, Object> getRoomRankingList(Integer dateType, Integer type, String curUserId,Long roomId) {
        Map<String, Object> resultMap = new HashMap<>();
        String count = parameterService.getParameterValueByKey("ranking_list_count");
        resultMap.put("rankCount",count);
        int endCount = Integer.parseInt(count);

        FuntimeChatroom chatroom = getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        String startDate;
        String endDate;

        if (dateType == 1){
            startDate = DateUtil.getCurrentDayStart();
            endDate = DateUtil.getCurrentDayEnd();
        }else if (dateType == 2){
            startDate = DateUtil.getCurrentWeekStart();
            endDate = DateUtil.getCurrentWeekEnd();
        }else if (dateType == 3){
            startDate = DateUtil.getCurrentMonthStart();
            endDate = DateUtil.getCurrentMonthEnd();
        }else if (dateType == 4){
            startDate = null;
            endDate = null;
        }else {
            resultMap.put("rankingList",null);
            return resultMap;
        }

        List<Map<String, Object>> list;

        if (type == 1){
            list = chatroomMapper.getRoomCharmList(endCount,startDate,endDate,roomId);
        }else{
            list = chatroomMapper.getRoomContributionList(endCount,startDate,endDate,roomId,chatroom.getUserId());
        }

        if (list==null||list.isEmpty()){
            resultMap.put("rankingList",null);
            return resultMap;
        }
        FuntimeUser user = userService.queryUserById(Long.parseLong(curUserId));
        FuntimeUserAccount userAccount= accountService.getUserAccountByUserId(Long.parseLong(curUserId));
        Map<String,Object> myInfoMap = new HashMap<>();
        myInfoMap.put("nickname",user.getNickname());
        myInfoMap.put("portraitAddress",user.getPortraitAddress());
        myInfoMap.put("signText",user.getSignText());
        myInfoMap.put("showId",user.getShowId());
        myInfoMap.put("sex",user.getSex());
        myInfoMap.put("level",userAccount.getLevel());
        myInfoMap.put("levelUrl",userAccount.getLevelUrl());
        boolean isRankMe = false;
        for (int i =0;i<list.size();i++){
            Map<String, Object> map = list.get(i);
            String userId = map.get("userId").toString();
            if (userId.equals(curUserId)){
                isRankMe = true;
                myInfoMap.put("isRankMe",true);
                myInfoMap.put("mySort", i+1);
                myInfoMap.put("myAmount", map.get("amountSum"));
                if (i == 0){
                    myInfoMap.put("diffAmount",0);
                }else{
                    BigDecimal currentAmount = new BigDecimal(map.get("amountSum").toString());
                    BigDecimal lastAmount = new BigDecimal(list.get(i-1).get("amountSum").toString());
                    myInfoMap.put("diffAmount",lastAmount.subtract(currentAmount).intValue());
                }

                resultMap.put("user",myInfoMap);
            }
        }
        if (!isRankMe){
            myInfoMap.put("isRankMe",false);
            resultMap.put("user",myInfoMap);
        }
        resultMap.put("rankingList",list);
        return resultMap;

    }

    @Override
    public void showCar(Long userId, Long roomId, Integer carNumber) {
        FuntimeUser user = userService.queryUserById(userId);
        if (user == null){
            return;
        }

        Integer counts = accountService.getShowCountsById(userId);
        String val = parameterService.getParameterValueByKey("show_car_count");
        Integer normal = val == null?10:Integer.parseInt(val);
        if (counts>=normal){
            throw new BusinessException(ErrorMsgEnum.ROOM_SHOWCAR_OVER.getValue(),ErrorMsgEnum.ROOM_SHOWCAR_OVER.getDesc());
        }

        accountService.insertShowcarRecord(userId,carNumber);


        Map<String, Object> carInfoMap = accountService.getCarInfoByCarId(carNumber);
        if (carInfoMap !=null&&carInfoMap.get("carUrl")!=null){
            String carUrl = carInfoMap.get("carUrl").toString();
            String carName = carInfoMap.get("carName").toString();
            String msg = "坐着"+carName+"进来了";
            String animationType = carInfoMap.get("animationType").toString();
            List<String> userIds = getRoomUserByRoomIdAll(roomId);
            if (userIds!=null&&userIds.size()>0) {
                //发送通知
                noticeService.notice40(roomId, userId, user.getNickname(), userIds,carUrl,msg,animationType);

            }
        }
    }

    @Override
    public Map<String, Object> getRoomStatement(String startDate, String endDate, Long roomId) {
        FuntimeChatroom chatroom = getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<Map<String,Object>> list = chatroomMapper.getRoomStatement(startDate,endDate,roomId,chatroom.getUserId());
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("listData",list);
        if (list!=null&&!list.isEmpty()){
            int total = 0;
            for (Map<String,Object> map : list){
                Integer totalCoefficient = map.get("totalCoefficient")==null?0:Integer.parseInt(map.get("totalCoefficient").toString());

                total += totalCoefficient;
            }
            resultMap.put("total",total);
        }
        return resultMap;
    }

    @Override
    public Long getInvitationRoomId() {
        return chatroomMapper.getInvitationConf();
    }

    @Override
    public void openScreen(Long roomId, Long userId) {
        FuntimeChatroom chatroom = getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        if(chatroom.getScreenFlag() == 1){
            throw new BusinessException(ErrorMsgEnum.ROOM_SCREEN_OPEN.getValue(),ErrorMsgEnum.ROOM_SCREEN_OPEN.getDesc());
        }
        if (!userId.equals(chatroom.getUserId())) {
            Long manager = getChatroomManager(roomId, userId);
            if (manager == null) {
                throw new BusinessException(ErrorMsgEnum.ROOM_NOT_MANAGER.getValue(), ErrorMsgEnum.ROOM_NOT_MANAGER.getDesc());
            }
        }
        int k = chatroomMapper.updateScreenFlag(roomId,1);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        FuntimeUser user = userService.queryUserById(userId);
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        String msg = "公屏已打开";
        if (userIds!=null&&!userIds.isEmpty()){
            noticeService.notice41(userIds,roomId,userId,user.getNickname(),msg);
        }
    }

    @Override
    public void closeScreen(Long roomId, Long userId) {
        FuntimeChatroom chatroom = getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        if(chatroom.getScreenFlag() == 2){
            throw new BusinessException(ErrorMsgEnum.ROOM_SCREEN_CLOSE.getValue(),ErrorMsgEnum.ROOM_SCREEN_CLOSE.getDesc());
        }
        if (!userId.equals(chatroom.getUserId())) {
            Long manager = getChatroomManager(roomId, userId);
            if (manager == null) {
                throw new BusinessException(ErrorMsgEnum.ROOM_NOT_MANAGER.getValue(), ErrorMsgEnum.ROOM_NOT_MANAGER.getDesc());
            }
        }
        int k = chatroomMapper.updateScreenFlag(roomId,2);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        FuntimeUser user = userService.queryUserById(userId);
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        String msg = "公屏已关闭";
        if (userIds!=null&&!userIds.isEmpty()){
            noticeService.notice42(userIds,roomId,userId,user.getNickname(),msg);
        }
    }

    @Override
    public Map<String,Object> getRecommendRoomList() {

        //实时热门
        List<Map<String, Object>> list = chatroomMapper.getRecommendRoomList(1);
        if (list == null||list.isEmpty()){
            list = chatroomMapper.getRecommendRoomListExt();
        }else{
            if (list.size() != 3){
                List<Map<String, Object>> exts = chatroomMapper.getRecommendRoomListExt();
                if (exts!=null&&!exts.isEmpty()){
                    for (Map<String, Object> map : exts){
                        if (!map.get("id").equals(list.get(0).get("id"))){
                            list.add(map);
                        }
                        if (list.size() == 3){
                            break;
                        }
                    }
                }
            }
        }

        //List<Map<String, Object>> list2 = chatroomMapper.getRecommendRoomList(2);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("recommends",null);
        resultMap.put("hots",list);

        return resultMap;
    }

    @Override
    public void openRoomRank(Long roomId, Long userId) {
        FuntimeChatroom chatroom = getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        if(chatroom.getRankFlag() == 1){
            throw new BusinessException(ErrorMsgEnum.ROOM_RANK_OPEN.getValue(),ErrorMsgEnum.ROOM_RANK_OPEN.getDesc());
        }
        if (!userId.equals(chatroom.getUserId())) {
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_CREATER.getValue(), ErrorMsgEnum.ROOM_NOT_CREATER.getDesc());
        }
        int k = chatroomMapper.updateRankFlag(roomId,1);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&!userIds.isEmpty()){
            noticeService.notice43(userIds,roomId,userId);
        }
    }

    @Override
    public void closeRoomRank(Long roomId, Long userId) {
        FuntimeChatroom chatroom = getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        if(chatroom.getRankFlag() == 2){
            throw new BusinessException(ErrorMsgEnum.ROOM_RANK_CLOSE.getValue(),ErrorMsgEnum.ROOM_RANK_CLOSE.getDesc());
        }
        if (!userId.equals(chatroom.getUserId())) {
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_CREATER.getValue(), ErrorMsgEnum.ROOM_NOT_CREATER.getDesc());
        }
        int k = chatroomMapper.updateRankFlag(roomId,2);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        List<String> userIds = getRoomUserByRoomIdAll(roomId);
        if (userIds!=null&&!userIds.isEmpty()){
            noticeService.notice44(userIds,roomId,userId);
        }
    }

    @Override
    public List<String> getRoomUserByRoomIdAll2(Long roomId) {
        return chatroomMicMapper.getRoomUserByRoomIdAll2(roomId);
    }

    @Override
    public List<FuntimeGift> getGiftListByOrder() {
        return giftMapper.getGiftListByOrder();
    }

    @Override
    public Map<String, Object> get1v1price(Long userId) {
        Integer counts = privateMapper.get1v1Counts(userId, DateUtil.getCurrentDayStart(), DateUtil.getCurrentDayEnd());
        int times = 0;
        if (counts==null||counts == 0){
            times++;
        }
        List<Map<String, Object>> v1price = privateMapper.get1v1price(times);
        if (v1price==null||v1price.isEmpty()){
            v1price = privateMapper.get1v1price(0);
        }
        if (v1price==null||v1price.isEmpty()){
            throw new BusinessException(ErrorMsgEnum.ROOM_PRIVATE_PRICECONF_ERROR);
        }
        Map<String, Object> resultMap = new HashMap<>();
        FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);
        resultMap.put("goldAmount",userAccount.getGoldCoin());
        resultMap.put("blueAmount",userAccount.getBlueDiamond());
        resultMap.put("prices",v1price);
        return resultMap;
    }

    @Override
    public ResultMsg<Object> doMatch(Long userId, Integer priceId) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();

        Map<String, Object> map = privateMapper.get1v1priceById(priceId);
        if (map == null||map.isEmpty()){
            throw new BusinessException(ErrorMsgEnum.ROOM_PRIVATE_PRICECONF_ERROR);
        }
        Integer priceType = Integer.parseInt(map.get("priceType").toString());
        Integer price = Integer.parseInt(map.get("amount").toString());
        FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);
        BigDecimal amount;
        if (priceType == 1){
            amount = userAccount.getGoldCoin();
            if (amount.subtract(new BigDecimal(price)).intValue()<0){
                resultMsg = new ResultMsg<>(ErrorMsgEnum.USER_ACCOUNT_GOLD_NOT_EN);
                map = new HashMap<>();
                map.put("userBlueAmount",userAccount.getGoldCoin().intValue());
                map.put("price",price);
                resultMsg.setData(map);
                return resultMsg;
            }
        }else{
            amount = userAccount.getBlueDiamond();
            if (amount.subtract(new BigDecimal(price)).intValue()<0) {
                resultMsg = new ResultMsg<>(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN);
                map = new HashMap<>();
                map.put("userBlueAmount", userAccount.getBlueDiamond().intValue());
                map.put("price", price);
                resultMsg.setData(map);
                return resultMsg;
            }
        }
        FuntimeUser user = userService.queryUserById(userId);
        Integer sex = 2;
        if (user.getSex() != null&&user.getSex() == 2){
            sex = 1;
        }
        Map<String, Object> roomMap = chatroomMicMapper.getRoomByMatch(sex);
        if (roomMap != null&&!roomMap.isEmpty()){
            Long roomId = Long.parseLong(roomMap.get("roomId").toString());
            Map<String, Object> nextPrice = get1v1price(userId);
            nextPrice.put("roomId",roomId);
            resultMsg.setData(nextPrice);
        }else {
            Funtime1v1Record record = new Funtime1v1Record() ;
            record.setPrice(price);
            record.setPriceType(priceType);
            record.setUserId(userId);
            record.setState(1);
            privateMapper.save1V1Record(record);
            resultMsg = new ResultMsg<>(ErrorMsgEnum.ROOM_MATCH_FAIL);
            String matchSeconds = parameterService.getParameterValueByKey("match_seconds");
            Map<String,Object> resultMap = new HashMap<>();

            resultMap.put("recordId",record.getId());
            resultMap.put("msg",(user.getSex() == 1?"小仙女":"小帅哥")+"正在路上...\n" +
                    "需要等待");
            resultMap.put("matchSeconds",matchSeconds);
            resultMsg.setData(resultMap);

        }

        return resultMsg;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void doMatchTask() {

        privateMapper.cancelMatch();
        List<Funtime1v1Record> records = privateMapper.get1v1RecordTask();
        if (records!=null&&!records.isEmpty()){
            List<Map<String, Object>> roomMaps1 = chatroomMicMapper.getRoomByMatchTask(1);
            List<Map<String, Object>> roomMaps2 = chatroomMicMapper.getRoomByMatchTask(2);
            FuntimeUserAccount userAccount;
            FuntimeUser user;
            BigDecimal amount;
            Long roomId;
            for (Funtime1v1Record record : records){
                Long userId = record.getUserId();
                user = userService.queryUserById(userId);
                if (user.getSex()!=null&&user.getSex() == 1){
                    if (roomMaps2 == null||roomMaps2.isEmpty()){
                        continue;
                    }
                    int i = RandomUtils.nextInt(0,roomMaps2.size()-1);
                    roomId = Long.parseLong(roomMaps2.get(i).get("roomId").toString());
                    roomMaps2.remove(i);
                }else{
                    if (roomMaps1 == null||roomMaps1.isEmpty()){
                        continue;
                    }
                    int i = RandomUtils.nextInt(0,roomMaps1.size()-1);
                    roomId = Long.parseLong(roomMaps1.get(i).get("roomId").toString());
                    roomMaps1.remove(i);
                }

                userAccount = accountService.getUserAccountByUserId(userId);
                if (record.getPriceType() == 1){
                    amount = userAccount.getGoldCoin();
                    if (amount.subtract(new BigDecimal(record.getPrice())).intValue()<0){
                        continue;
                    }
                }else{
                    amount = userAccount.getBlueDiamond();
                    if (amount.subtract(new BigDecimal(record.getPrice())).intValue()<0) {
                        continue;
                    }
                }

                privateMapper.compeleteMatch(record.getId(),roomId);
                noticeService.notice46(record.getUserId(),roomId);
            }

        }
    }

    @Override
    public void cancelMatch(Long userId, Long recordId) {

        privateMapper.cancelMatchById(recordId);
    }

    public FuntimeChatroomMic getInfoByRoomIdAndUser(Long roomId,Long userId){
        return chatroomMicMapper.getInfoByRoomIdAndUser(roomId,userId);
    }

    @Override
    public Integer getUserRole2(Long roomId,Long userId){
        FuntimeChatroomMic user = getInfoByRoomIdAndUser(roomId, userId);
        return user == null?null:user.getUserRole();
    }

    public List<String> getRoomManagerByRoomId(Long roomId){
        return chatroomMicMapper.getRoomManagerByRoomId(roomId);
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
        game21Service.delMicInfoForlowerWheat(roomId,micLocation);


    }

    public void updateOnlineNumPlus(Long id, int hots){
        int k = chatroomMapper.updateOnlineNumPlus(id,hots);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    public void updateOnlineNumSub(Long id,Integer hots){
        int k = chatroomMapper.updateOnlineNumSub(id,hots);
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
