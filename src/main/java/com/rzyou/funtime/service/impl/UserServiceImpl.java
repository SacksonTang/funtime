package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.httputil.HttpClientUtil;
import com.rzyou.funtime.common.im.BankCardVerificationUtil;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.common.jwt.util.JwtHelper;
import com.rzyou.funtime.common.qqutils.QqLoginUtils;
import com.rzyou.funtime.common.wxutils.WeixinLoginUtils;
import com.rzyou.funtime.component.RedisUtil;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.*;
import com.rzyou.funtime.utils.DateUtil;
import com.rzyou.funtime.utils.StringUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    SmsService smsService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    RoomService roomService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    AdvertisService advertisService;
    @Autowired
    DdzService ddzService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    FuntimeUserMapper userMapper;
    @Autowired
    FuntimeTagMapper tagMapper;
    @Autowired
    FuntimeUserTagMapper userTagMapper;
    @Autowired
    FuntimeUserLoginRecordMapper loginRecordMapper;
    @Autowired
    FuntimeUserAccountMapper accountMapper;
    @Autowired
    FuntimeUserValidMapper userValidMapper;
    @Autowired
    FuntimeUserConcernMapper userConcernMapper;
    @Autowired
    FuntimeUserThirdMapper userThirdMapper;
    @Autowired
    FuntimeGiftMapper giftMapper;
    @Autowired
    FuntimeUserPhotoAlbumMapper userPhotoAlbumMapper;

    @Autowired
    FuntimeAccusationMapper accusationMapper;
    @Autowired
    FuntimeWithdrawalConfMapper withdrawalConfMapper;
    @Autowired
    FuntimeAppVersionMapper appVersionMapper;


    @Override
    public void insertUserActivity(Long userId, Integer activityId) {
        if (userMapper.getUserActivity(userId)==null){
            userMapper.insertUserActivity(userId,activityId);
        }
    }

    @Override
    public List<String> getAllUserId() {
        return userMapper.getAllUserId();
    }

    @Override
    public List<String> getAllUserIdByApp() {
        return userMapper.getAllUserIdByApp();
    }

    @Override
    public boolean checkUserExists(Long id) {
        if (userMapper.checkUserExists(id)==null){
            return false;
        }
        return true;
    }

    @Override
    public FuntimeUser queryUserById(Long id) {
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public FuntimeUser queryUserInfoByPhone(String phone){
        Map<String,Object> map = new HashMap<>();
        map.put("phone",phone);
        return userMapper.queryUserInfo(map);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void updateUserInfo(Long id, Integer onlineState, String imei, String ip, String nikename, String loginType, String deviceName, String appVersion){
        String uuid = StringUtil.createNonceStr();
        String token = JwtHelper.generateJWT(id.toString(),uuid);
        FuntimeUser user = new FuntimeUser();
        user.setToken(token);
        user.setPhoneImei(imei);
        user.setOnlineState(onlineState);
        user.setId(id);
        user.setIp(ip);
        user.setAppVersion(appVersion);
        user.setLastLoginTime(new Date());
        updateByPrimaryKeySelective(user);

        //足迹
        saveRecode(id,user.getPhoneImei(),user.getIp(),1, nikename, loginType, deviceName);
        RedisUser redisUser = new RedisUser();
        redisUser.onlineState = 1;
        redisUser.token = token;
        redisUtil.set(Constant.REDISUSER_PREFIX+user.getId(),redisUser);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void updateUserInfo(FuntimeUser user){
        String uuid = StringUtil.createNonceStr();
        String token = JwtHelper.generateJWT(user.getId().toString(),uuid);
        user.setToken(token);
        user.setLastLoginTime(new Date());
        updateByPrimaryKeySelective(user);

        //足迹
        saveRecode(user.getId(),user.getPhoneImei(),user.getIp(),1, user.getNickname(), user.getLoginType(), user.getDeviceName());
        RedisUser redisUser = new RedisUser();
        redisUser.onlineState = 1;
        redisUser.token = token;
        redisUtil.set(Constant.REDISUSER_PREFIX+user.getId(),redisUser);
    }



    @Override
    public FuntimeUser getUserBasicInfoById(Long id) {

        FuntimeUser user = userMapper.selectByPrimaryKey(id);

        if(user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (user.getBirthday()!=null) {
            Integer birthday = user.getBirthday();
            user.setAge(DateUtil.getAgeByBirthday(birthday));
            user.setConstellation(DateUtil.getConstellationByBirthday(birthday));
        }
        if (user.getSex()!=null){
            if (user.getSex() == 1){
                user.setSexColor(Constant.SEX_MALE_COLOR);
            }else {
                user.setSexColor(Constant.SEX_FEMALE_COLOR);
            }
        }
        if (user.getHeight()!=null){
            user.setHeightColor(Constant.HEIGHT_COLOR);
        }

        List<Map<String, Object>> tagNames = tagMapper.queryTagNamesByUserId(user.getId());
        if (tagNames!=null&&!tagNames.isEmpty()){
            for (Map<String, Object> map : tagNames){
                if (map.get("tagType").toString()!=null){
                    map.put("tagColor", TagColorEnmu.getDescByValue(map.get("tagType").toString()));
                }

            }
        }
        user.setTagNames(tagNames);
        FuntimeUserAccount userAccount = accountMapper.selectByUserId(id);
        user.setBlueAmount(userAccount.getBlueDiamond().intValue());
        user.setReceivedGiftNum(userAccount.getReceivedGiftNum());
        user.setLevel(userAccount.getLevel());
        user.setLevelUrl(userAccount.getLevelUrl());
        user.setLevelName(userAccount.getLevelName());
        user.setShowUrl(userAccount.getShowUrl());
        if (userAccount.getLevel() == 0){
            user.setBackUrl(Constant.USER_LEVEL0_URL);
            user.setDarkUrl(Constant.USER_LEVEL0_URL);
        }else{
            user.setBackUrl(Constant.USER_BACK_URL);
            user.setDarkUrl(Constant.USER_DARK_URL);
        }
        FuntimeChatroom chatroom = roomService.getRoomByUserId(id);

        if (chatroom!=null) {

            user.setRoomId(chatroom.getId());

            user.setIsBlock(chatroom.getIsBlock());

            user.setIsLock(chatroom.getIsLock());

            user.setRoomState(chatroom.getState());
        }

        List<Integer> tags = queryTagsByUserId(id);
        user.setTags(tags);
        return user;
    }

    @Override
    public FuntimeUserThird queryUserInfoByOpenid(String openid,String thirdType) {
        return userThirdMapper.queryUserByOpenid(openid,thirdType);
    }

    @Override
    public String queryUserOpenidByType(Long userId, String thirdType) {
        return userThirdMapper.queryUserOpenidByType(userId,thirdType);
    }

    @Override
    public FuntimeUserThird queryUserThirdIdByType(Long userId, String thirdType) {
        return userThirdMapper.queryUserThirdIdByType(userId,thirdType);
    }


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Boolean saveUser(FuntimeUser user, String openType, String openid, String unionid,String accessToken) {

        List<Long> beautyList = userMapper.getBeautyNumbers();
        Long maxShowId = userMapper.getMaxShowId();
        for (int i = 0;i<10000;i++){
            maxShowId++;
            if(!beautyList.contains(maxShowId)){
                break;
            }
        }
        user.setShowId(maxShowId);

        insertSelective(user);
        String uuid = StringUtil.createNonceStr();
        String token = JwtHelper.generateJWT(user.getId().toString(),uuid);
        user.setToken(token);
        updateTokenById(user.getId(),token);
        if (StringUtils.isNotBlank(user.getPhoneNumber())){
            userMapper.saveUserInfoChangeLog(user.getId(),"phone_number",user.getPhoneNumber());
        }

        //足迹
        saveRecode(user.getId(),user.getPhoneImei(),user.getIp(),1, null, user.getLoginType(), user.getDeviceName());

        saveUserAccount(user.getId());

        String counts = parameterService.getParameterValueByKey("user_im_count");

        userMapper.insertUserImDayCount(user.getId(),counts==null?10:Integer.parseInt(counts));

        if (openType!=null) {
            saveUserThird(user.getId(), openType, openid, unionid, accessToken,user.getNickname());
            userMapper.saveUserInfoChangeLog(user.getId(),"openid",openType+"/"+openid);
        }
        RedisUser redisUser = new RedisUser();
        redisUser.onlineState = 1;
        redisUser.token = token;
        redisUtil.set(Constant.REDISUSER_PREFIX+user.getId(),redisUser);
        return true;
    }

    private void saveUserThird(Long userId, String openType, String openid, String unionid, String accessToken,String nickname){
        FuntimeUserThird userThird = new FuntimeUserThird();
        userThird.setUserId(userId);
        userThird.setThirdType(openType);
        userThird.setOpenid(openid);
        userThird.setUnionid(unionid);
        userThird.setToken(accessToken);
        userThird.setNickname(nickname);
        userThirdMapper.insertSelective(userThird);

    }

    private void saveUserAccount(Long userId) {
        FuntimeUserAccount userAccount = new FuntimeUserAccount();
        userAccount.setUserId(userId);
        userAccount.setVersion(System.currentTimeMillis());
        accountMapper.insertSelective(userAccount);
    }

    public void saveRecode(Long userId, String phoneImei, String ip, Integer operation, String nikename, String loginType, String deviceName) {
        FuntimeUserLoginRecord record = new FuntimeUserLoginRecord();
        record.setIp(ip);
        record.setUserId(userId);
        record.setPhoneImei(phoneImei);
        record.setOperation(operation);
        record.setNickname(nikename);
        record.setLoginType(loginType);
        record.setDeviceName(deviceName);
        loginRecordMapper.insertSelective(record);
    }

    public Boolean insertSelective(FuntimeUser user) {
        user.setLastLoginTime(new Date());
        if(userMapper.insertSelective(user)!=1){

            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Boolean updateUserBasicInfoById(FuntimeUser user) {
        FuntimeUser funtimeUser = queryUserById(user.getId());
        if(funtimeUser == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        List<Integer> tags = new ArrayList<>();
        if (user.getTags()!=null){
            tags.addAll(user.getTags());
            updateTagsByUserId(tags,user.getId());
        }
        if (user.getBirthday()==null&&funtimeUser.getBirthday()==null){
            user.setBirthday(Integer.parseInt(DateUtil.getCurrentYearAdd(new Date(),-18)));
        }

        if (user.getNewUser()!=null&&user.getNewUser()){
            if (user.getSex()!=null) {
                if (user.getLoginType() != null && (user.getLoginType().equals(Constant.LOGIN_APPLE)
                        || Constant.LOGIN_ONEKEY.equals(user.getLoginType())
                        || Constant.LOGIN_TEL.equals(user.getLoginType()))) {
                    List<String> userImageDefaultUrls = getUserImageDefaultUrls(user.getSex());
                    if (userImageDefaultUrls == null || userImageDefaultUrls.isEmpty()) {
                        if (user.getSex() == 1) {
                            user.setPortraitAddress(Constant.COS_URL_PREFIX + Constant.DEFAULT_MALE_HEAD_PORTRAIT);
                        }
                        if (user.getSex() == 2) {
                            user.setPortraitAddress(Constant.COS_URL_PREFIX + Constant.DEFAULT_FEMALE_HEAD_PORTRAIT);
                        }
                    } else {
                        user.setPortraitAddress(userImageDefaultUrls.get(RandomUtils.nextInt(0, userImageDefaultUrls.size())));
                    }
                }
            }
        }else {

            if (user.getPortraitAddress() == null && funtimeUser.getPortraitAddress() == null) {
                if (user.getSex() != null) {
                    user.setNickname(getDefaultNameBySex(user.getSex()));
                    List<String> userImageDefaultUrls = getUserImageDefaultUrls(user.getSex());
                    if (userImageDefaultUrls == null || userImageDefaultUrls.isEmpty()) {
                        if (user.getSex() == 1) {
                            user.setPortraitAddress(Constant.COS_URL_PREFIX + Constant.DEFAULT_MALE_HEAD_PORTRAIT);
                        }
                        if (user.getSex() == 2) {
                            user.setPortraitAddress(Constant.COS_URL_PREFIX + Constant.DEFAULT_FEMALE_HEAD_PORTRAIT);
                        }
                    } else {
                        user.setPortraitAddress(userImageDefaultUrls.get(RandomUtils.nextInt(0, userImageDefaultUrls.size())));
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(user.getPortraitAddress())){
            if (user.getPortraitAddress().contains("funtime-1300805214.cos.ap-shanghai.myqcloud.com")){
                user.setPortraitAddress(user.getPortraitAddress().replace("funtime-1300805214.cos.ap-shanghai.myqcloud.com",Constant.COS_URL_PREFIX2));
            }
        }

        updateByPrimaryKeySelective(user);
        if (StringUtils.isNotBlank(user.getNickname())||StringUtils.isNotBlank(user.getPortraitAddress())||user.getSex()!=null){
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            boolean flag = TencentUtil.portraitSet(userSig, user.getId().toString(), user.getNickname(), user.getPortraitAddress(),user.getSex()==null?null:user.getSex().toString());
            if (!flag){
                throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(),ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
            }
            Long roomId = roomService.checkUserIsInMic(user.getId());
            if (roomId!=null){
                roomService.sendRoomInfoNotice(roomId);
            }
        }

        return true;
    }

    @Override
    public void updateQQUserImage(){
        List<FuntimeUser> users = userMapper.getAllQqUser();
        if (users!=null&& !users.isEmpty()) {
            log.info("查询到的QQ用户数=====>{}", users.size());
            for (FuntimeUser user : users){
                int sex = user.getSex() == null?1:user.getSex();
                List<String> userImageDefaultUrls = getUserImageDefaultUrls(sex);
                if (userImageDefaultUrls == null || userImageDefaultUrls.isEmpty()) {
                    if (user.getSex() == 1) {
                        user.setPortraitAddress(Constant.COS_URL_PREFIX + Constant.DEFAULT_MALE_HEAD_PORTRAIT);
                    }
                    if (user.getSex() == 2) {
                        user.setPortraitAddress(Constant.COS_URL_PREFIX + Constant.DEFAULT_FEMALE_HEAD_PORTRAIT);
                    }
                } else {
                    user.setPortraitAddress(userImageDefaultUrls.get(RandomUtils.nextInt(0, userImageDefaultUrls.size())));
                }
                user.setSex(null);
                updateByPrimaryKeySelective(user);
                String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
                boolean flag = TencentUtil.portraitSet(userSig, user.getId().toString(), null, user.getPortraitAddress(),null);
                if (!flag){
                    throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(),ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
                }
            }
        }
    }

    @Override
    public List<String> getUserImageDefaultUrls(Integer sex){
        return userMapper.getUserImageDefaultUrls(sex);
    }

    public void updateTagsByUserId(List<Integer> tags,Long userId){
        userTagMapper.deleteUserTags(userId);
        FuntimeUserTag userTag;
        for (Integer tagId:tags){
            userTag = new FuntimeUserTag();
            userTag.setTagId(tagId);
            userTag.setUserId(userId);
            userTagMapper.insertSelective(userTag);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Boolean deleteUser(Long id) {
        FuntimeUser funtimeUser = userMapper.selectByPrimaryKey(id);
        if(funtimeUser==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if(funtimeUser.getState().intValue()==2){
            throw new BusinessException(ErrorMsgEnum.USER_IS_DELETE.getValue(),ErrorMsgEnum.USER_IS_DELETE.getDesc());
        }
        FuntimeUser user = new FuntimeUser();
        user.setId(id);
        user.setState(2);

        return updateByPrimaryKeySelective(user);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Boolean enableUser(Long id) {
        FuntimeUser funtimeUser = userMapper.selectByPrimaryKey(id);
        if(funtimeUser==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if(funtimeUser.getState().intValue()==1){
            throw new BusinessException(ErrorMsgEnum.USER_IS_NORMAL.getValue(),ErrorMsgEnum.USER_IS_NORMAL.getDesc());
        }
        FuntimeUser user = new FuntimeUser();
        user.setId(id);
        user.setState(1);

        return updateByPrimaryKeySelective(user);
    }

    @Override
    public List<Integer> queryTagsByUserId(Long userId) {
        return tagMapper.queryTagsByUserId(userId);
    }

    @Override
    public Integer queryTagsByTypeAndName(String tagType, String tagName) {
        return tagMapper.queryTagsByTypeAndName(tagType,tagName);
    }

    @Override
    public List<Map<String,Object>> queryTagsByType(String tagType, Integer type) {
        List<Map<String,Object>> list = tagMapper.queryTagsByType(tagType);
        List<Map<String,Object>> results = new ArrayList<>();
        if (type!=null){
            if (list!=null&&!list.isEmpty()) {
                if (type == 1) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", 0);
                    map.put("tagName", "全部");
                    results.add(map);
                    map = new HashMap<>();
                    map.put("id", -1);
                    map.put("tagName", "热门");
                    results.add(map);
                }else if (type == 2){
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", 0);
                    map.put("tagName", "全部");
                    results.add(map);
                }
            }

        }
        results.addAll(list);
        return results;
    }

    @Override
    public FuntimeUserAccount getUserAccountInfoById(Long userId) {
        FuntimeUserAccount userAccount = accountMapper.selectByUserId(userId);
        if (userAccount!=null){
            String horn_price = parameterService.getParameterValueByKey("horn_price");
            if (StringUtils.isNotBlank(horn_price)) {
                userAccount.setHornPrice(new BigDecimal(horn_price));
            }
        }
        return userAccount;
    }

    @Override
    public void updateUserAccountForPlus(Long userId,BigDecimal blackDiamond, BigDecimal blueDiamond, Integer hornNumber) {
        int k = accountMapper.updateUserAccountForPlus(userId,blackDiamond,blueDiamond,hornNumber);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void updateUserAccountForPlusGift(Long userId, BigDecimal blackDiamond, Integer receivedGiftNum, Integer charmVal) {
        int k = accountMapper.updateUserAccountForPlusGift(userId,blackDiamond,receivedGiftNum,charmVal);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void updateUserAccountForSub(Long userId,BigDecimal blackDiamond, BigDecimal blueDiamond, Integer hornNumber) {
        checkAccountState(userId);
        FuntimeUserAccount info = getUserAccountInfoById(userId);
        if (info==null){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }
        BigDecimal exp ;
        if(blackDiamond!=null){
            exp = info.getBlackDiamond().subtract(blackDiamond);
            if(exp.intValue()<0){
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLACK_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_BLACK_NOT_EN.getDesc());
            }
        }
        if(blueDiamond!=null){
            exp = info.getBlueDiamond().subtract(blueDiamond);
            if(exp.intValue()<0){
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            }
        }
        if(hornNumber!=null){
            exp = new BigDecimal(info.getHornNumber() - hornNumber);
            if(exp.intValue()<0){
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_HORN_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_HORN_NOT_EN.getDesc());
            }
        }
        int k = accountMapper.updateUserAccountForSub(userId,blackDiamond,blueDiamond,hornNumber,info.getVersion(),System.currentTimeMillis());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

    }

    @Override
    public void updateUserAccountGoldCoinPlus(Long userId, Integer goldCoin) {
        int k = accountMapper.updateUserAccountGoldCoinPlus(userId,goldCoin);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }
    @Override
    public void updateUserAccountGoldCoinSub(Long userId, Integer goldCoin) {
        checkAccountState(userId);
        int k = accountMapper.updateUserAccountGoldCoinSub(userId,goldCoin);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void updatePhoneNumber(Long userId, String newPhoneNumber, String code, String oldPhoneNumber) {
        FuntimeUser user = userMapper.queryUserInfoByPhone(newPhoneNumber);
        if (user!=null){
            throw new BusinessException(ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getValue(),ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getDesc());
        }
        user = userMapper.selectByPrimaryKey(userId);
        if(user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        if (!oldPhoneNumber.equals(user.getPhoneNumber())){
            throw new BusinessException(ErrorMsgEnum.PHONE_NUMBER_IS_NOT_REGISTER.getValue(),ErrorMsgEnum.PHONE_NUMBER_IS_NOT_REGISTER.getDesc());
        }

        String isSend = parameterService.getParameterValueByKey("is_send");
        if (isSend!=null&&isSend.equals("1")) {
            smsService.validateSms(SmsType.UPDATE_PHONENUMBER.getValue(),newPhoneNumber,code);
        }

        int k = userMapper.updatePhoneNumberById(userId,user.getVersion(),System.currentTimeMillis(),newPhoneNumber);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

    }

    @Override
    public void updateAccountState(Long id) {
        FuntimeUser user = queryUserById(id);
        if (user!=null&&user.getAccountState() == 1){
            userMapper.updateAccountState(id);
        }

    }

    @Override
    public void checkAccountState(Long userId){
        Integer accountState = userMapper.getAccountState(userId);
        if (accountState==null||accountState == 2){
            throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_STOP.getValue(),ErrorMsgEnum.USER_ACCOUNT_STOP.getDesc());
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveUserValid(Long userId, String fullname, String identityCard, String depositCard, String code) {
        FuntimeUser user = queryUserById(userId);
        if(user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeUserValid userValid = queryValidInfoByUserId(userId);
        if(userValid!=null){
            throw new BusinessException(ErrorMsgEnum.USERVALID_IS_EXISTS.getValue(),ErrorMsgEnum.USERVALID_IS_EXISTS.getDesc());
        }
        String isSend = parameterService.getParameterValueByKey("is_send");
        if (isSend!=null&&isSend.equals("1")) {
            smsService.validateSms(SmsType.REAL_VALID.getValue(),user.getPhoneNumber(),code);
        }
        userValid = new FuntimeUserValid();
        userValid.setDepositCard(depositCard);
        userValid.setFullname(fullname);
        userValid.setIdentityCard(identityCard);
        userValid.setUserId(userId);
        if (userValidMapper.checkValidExist(userValid)==null) {
            BankCardVerificationUtil.bankCardVerification(depositCard, fullname, identityCard);
        }

        int k = userValidMapper.insertSelective(userValid);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        k = userMapper.updateRealnameAuthenticationFlagById(userId);

        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void updateUserValid(Long userId, String depositCard, String code) {
        FuntimeUser user = queryUserById(userId);
        if(user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeUserValid userValid = queryValidInfoByUserId(userId);
        if(userValid==null){
            throw new BusinessException(ErrorMsgEnum.USERVALID_IS_NOT_VALID.getValue(),ErrorMsgEnum.USERVALID_IS_NOT_VALID.getDesc());
        }
        if (userValid.getDepositCardReal().equals(depositCard)){
            throw new BusinessException(ErrorMsgEnum.USER_VALID_CARD_SAME.getValue(),ErrorMsgEnum.USER_VALID_CARD_SAME.getDesc());
        }
        String isSend = parameterService.getParameterValueByKey("is_send");
        if (isSend!=null&&isSend.equals("1")) {
            smsService.validateSms(SmsType.REAL_VALID.getValue(),user.getPhoneNumber(),code);
        }

        BankCardVerificationUtil.bankCardVerification(depositCard,userValid.getFullname(),userValid.getIdentityCard());


        userValid.setDepositCard(depositCard);
        userValid.setUserId(userId);
        int k = userValidMapper.updateByPrimaryKeySelective(userValid);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

    }

    @Override
    public void insertFuntimeImgeCallback(FuntimeImgeCallback imgeCallback) {
        userMapper.insertFuntimeImgeCallback(imgeCallback);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void doPoint(FuntimeDeviceInfo deviceInfo) {

        dataReport(deviceInfo);

        if (deviceInfo.getAdv() == null){
            deviceInfo.setAdv(2);
        }
        userMapper.insertDeviceInfo(deviceInfo);

    }

    private void dataReport(FuntimeDeviceInfo deviceInfo){
        Integer count ;
        try {
            if ("1".equals(deviceInfo.getOs())&&StringUtils.isNotBlank(deviceInfo.getIdfa())) {
                if ("startup".equals(deviceInfo.getPoint())) {
                    count = userMapper.checkDeviceExistsForApple(deviceInfo.getIdfa(),"startup");
                    String url = null;
                    if (count == 0) {
                        //快手
                        //url = advertisService.getCallBackUrlForKSApple(deviceInfo.getIdfa());
                        if (StringUtils.isNotBlank(url)) {
                            log.info("**************苹果快手激活数据上报*****************idfa:{}",deviceInfo.getIdfa());
                            url = URLDecoder.decode(url,"utf-8");
                            url = url + "&event_type=1&event_time=" + System.currentTimeMillis();
                            HttpClientUtil.doGet(url);
                            deviceInfo.setAdv(1);
                        }else {
                            //头条
                            url = advertisService.getCallBackUrlForQTTApple(deviceInfo.getIdfa());
                            if (StringUtils.isNotBlank(url)) {
                                log.info("**************苹果头条激活数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                url = URLDecoder.decode(url, "utf-8");
                                url = url + "&op2=0&opt_active_time=" + System.currentTimeMillis();
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }else{
                                //wifi
                                //Map<String,String> map = advertisService.getCallBackInfoForWifiApple(deviceInfo.getIdfa());
                                Map<String,String> map = null;
                                if (map!=null&&!map.isEmpty()){
                                    log.info("**************苹果WIFI激活数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                    url = getUrl(map, "1");
                                    HttpClientUtil.doGet(url);
                                    deviceInfo.setAdv(1);
                                }else{
                                    //知乎
                                    //url = advertisService.getCallBackUrlForZhihuApple(deviceInfo.getIdfa());
                                    if (StringUtils.isNotBlank(url)) {
                                        log.info("**************苹果知乎激活数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                        url = URLDecoder.decode(url, "utf-8");
                                        url = url.replaceAll("__EVENTTYPE__", "install")
                                                .replaceAll("__TIMESTAMP__", String.valueOf(System.currentTimeMillis()));
                                        HttpClientUtil.doGet(url);
                                        deviceInfo.setAdv(1);
                                    }else{
                                        //b站
                                        String trackid = advertisService.getTrackidForBstationApple(deviceInfo.getIdfa());
                                        if (StringUtils.isNotBlank(trackid)) {
                                            log.info("**************苹果b站激活数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                            url = Constant.BSTATION_CALLBACKURL+"conv_type=APP_FIRST_ACTIVE&conv_time="+System.currentTimeMillis()+"&client_ip="+deviceInfo.getIp()+"&track_id="+trackid;
                                            HttpClientUtil.doGet(url);
                                            deviceInfo.setAdv(1);
                                        }else{
                                            //sohu
                                            //url = advertisService.getCallBackForSohuApple(deviceInfo.getIdfa(), deviceInfo.getIp());
                                            log.info("**************苹果sohu激活数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                            if (StringUtils.isNotBlank(url)){

                                            }else{
                                                //美拍
                                                url = advertisService.getCallBackUrlForMeipaiApple(deviceInfo.getIdfa());
                                                if (StringUtils.isNotBlank(url)){
                                                    log.info("**************苹果美拍激活数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                                    url = URLDecoder.decode(url, "utf-8");
                                                    HttpClientUtil.doGet(url);
                                                    deviceInfo.setAdv(1);
                                                }else{
                                                    //最又
                                                    url = advertisService.getCallBackForZuiyouApple(deviceInfo.getIdfa());
                                                    if (StringUtils.isNotBlank(url)) {
                                                        log.info("**************苹果最右激活数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                                        url = URLDecoder.decode(url, "utf-8");
                                                        url = url.replace("__EVENT__","0");
                                                        HttpClientUtil.doGet(url);
                                                        deviceInfo.setAdv(1);
                                                    }else{
                                                        //url = advertisService.getCallBackUrlForChubaoApple(deviceInfo.getIdfa());
                                                        url = null;
                                                        if (StringUtils.isNotBlank(url)) {
                                                            log.info("**************苹果触宝激活数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                                            url = URLDecoder.decode(url, "utf-8");
                                                            url += "&conv_time=" + System.currentTimeMillis();
                                                            HttpClientUtil.doGet(url);
                                                            deviceInfo.setAdv(1);
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }

                            }
                        }
                    }
                }else if ("startIndex".equals(deviceInfo.getPoint())){
                    count = userMapper.checkDeviceExistsForApple(deviceInfo.getIdfa(),"startIndex");
                    String url = null;
                    if (count == 0) {
                        //快手
                        //url = advertisService.getCallBackUrlForKSApple(deviceInfo.getIdfa());
                        if (StringUtils.isNotBlank(url)) {
                            log.info("**************苹果快手首页数据上报*****************idfa:{}",deviceInfo.getIdfa());
                            url = URLDecoder.decode(url,"utf-8");
                            url = url + "&event_type=2&event_time=" + System.currentTimeMillis();
                            HttpClientUtil.doGet(url);
                            deviceInfo.setAdv(1);
                        }else {
                            //头条
                            url = advertisService.getCallBackUrlForQTTApple(deviceInfo.getIdfa());
                            if (StringUtils.isNotBlank(url)) {
                                log.info("**************苹果头条首页数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                url = URLDecoder.decode(url, "utf-8");
                                url = url + "&op2=1&opt_active_time=" + System.currentTimeMillis();
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }else{
                                //wifi
                                //Map<String,String> map = advertisService.getCallBackInfoForWifiApple(deviceInfo.getIdfa());
                                Map<String,String> map = null;
                                if (map!=null&&!map.isEmpty()){
                                    log.info("**************苹果WIFI首页数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                    url = getUrl(map, "2");
                                    HttpClientUtil.doGet(url);
                                    deviceInfo.setAdv(1);
                                }else{
                                    //url = advertisService.getCallBackUrlForZhihuApple(deviceInfo.getIdfa());
                                    if (StringUtils.isNotBlank(url)) {
                                        log.info("**************苹果知乎首页数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                        url = URLDecoder.decode(url, "utf-8");
                                        url = url.replaceAll("__EVENTTYPE__", "reged")
                                                .replaceAll("__TIMESTAMP__", String.valueOf(System.currentTimeMillis()));
                                        HttpClientUtil.doGet(url);
                                        deviceInfo.setAdv(1);
                                    }else{
                                        //b站
                                        String trackid = advertisService.getTrackidForBstationApple(deviceInfo.getIdfa());
                                        if (StringUtils.isNotBlank(trackid)) {
                                            log.info("**************苹果b站首页数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                            url = Constant.BSTATION_CALLBACKURL+"conv_type=USER_REGISTER&conv_time="+System.currentTimeMillis()+"&client_ip="+deviceInfo.getIp()+"&track_id="+trackid;
                                            HttpClientUtil.doGet(url);
                                            deviceInfo.setAdv(1);
                                        }else{
                                            //sohu
                                            url = advertisService.getCallBackForSohuApple(deviceInfo.getIdfa(),deviceInfo.getIp());
                                            if(StringUtils.isNotBlank(url)) {
                                                log.info("**************苹果sohu首页数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                                url = URLDecoder.decode(url, "utf-8");
                                                url = url.replaceAll("__TS__", String.valueOf(System.currentTimeMillis()));
                                                HttpClientUtil.doGet(url);
                                                deviceInfo.setAdv(1);
                                            }else{
                                                url = advertisService.getCallBackUrlForMeipaiApple(deviceInfo.getIdfa());
                                                if (StringUtils.isNotBlank(url)){
                                                    log.info("**************苹果美拍首页数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                                    url = URLDecoder.decode(url, "utf-8");
                                                    url += "&event_type=1";
                                                    HttpClientUtil.doGet(url);
                                                    deviceInfo.setAdv(1);
                                                }else {
                                                    url = advertisService.getCallBackUrlForChubaoApple(deviceInfo.getIdfa());
                                                    if (StringUtils.isNotBlank(url)) {
                                                        log.info("**************苹果触宝首页数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                                        url = URLDecoder.decode(url, "utf-8");
                                                        url += "&conv_time="+System.currentTimeMillis();
                                                        HttpClientUtil.doGet(url);
                                                        deviceInfo.setAdv(1);
                                                    }else{
                                                        url = advertisService.getCallBackForZuiyouApple(deviceInfo.getIdfa());
                                                        if (StringUtils.isNotBlank(url)) {
                                                            log.info("**************苹果最右首页数据上报*****************idfa:{}", deviceInfo.getIdfa());
                                                            url = URLDecoder.decode(url, "utf-8");
                                                            url = url.replace("__EVENT__","1");
                                                            HttpClientUtil.doGet(url);
                                                            deviceInfo.setAdv(1);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else if("0".equals(deviceInfo.getOs())&&StringUtils.isNotBlank(deviceInfo.getIp())&&StringUtils.isNotBlank(deviceInfo.getAndroidId())){
                if ("kuaishou".equals(deviceInfo.getChannel())) {
                    if ("consentAgreement".equals(deviceInfo.getPoint())||"rejectAgreement".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "consentAgreement");
                        if (count == 0) {
                            log.info("**************快手激活数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackUrlForKS2(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url = url + "&event_type=1&event_time=" + System.currentTimeMillis();
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    } else if ("startIndex".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "startIndex");
                        if (count == 0) {
                            log.info("**************快手首页数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackUrlForKS2(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url = url + "&event_type=2&event_time=" + System.currentTimeMillis();
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }

                    }
                }
                else if ("qutoutiao".equals(deviceInfo.getChannel())||"qutoutiao-wx".equals(deviceInfo.getChannel())||"qutoutiao-ld".equals(deviceInfo.getChannel())) {
                    if ("consentAgreement".equals(deviceInfo.getPoint())||"rejectAgreement".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "consentAgreement");
                        if (count == 0) {
                            log.info("**************头条激活数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackUrlForQTT2(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url = url + "&op2=0&opt_active_time=" + System.currentTimeMillis();
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    } else if ("startIndex".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "startIndex");
                        if (count == 0) {
                            log.info("**************头条首页数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackUrlForQTT2(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url = url + "&op2=1&opt_active_time=" + System.currentTimeMillis();
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    }
                }else if ("wifi".equals(deviceInfo.getChannel())||"bjtz-wifi".equals(deviceInfo.getChannel())){
                    if ("consentAgreement".equals(deviceInfo.getPoint())||"rejectAgreement".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "consentAgreement");
                        if (count == 0) {
                            log.info("**************WIFI激活数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            Map<String,String> data = advertisService.getCallBackInfoForWIFI(deviceInfo.getIp());
                            if (data!=null&&!data.isEmpty()) {
                                String url = getUrl(data, "1");
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    } else if ("startIndex".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "startIndex");
                        if (count == 0) {
                            log.info("**************WIFI首页数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            Map<String,String> data = advertisService.getCallBackInfoForWIFI(deviceInfo.getIp());
                            if (data!=null&&!data.isEmpty()) {
                                String url = getUrl(data, "2");
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    }
                }else if ("zhihu".equals(deviceInfo.getChannel())){
                    if ("consentAgreement".equals(deviceInfo.getPoint())||"rejectAgreement".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "consentAgreement");
                        if (count == 0) {
                            log.info("**************知乎激活数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackUrlForZhihu(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url = url.replaceAll("__EVENTTYPE__", "install")
                                        .replaceAll("__TIMESTAMP__", String.valueOf(System.currentTimeMillis()));
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    } else if ("startIndex".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "startIndex");
                        if (count == 0) {
                            log.info("**************知乎首页数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackUrlForZhihu(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url = url.replaceAll("__EVENTTYPE__", "reged")
                                        .replaceAll("__TIMESTAMP__", String.valueOf(System.currentTimeMillis()));
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    }
                }else if("b-jwn".equals(deviceInfo.getChannel())||"b-chujian".equals(deviceInfo.getChannel())){
                    Integer channel = "b-jwn".equals(deviceInfo.getChannel())?1:2;
                    if ("consentAgreement".equals(deviceInfo.getPoint())||"rejectAgreement".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "consentAgreement");
                        if (count == 0) {
                            log.info("**************B站激活数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String trackid = advertisService.getTrackidForBstation(deviceInfo.getIp(),channel);
                            if (StringUtils.isNotBlank(trackid)) {
                                String url = Constant.BSTATION_CALLBACKURL+"conv_type=APP_FIRST_ACTIVE&conv_time="+System.currentTimeMillis()+"&client_ip="+deviceInfo.getIp()+"&track_id="+trackid;

                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    } else if ("startIndex".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "startIndex");
                        if (count == 0) {
                            log.info("**************B站首页数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String trackid = advertisService.getTrackidForBstation(deviceInfo.getIp(),channel);
                            if (StringUtils.isNotBlank(trackid)) {
                                String url = Constant.BSTATION_CALLBACKURL+"conv_type=USER_REGISTER&conv_time="+System.currentTimeMillis()+"&client_ip="+deviceInfo.getIp()+"&track_id="+trackid;

                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    }
                }else if ("sohu".equals(deviceInfo.getChannel())){
                    if ("consentAgreement".equals(deviceInfo.getPoint())||"rejectAgreement".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "consentAgreement");
                        if (count == 0) {
                            log.info("**************sohu激活数据上报*****************androidId:{}", deviceInfo.getAndroidId());
                        }
                    } else if ("startIndex".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "startIndex");
                        if (count == 0) {
                            log.info("**************sohu首页数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackForSohu(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url = url.replaceAll("__TS__", String.valueOf(System.currentTimeMillis()));
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    }
                }else if("meipai".equals(deviceInfo.getChannel())){
                    if ("consentAgreement".equals(deviceInfo.getPoint())||"rejectAgreement".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "consentAgreement");
                        if (count == 0) {
                            log.info("**************美拍激活数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackUrlForMeipai(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    } else if ("startIndex".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "startIndex");
                        if (count == 0) {
                            log.info("**************美拍首页数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackUrlForMeipai(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url+="&event_type=1";
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    }
                }else if("chubao".equals(deviceInfo.getChannel())){
                    if ("startIndex".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "startIndex");
                        if (count == 0) {
                            String url = advertisService.getCallBackUrlForChubao(deviceInfo.getIp());
                            log.info("**************触宝注册数据上报*****************androidId:{},url=={}",deviceInfo.getAndroidId(),url==null?"no url "+deviceInfo.getIp():"url true");
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url+="&conv_time="+System.currentTimeMillis();
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    }
                }else if ("zuiyou".equals(deviceInfo.getChannel())){
                    if ("consentAgreement".equals(deviceInfo.getPoint())||"rejectAgreement".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "consentAgreement");
                        if (count == 0) {
                            log.info("**************最右激活数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackForZuiyou(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url = url.replace("__EVENT__","0");
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    } else if ("startIndex".equals(deviceInfo.getPoint())) {
                        count = userMapper.checkDeviceExistsForAndroid(deviceInfo.getAndroidId(), "startIndex");
                        if (count == 0) {
                            log.info("**************最右首页数据上报*****************androidId:{}",deviceInfo.getAndroidId());
                            String url = advertisService.getCallBackForZuiyou(deviceInfo.getIp());
                            if (StringUtils.isNotBlank(url)) {
                                url = URLDecoder.decode(url, "utf-8");
                                url = url.replace("__EVENT__","1");
                                HttpClientUtil.doGet(url);
                                deviceInfo.setAdv(1);
                            }
                        }
                    }
                }
            }else {

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getUrl(Map<String, String> data,String eventType) throws Exception {
        data.put("clientid",Constant.WIFI_CLIENT_ID);
        data.put("ts",System.currentTimeMillis()+"");
        data.put("event_type",eventType);
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (StringUtils.isNotBlank(data.get(k))) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.get(k).trim()).append("&");
        }
        String url = Constant.WIFI_CALLBACKURL+sb.toString();
        sb.append("secretkey=").append(Constant.WIFI_SECRETKEY);
        return url+"sign="+StringUtil.MD5(sb.toString()).toUpperCase();

    }



    @Override
    public FuntimeUser queryUserInfoByPhoneImei(String phoneImei) {
        return userMapper.queryUserInfoByImei(phoneImei);
    }

    @Override
    public void checkSensitive(String content) {
       Integer count = userMapper.checkSensitive(content);
       if (count>0){
           throw new BusinessException(ErrorMsgEnum.SENSITIVE_ERROR.getValue(),ErrorMsgEnum.SENSITIVE_ERROR.getDesc());
       }
    }

    @Override
    public void getBlockDevice(String phoneImei) {
        Integer count = userMapper.getBlockDevice(phoneImei);
        if (count>0){
            throw new BusinessException(ErrorMsgEnum.USER_DEVICE_BLOCK.getValue(),ErrorMsgEnum.USER_DEVICE_BLOCK.getDesc());
        }
    }

    @Override
    public void checkForbiddenWords(Long userId) {
        Integer hours = userMapper.checkForbiddenWords(userId);
        if (hours!=null){
            throw new BusinessException(ErrorMsgEnum.USER_FORBIDDEN_WORDS.getValue(),ErrorMsgEnum.USER_FORBIDDEN_WORDS.getDesc().replace("X",hours.toString()));
        }
    }

    @Override
    public void cancellation(Long userId, String code) {

        FuntimeUser user = queryUserById(userId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (StringUtils.isBlank(user.getPhoneNumber())){
            throw new BusinessException(ErrorMsgEnum.USER_PHONE_NOT_BIND.getValue(),ErrorMsgEnum.USER_PHONE_NOT_BIND.getDesc());
        }
        String isSend = parameterService.getParameterValueByKey("is_send");
        if (isSend != null && isSend.equals("1")) {
            //校验验证码
            smsService.validateSms(SmsType.USERCANCELATION.getValue(), user.getPhoneNumber(), code);
        }
        Long roomId = roomService.checkUserIsInRoom(userId);
        if (roomId!=null){
            roomService.roomExit(userId, roomId);
        }

        userMapper.userCancellation(userId);

        userThirdMapper.deleteByUserId(userId);


    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void updateUserLocation(Long userId, String longitude, String latitude) {
        FuntimeUser user = new FuntimeUser();
        user.setId(userId);
        user.setLongitude(longitude);
        user.setLatitude(latitude);
        int k = userMapper.updateByPrimaryKeySelective(user);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        k = userMapper.insertUserLocationLog(userId,longitude,latitude);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public FuntimeUserValid queryValidInfoByUserId(Long userId) {

        FuntimeUserValid userValid = userValidMapper.selectByUserId(userId);

        return userValid;
    }



    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveConcern(Long userId, Long toUserId) {
        Long id = userConcernMapper.checkRecordExist(userId,toUserId);
        if(id!=null){
            throw new BusinessException(ErrorMsgEnum.USERCONCERN_IS_EXISTS.getValue(),ErrorMsgEnum.USERCONCERN_IS_EXISTS.getDesc());
        }
        FuntimeUserConcern concern = new FuntimeUserConcern();
        concern.setUserId(userId);
        concern.setToUserId(toUserId);
        int k = userConcernMapper.insertSelective(concern);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        userMapper.updateConcernsPlus(userId);
        userMapper.updateFansPlus(toUserId);
        if (userConcernMapper.checkRecordExist(toUserId,userId)!=null){
            Long var1 = userId>toUserId?toUserId:userId;
            Long var2 = userId>toUserId?userId:toUserId;
            if (userConcernMapper.checkFriendExist(var1,var2)==null) {
                userConcernMapper.insertUserFriend(var1, var2);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deleteConcern(Long userId, Long toUserId) {
        Long id = userConcernMapper.checkRecordExist(userId,toUserId);
        if(id==null){
            throw new BusinessException(ErrorMsgEnum.USERCONCERN_IS_NOT_EXISTS.getValue(),ErrorMsgEnum.USERCONCERN_IS_NOT_EXISTS.getDesc());
        }

        int k = userConcernMapper.deleteByPrimaryKey(id);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        userMapper.updateConcernsSub(userId);
        userMapper.updateFansSub(toUserId);
        Long var1 = userId>toUserId?toUserId:userId;
        Long var2 = userId>toUserId?userId:toUserId;
        if (userConcernMapper.checkFriendExist(var1,var2)!=null) {
            userConcernMapper.delUserFriend(var1,var2);
        }

    }

    @Override
    public void updateOnlineState(Long userId, Integer onlineState) {

        if(userMapper.updateOnlineState(userId, onlineState)!=1){

            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        RedisUser redisUser = (RedisUser) redisUtil.get(Constant.REDISUSER_PREFIX+userId);
        if (redisUser!=null) {
            redisUser.onlineState = onlineState;
            redisUtil.set(Constant.REDISUSER_PREFIX+userId,redisUser);
        }

    }



    @Override
    public void updateShowIdById(Long userId) {
        userMapper.updateShowIdById(userId);
    }

    @Override
    public void updateCreateRoomPlus(Long id) {
        userMapper.updateCreateRoomPlus(id);
    }

    @Override
    public void updateCreateRoomSub(Long id) {

        userMapper.updateCreateRoomSub(id);
    }

    @Override
    public PageInfo<FuntimeUser> queryUserInfoByOnline(Integer startPage, Integer pageSize, Integer sex, Integer ageType, Long userId) {

        String startAge = null;
        String endAge = null;
        if (ageType!=null) {
            if (ageType.intValue() == 1) {//小于23
                endAge = DateUtil.getCurrentYearAdd(new Date(), -23);
            } else if (ageType.intValue() == 2) {
                startAge = DateUtil.getCurrentYearAdd(new Date(), -24);
                endAge = DateUtil.getCurrentYearAdd(new Date(), -29);
            } else if (ageType.intValue() == 3) {
                startAge = DateUtil.getCurrentYearAdd(new Date(), -30);
                endAge = DateUtil.getCurrentYearAdd(new Date(), -39);
            } else if (ageType.intValue() == 4) {
                startAge = DateUtil.getCurrentYearAdd(new Date(), -40);
                endAge = DateUtil.getCurrentYearAdd(new Date(), -49);
            } else if (ageType.intValue() == 5) {
                startAge = DateUtil.getCurrentYearAdd(new Date(), -50);
                endAge = DateUtil.getCurrentYearAdd(new Date(), -59);
            } else if (ageType.intValue() == 6) {
                startAge = DateUtil.getCurrentYearAdd(new Date(), -60);
            } else {
                throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(), ErrorMsgEnum.PARAMETER_ERROR.getDesc());
            }
        }
        PageHelper.startPage(startPage,pageSize);
        List<FuntimeUser> list = userMapper.queryUserInfoByOnline(sex,startAge,endAge,userId);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            for (FuntimeUser user:list){
                if (user.getBirthday()!=null) {
                    Integer birthday = user.getBirthday();
                    user.setAge(DateUtil.getAgeByBirthday(birthday));
                    user.setConstellation(DateUtil.getConstellationByBirthday(birthday));
                }
                if (user.getSex()!=null){
                    if (user.getSex() == 1){
                        user.setSexColor(Constant.SEX_MALE_COLOR);
                    }else {
                        user.setSexColor(Constant.SEX_FEMALE_COLOR);
                    }
                }
                if (user.getHeight()!=null){
                    user.setHeightColor(Constant.HEIGHT_COLOR);
                }

                List<Map<String, Object>> tagNames = tagMapper.queryTagNamesByUserId(user.getId());
                if (tagNames!=null&&!tagNames.isEmpty()){
                    for (Map<String, Object> map : tagNames){
                        if (map.get("tagType").toString()!=null){
                            map.put("tagColor", TagColorEnmu.getDescByValue(map.get("tagType").toString()));
                        }

                    }
                }
                user.setTagNames(tagNames);

            }

            return new PageInfo<>(list);
        }
    }


    @Override
    public PageInfo<Map<String,Object>> getUserList(Integer startPage, Integer pageSize, Integer sex, Long userId, Integer tagId, BigDecimal longitude, BigDecimal latitude, String ip) {

        List<Map<String,Object>> list = null;

        if (tagId == 81) {
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getUserList1(sex, userId);
        }
        if (tagId == 82) {
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getUserList2(sex, userId);
        }
        if (tagId == 83) {
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getUserList3(sex, userId);
        }
        if (tagId == 84) {
            if (longitude==null||latitude==null){
                FuntimeUser user = queryUserById(userId);
                longitude = user.getLongitude()==null?null:new BigDecimal(user.getLongitude());
                latitude = user.getLatitude()==null?null:new BigDecimal(user.getLatitude());
                if(longitude==null||latitude==null){
                    list = null;
                }else {
                    PageHelper.startPage(startPage, pageSize);
                    list = userMapper.getUserList4(sex, userId, longitude, latitude);
                }
            }else {
                updateUserLocation(userId,longitude.toString(),latitude.toString());
                PageHelper.startPage(startPage,pageSize);
                list = userMapper.getUserList4(sex, userId, longitude, latitude);
            }
        }
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            for (Map<String,Object> userMap:list){
                if (userMap.get("birthday")!=null) {
                    Integer birthday = Integer.parseInt(userMap.get("birthday").toString());
                    userMap.put("age",DateUtil.getAgeByBirthday(birthday));
                    userMap.put("constellation",DateUtil.getConstellationByBirthday(birthday));
                }
                if (userMap.get("sex")!=null){
                    if (Integer.parseInt(userMap.get("sex").toString()) == 1){
                        userMap.put("sexColor",Constant.SEX_MALE_COLOR);
                    }else {
                        userMap.put("sexColor",Constant.SEX_FEMALE_COLOR);
                    }
                }
                if (userMap.get("height")!=null){
                    userMap.put("heightColor",Constant.HEIGHT_COLOR);
                }

                List<Map<String, Object>> tagNames = tagMapper.queryTagNamesByUserId(Long.parseLong(userMap.get("id").toString()));
                if (tagNames!=null&&!tagNames.isEmpty()){
                    for (Map<String, Object> map : tagNames){
                        if (map.get("tagType").toString()!=null){
                            map.put("tagColor", TagColorEnmu.getDescByValue(map.get("tagType").toString()));
                        }

                    }
                }
                userMap.put("tagNames",tagNames);
            }

            return new PageInfo<>(list);
        }
    }

    @Override
    public void doAction(Long userId, String page, String ip) {

        int k = userMapper.insertUserAction(userId,page,ip);
    }

    @Override
    public Map<String, Object> checkSendImCounts(Long userId, Long toUserId) {
        Map<String, Object> result = new HashMap<>();
        result.put("isBlacklist",false);
        Integer k = userMapper.checkBlacklist(userId, toUserId);
        if (k != null){
            result.put("isBlacklist",true);
            result.put("msg",ErrorMsgEnum.USER_BLACKLIST_ADDED.getDesc());
        }
        k = userMapper.checkBlacklist(toUserId,userId);
        if (k != null){
            result.put("isBlacklist",true);
            result.put("msg",ErrorMsgEnum.USER_BLACKLIST_ADDED2.getDesc());
        }

        Long var1 = userId>toUserId?toUserId:userId;
        Long var2 = userId>toUserId?userId:toUserId;
        if (userConcernMapper.checkFriendExist(var1,var2)==null) {
            Integer dayTime = DateUtil.getCurrentInt();
            k = userMapper.getUserImRecord(userId,toUserId,dayTime);
            if (k == null){
                k = userMapper.getUserImDayCount(userId,dayTime);
                if(k!=null&&k>0){
                    result.put("sendAgreen",true);
                }else{
                    result.put("sendAgreen",false);
                    result.put("unlockGifts",giftMapper.getGiftListByUnlock());
                }
            }else{
                result.put("sendAgreen",true);
            }
        }else{
            result.put("sendAgreen",true);
        }
        return result;
    }

    @Override
    public void subImCounts(Long userId, Long toUserId) {
        Long var1 = userId>toUserId?toUserId:userId;
        Long var2 = userId>toUserId?userId:toUserId;
        if (userConcernMapper.checkFriendExist(var1,var2)==null) {
            Integer dayTime = DateUtil.getCurrentInt();
            Integer k = userMapper.getUserImRecord(userId, toUserId, dayTime);
            if (k == null) {
                k = userMapper.getUserImDayCount(userId, dayTime);
                if (k < 1) {
                    throw new BusinessException(ErrorMsgEnum.USER_IMCOUNTS_EXCEED.getValue(), ErrorMsgEnum.USER_IMCOUNTS_EXCEED.getDesc());
                }
                insertUserImRecord(userId, toUserId, dayTime, 2);
            }
        }
    }

    @Override
    public void insertUserImRecord(Long userId, Long toUserId, Integer dayTime,Integer unlock){
        userMapper.insertUserImRecord(userId, toUserId, dayTime,unlock);
    }

    @Override
    public PageInfo<Map<String,Object>> getBlacklists(Integer startPage, Integer pageSize, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String, Object>> blacklists = userMapper.getBlacklists(userId);
        if(blacklists==null||blacklists.isEmpty()) {
            return new PageInfo<>();
        }
        return new PageInfo<>(blacklists);
    }

    @Override
    public void addBlacklist(Long userId, Long toUserId) {
        if (userMapper.checkBlacklist(userId,toUserId) == null) {
            userMapper.insertUserBlacklist(userId, toUserId);
        }
    }

    @Override
    public void delBlacklist(Long userId, Long toUserId) {
        if (userMapper.checkBlacklist(userId,toUserId) != null) {
            userMapper.delBlacklist(userId, toUserId);
        }
    }

    @Override
    public Map<String, Object> getDdzUserInfoById(Long userId) {
        return userMapper.getDdzUserInfoById(userId);
    }

    @Override
    public void checkBlacklist(Long userId, Long toUserId) {
        Integer k = userMapper.checkBlacklist(userId, toUserId);
        if (k != null){
            throw new BusinessException(ErrorMsgEnum.USER_BLACKLIST_ADDED.getValue(), ErrorMsgEnum.USER_BLACKLIST_ADDED.getDesc());
        }
        k = userMapper.checkBlacklist(toUserId,userId);
        if (k != null){
            throw new BusinessException(ErrorMsgEnum.USER_BLACKLIST_ADDED2.getValue(), ErrorMsgEnum.USER_BLACKLIST_ADDED2.getDesc());
        }
    }

    @Override
    public void saveDeviceToken(String deviceToken, Long userId) {
        userId = userId == null?0:userId;
        List<Long> list = userMapper.checkTokenExists(deviceToken);
        if (list==null||list.isEmpty()) {
            userMapper.saveDeviceToken(deviceToken, userId);
        }else{
            if (userId > 0){
                if (list.size() == 1&&list.get(0) == 0){
                    userMapper.updateDeviceToken(deviceToken, userId);
                }else {
                    if (!list.contains(userId)) {
                        userMapper.saveDeviceToken(deviceToken, userId);
                    }
                }
            }
        }
    }

    @Override
    public String getUserCounts(String startDate, String endDate, String channel) {
        return userMapper.getUserCounts(startDate,endDate,channel);
    }

    @Override
    public List<Map<String,Object>> getGiftByUserId(Long userId) {
        List<Map<String, Object>> list = giftMapper.getGiftByUserId(userId);
        if (list==null||list.isEmpty()){
            return null;
        }
        return list;
    }

    @Override
    public List<FuntimeUserPhotoAlbum> getPhotoByUserId(Long userId) {
        List<FuntimeUserPhotoAlbum> list = userPhotoAlbumMapper.getPhotoAlbumByUserId(userId);
        if (list == null || list.isEmpty()){
            return null;
        }
        return list;
    }

    @Override
    public Map<String, Object> queryUserByChatUser(Long userId, Long byUserId) {
        Map<String,Object> result = userMapper.queryUserByChatUser(userId,byUserId);

        if (result!=null&&result.get("birthday")!=null){

            int birthday = Integer.valueOf(result.get("birthday").toString());

            result.put("age",DateUtil.getAgeByBirthday(birthday));

            result.put("constellation",DateUtil.getConstellationByBirthday(birthday));

            if (result.get("sex")!=null){
                Integer sex = Integer.parseInt(result.get("sex").toString());
                if (sex == 1){
                    result.put("sexColor",Constant.SEX_MALE_COLOR);
                }else {
                    result.put("sexColor",Constant.SEX_FEMALE_COLOR);
                }
            }
            if (result.get("height")!=null){
                result.put("height",Constant.HEIGHT_COLOR);
            }

        }
        return result;
    }

    @Override
    public List<Map<String,Object>> queryAuthorityByRole(Integer userRole) {
        return userMapper.queryAuthorityByRole(userRole);
    }

    @Override
    public boolean checkAuthorityForUserRole(Integer userRole, Integer authority) {
        if (userMapper.checkAuthorityForUserRole(userRole,authority)==null){
            return false;
        }
        return true;
    }

    @Override
    public PageInfo<FuntimeUser> getConcernUserList(Integer startPage, Integer pageSize, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<FuntimeUser> list = userMapper.getConcernUserList(userId);

        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            for (FuntimeUser user:list){
                if (user.getBirthday()!=null) {
                    Integer birthday = user.getBirthday();
                    user.setAge(DateUtil.getAgeByBirthday(birthday));
                    user.setConstellation(DateUtil.getConstellationByBirthday(birthday));
                }
                if (user.getSex()!=null){
                    if (user.getSex() == 1){
                        user.setSexColor(Constant.SEX_MALE_COLOR);
                    }else {
                        user.setSexColor(Constant.SEX_FEMALE_COLOR);
                    }
                }
                if (user.getHeight()!=null){
                    user.setHeightColor(Constant.HEIGHT_COLOR);
                }

                List<Map<String, Object>> tagNames = tagMapper.queryTagNamesByUserId(user.getId());
                if (tagNames!=null&&!tagNames.isEmpty()){
                    for (Map<String, Object> map : tagNames){
                        if (map.get("tagType").toString()!=null){
                            map.put("tagColor", TagColorEnmu.getDescByValue(map.get("tagType").toString()));
                        }

                    }
                }
                user.setTagNames(tagNames);

            }

            return new PageInfo<>(list);
        }
    }

    @Override
    public PageInfo<FuntimeUser> getFansList(Integer startPage, Integer pageSize, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<FuntimeUser> list = userMapper.getFansList(userId);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            for (FuntimeUser user:list){
                if (user.getBirthday()!=null) {
                    Integer birthday = user.getBirthday();
                    user.setAge(DateUtil.getAgeByBirthday(birthday));
                    user.setConstellation(DateUtil.getConstellationByBirthday(birthday));
                }
                if (user.getSex()!=null){
                    if (user.getSex() == 1){
                        user.setSexColor(Constant.SEX_MALE_COLOR);
                    }else {
                        user.setSexColor(Constant.SEX_FEMALE_COLOR);
                    }
                }
                if (user.getHeight()!=null){
                    user.setHeightColor(Constant.HEIGHT_COLOR);
                }

                List<Map<String, Object>> tagNames = tagMapper.queryTagNamesByUserId(user.getId());
                if (tagNames!=null&&!tagNames.isEmpty()){
                    for (Map<String, Object> map : tagNames){
                        if (map.get("tagType").toString()!=null){
                            map.put("tagColor", TagColorEnmu.getDescByValue(map.get("tagType").toString()));
                        }

                    }
                }
                user.setTagNames(tagNames);

            }

            return new PageInfo<>(list);
        }
    }

    @Override
    public Map<String, Object> getRankingList(Integer dateType, Integer type, String curUserId) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("is_ranklist_show",parameterService.getParameterValueByKey("is_ranklist_show"));
        String count = parameterService.getParameterValueByKey("ranking_list_count");
        resultMap.put("rankCount",count);
        int endCount = Integer.parseInt(count);

        String startDate;
        String endDate;
        Integer dateTypeConf = dateType;
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
            dateTypeConf = 1;
            startDate = DateUtil.getLastDayStart();
            endDate = DateUtil.getLastDayEnd();
        }else if (dateType == 5){
            dateTypeConf = 2;
            startDate = DateUtil.getLastWeekStart();
            endDate = DateUtil.getLastWeekEnd();
        }else if (dateType == 6){
            dateTypeConf = 3;
            startDate = DateUtil.getLastMonthStart();
            endDate = DateUtil.getLastMonthEnd();
        }else {
            resultMap.put("rankingList",null);
            return resultMap;
        }

        List<Map<String, Object>> list;

        Map<String,Object> conf = new HashMap<>();
        List<Map<String, Object>> charmList = new ArrayList<>();
        List<Map<String, Object>> contributionList = new ArrayList<>();
        String[] array = {"一", "二", "三", "四", "五", "六", "七", "八", "九","十"};
        list = userMapper.getRankRewardConf(dateTypeConf);
        if (list!=null&&!list.isEmpty()) {
            resultMap.put("isRewardShow",true);
            for (Map<String, Object> map : list) {
                int rankType = Integer.parseInt(map.get("rankType").toString());
                int ranking = Integer.parseInt(map.get("ranking").toString());
                map.put("rankingName", "第" + array[ranking - 1] + "名");
                if (rankType == 1) {
                    charmList.add(map);
                } else {
                    contributionList.add(map);
                }
            }
            conf.put("charmConf", charmList);
            conf.put("contributionConf", contributionList);
            resultMap.put("conf", conf);
        }else{
            resultMap.put("isRewardShow",false);
        }

        if (type == 1){
            list = userMapper.getCharmList(startDate,endDate,endCount);
        }else if(type == 2){
            list = userMapper.getContributionList(startDate,endDate,endCount);
        }else{
            list = userMapper.getHotList(startDate,endDate,endCount);
        }

        if (list==null||list.isEmpty()){
            resultMap.put("rankingList",null);
            return resultMap;
        }
        FuntimeUser user = userMapper.selectByPrimaryKey(Long.parseLong(curUserId));
        FuntimeUserAccount userAccount= accountMapper.selectByUserId(Long.parseLong(curUserId));
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

    public Map<String, Object> getRankingList(Integer dateType, String curUserId) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("is_ranklist_show",parameterService.getParameterValueByKey("is_ranklist_show"));
        String count = parameterService.getParameterValueByKey("ddz_rank_count");
        resultMap.put("rankCount",count);

        Integer dateTypeConf = dateType;
        if (dateType == 4){
            dateTypeConf = 1;
        }else if (dateType == 5){
            dateTypeConf = 2;
        }else if (dateType == 6){
            dateTypeConf = 3;
        }else {
            resultMap.put("rankingList",null);
            return resultMap;
        }

        List<Map<String, Object>> list;

        Map<String,Object> conf = new HashMap<>();
        List<Map<String, Object>> charmList = new ArrayList<>();
        List<Map<String, Object>> ddzList = new ArrayList<>();
        List<Map<String, Object>> contributionList = new ArrayList<>();
        String[] array = {"一", "二", "三", "四", "五", "六", "七", "八", "九","十"};
        list = userMapper.getRankRewardConf(dateTypeConf);
        if (list!=null&&!list.isEmpty()) {
            resultMap.put("isRewardShow",true);
            for (Map<String, Object> map : list) {
                int rankType = Integer.parseInt(map.get("rankType").toString());
                int ranking = Integer.parseInt(map.get("ranking").toString());
                map.put("rankingName", "第" + array[ranking - 1] + "名");
                if (rankType == 1) {
                    charmList.add(map);
                } else if (rankType == 2){
                    contributionList.add(map);
                }else{
                    ddzList.add(map);
                }
            }
            conf.put("charmConf", charmList);
            conf.put("contributionConf", contributionList);
            conf.put("ddzConf",ddzList);
            resultMap.put("conf", conf);
        }else{
            resultMap.put("isRewardShow",false);
        }


        list = ddzService.getRankList();


        if (list==null||list.isEmpty()){
            resultMap.put("rankingList",null);
            return resultMap;
        }
        FuntimeUser user = userMapper.selectByPrimaryKey(Long.parseLong(curUserId));
        FuntimeUserAccount userAccount= accountMapper.selectByUserId(Long.parseLong(curUserId));
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
    @Transactional(rollbackFor = Throwable.class)
    public void updatePhotoByUserId(Long userId, JSONArray array) {
        if (userMapper.checkUserExists(userId)==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        userPhotoAlbumMapper.deleteByUserId(userId);
        if (array!=null&&array.size()>0) {
            JSONObject object;
            FuntimeUserPhotoAlbum photoAlbum;
            List<FuntimeUserPhotoAlbum> list = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                object = array.getJSONObject(i);
                photoAlbum = JSONObject.toJavaObject(object, FuntimeUserPhotoAlbum.class);

                if (photoAlbum.getResourceUrl().contains("funtime-1300805214.cos.ap-shanghai.myqcloud.com")){
                    photoAlbum.setResourceUrl(photoAlbum.getResourceUrl().replace("funtime-1300805214.cos.ap-shanghai.myqcloud.com",Constant.COS_URL_PREFIX2));
                }
                photoAlbum.setUserId(userId);
                list.add(photoAlbum);
            }
            userPhotoAlbumMapper.insertBatch(list);
        }
    }

    @Override
    public void makeAccusation(FuntimeAccusation accusation) {
        accusation.setState(1);
        if (accusation.getImg1()!=null){
            accusation.setImg1(Constant.COS_URL_PREFIX+"/"+accusation.getImg1());
        }
        if (accusation.getImg2()!=null){
            accusation.setImg2(Constant.COS_URL_PREFIX+"/"+accusation.getImg2());
        }
        if (accusation.getImg3()!=null){
            accusation.setImg3(Constant.COS_URL_PREFIX+"/"+accusation.getImg3());
        }
        if (accusation.getImg4()!=null){
            accusation.setImg4(Constant.COS_URL_PREFIX+"/"+accusation.getImg4());
        }
        if (accusation.getImg5()!=null){
            accusation.setImg5(Constant.COS_URL_PREFIX+"/"+accusation.getImg5());
        }
        if (accusationMapper.insertSelective(accusation)!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void saveHeart(Long userId, String ipAddr) {
        if (checkUserExists(userId)){
            userMapper.saveHeart(userId,ipAddr);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveImHeart(Long userId,Integer userState,String action,String reason) {
        if (checkUserExists(userId)) {
            userMapper.saveImHeart(userId, userState, action, reason);
            if (userState == 1) {
                updateOnlineState(userId, 1);
            }
        }
    }

    @Override
    public List<Long> getOfflineUser() {
        return userMapper.getOfflineUser();
    }

    @Override
    public List<Map<String, Object>> getExpression() {
        List<Map<String, Object>> list = userMapper.getExpression();

        return list;
    }

    @Override
    public List<Map<String, Object>> getBanners() {
        List<Map<String, Object>> list = userMapper.getBanners();

        return list;
    }

    @Override
    public PageInfo<FuntimeUser> queryUserInfoByIndex(Integer startPage, Integer pageSize, String content, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<FuntimeUser> list = userMapper.queryUserInfoByIndex(content,userId);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            for (FuntimeUser user:list){
                if (user.getBirthday()!=null) {
                    Integer birthday = user.getBirthday();
                    user.setAge(DateUtil.getAgeByBirthday(birthday));
                    user.setConstellation(DateUtil.getConstellationByBirthday(birthday));
                }
                if (user.getSex()!=null){
                    if (user.getSex() == 1){
                        user.setSexColor(Constant.SEX_MALE_COLOR);
                    }else {
                        user.setSexColor(Constant.SEX_FEMALE_COLOR);
                    }
                }
                if (user.getHeight()!=null){
                    user.setHeightColor(Constant.HEIGHT_COLOR);
                }

                List<Map<String, Object>> tagNames = tagMapper.queryTagNamesByUserId(user.getId());
                if (tagNames!=null&&!tagNames.isEmpty()){
                    for (Map<String, Object> map : tagNames){
                        if (map.get("tagType").toString()!=null){
                            map.put("tagColor", TagColorEnmu.getDescByValue(map.get("tagType").toString()));
                        }

                    }
                }
                user.setTagNames(tagNames);

            }

            return new PageInfo<>(list);
        }
    }

    @Override
    public Map<String, Object> getCustomerService() {
        return userMapper.getCustomerService();
    }

    @Override
    public Map<String, Object> getUserValidInfo(Long userId) {
        Map<String,Object> result = new HashMap<>();
        FuntimeUserValid userValid = queryValidInfoByUserId(userId);
        if (userValid!=null){
            userValid.setIdentityCard(userValid.getIdentityCard().substring(0,6)+"************");
            int len = userValid.getDepositCardReal().length();
            userValid.setDepositCard("************"+userValid.getDepositCardReal().substring(len-4,len));
            userValid.setDepositCardReal(null);
        }
        result.put("userValid",userValid);
        return result;
    }

    @Override
    public Map<String, Object> getWithdralInfo(Long userId) {
        Map<String,Object> result = new HashMap<>();
        FuntimeUserValid userValid = queryValidInfoByUserId(userId);
        result.put("userValid",userValid);
        List<FuntimeWithdrawalConf> withdralConf = withdrawalConfMapper.getWithdralConf();
        result.put("withdralConf",withdralConf);
        String black_to_rmb = parameterService.getParameterValueByKey("black_to_rmb");
        result.put("black_to_rmb",black_to_rmb);
        result.put("agreementUrl",Constant.COS_URL_PREFIX+Constant.AGREEMENT_WITHDRAL);

        result.put("withdrawalMaxDay",parameterService.getParameterValueByKey("withdrawal_max_day"));
        result.put("withdrawalMinOnce",parameterService.getParameterValueByKey("withdrawal_min_once"));
        result.put("withdrawalWxAmount",parameterService.getParameterValueByKey("withdrawal_wx_amount"));
        FuntimeUserThird userThird = queryUserThirdIdByType(userId, Constant.LOGIN_WX);
        if (userThird==null){
            result.put("wx_bind",false);
        }else{
            result.put("wx_bind",true);
            result.put("wxNickname",userThird.getNickname());
        }
        return result;
    }

    @Override
    public void logout(Long userId) {
        if (userMapper.checkUserExists(userId)==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (userMapper.checkUserAllowOffline(userId) == null) {
            updateOnlineState(userId, 2);
            Long roomId = roomService.checkUserIsInRoom(userId);
            if (roomId != null) {
                roomService.roomExit(userId, roomId);
            }
        }
    }

    @Override
    public void validPhone(Long userId, String code, String oldPhoneNumber) {
        if (!checkUserExists(userId)){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        String isSend = parameterService.getParameterValueByKey("is_send");
        if (isSend!=null&&isSend.equals("1")) {
            smsService.validateSms(SmsType.UPDATE_PHONENUMBER.getValue(),oldPhoneNumber,code);
        }
    }

    @Override
    public boolean checkRecordExist(Long userId, Long toUserId) {
        if (userConcernMapper.checkRecordExist(userId,toUserId)==null){
            return false;
        }
        return true;
    }

    @Override
    public PageInfo<Map<String, Object>> getInvitationUserList(Integer startPage, Integer pageSize, Long userId, Long roomId, Integer type, String content) {

        List<Map<String, Object>> list;
        if (type == 1){
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getInvitationUserList(userId,roomId,content);
        }else if (type == 2){
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getInvitationUserList2(userId,roomId,content);
        }else if (type == 3){
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getInvitationUserList3(userId,roomId,content);
        }else{
            list = null;
        }
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }

        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map<String, Object>> getInvitationList(Integer startPage, Integer pageSize, Long userId, Long roomId, Integer tagId, String content, BigDecimal longitude, BigDecimal latitude, String ip) {

        List<Map<String, Object>> list = null;
        if (tagId == 87) {
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getInvitationUserList87( userId,content,roomId);
        }
        if (tagId == 88) {
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getInvitationUserList88( userId,content,roomId);
        }
        if (tagId == 89) {
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getInvitationUserList89( userId,content,roomId);
        }
        if (tagId == 90) {
            if (longitude==null||latitude==null){
                FuntimeUser user = queryUserById(userId);
                longitude = user.getLongitude()==null?null:new BigDecimal(user.getLongitude());
                latitude = user.getLatitude()==null?null:new BigDecimal(user.getLatitude());
                if(longitude==null||latitude==null){
                    list = null;
                }else {
                    PageHelper.startPage(startPage, pageSize);
                    list = userMapper.getInvitationUserList90(userId, content, longitude, latitude, roomId);
                }

            }else {
                updateUserLocation(userId,longitude.toString(),latitude.toString());
                PageHelper.startPage(startPage,pageSize);
                list = userMapper.getInvitationUserList90( userId,content, longitude, latitude,roomId);
            }
        }
        if (tagId == 85){
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getInvitationUserList(userId,roomId,content);
        }
        if (tagId == 86){
            PageHelper.startPage(startPage,pageSize);
            list = userMapper.getInvitationUserList2(userId,roomId,content);
        }

        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }

        return new PageInfo<>(list);
    }

    @Override
    public Map<String, Object> checkVersion(String platform, String appVersion) {

        Map<String, Object> result = new HashMap<>();
        result.put("appUrl",Constant.SHARE_URL);
        //当前版本
        Map<String, Integer> curVer = appVersionMapper.getVersionInfoByVerAndPlatform(platform, appVersion);
        //当前版本信息缺失,直接更新最新版本
        if (curVer==null){
            result.put("state",4);
            result.put("versionInfo",appVersionMapper.getNewVersionInfoByPlatform(platform));
            return result;
        }
        Integer curState = curVer.get("state");
        //最新版本
        if (curState==1){
            result.put("state",1);
            return result;
        }
        //强制更新
        else if (curState==2){
            result.put("state",2);
            result.put("versionInfo",appVersionMapper.getNewVersionInfoByPlatform(platform));
            return result;
        }
        //其他更新,需要检测后续版本有没有强制更新的
        else{
            Integer id = curVer.get("id");
            Integer count = appVersionMapper.checkVersion(id);
            if (count!=null&&count>0){
                //后续有强制更新
                result.put("state",2);
                result.put("versionInfo",appVersionMapper.getNewVersionInfoByPlatform(platform));
                return result;
            }else{
                //后续无强制更新
                result.put("state",curState);
                result.put("versionInfo",appVersionMapper.getNewVersionInfoByPlatform(platform));
                return result;
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void bindPhoneNumber(Long userId, String phoneNumber) {
        FuntimeUser user = userMapper.queryUserInfoByPhone(phoneNumber);
        if (user!=null){
            throw new BusinessException(ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getValue(),ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getDesc());
        }
        user = userMapper.selectByPrimaryKey(userId);
        if(user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        if (user.getPhoneNumber()!=null){
            throw new BusinessException(ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getValue(),ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getDesc());
        }

        int k = userMapper.updatePhoneNumberById(userId,user.getVersion(),System.currentTimeMillis(),phoneNumber);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        userMapper.saveUserInfoChangeLog(userId,"phone_number",phoneNumber);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void bindPhoneNumber(Long userId, String phoneNumber, String code) {
        FuntimeUser user = userMapper.queryUserInfoByPhone(phoneNumber);
        if (user!=null){
            throw new BusinessException(ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getValue(),ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getDesc());
        }
        user = userMapper.selectByPrimaryKey(userId);
        if(user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        if (StringUtils.isNotBlank(user.getPhoneNumber())){
            throw new BusinessException(ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getValue(),ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getDesc());
        }
        String isSend = parameterService.getParameterValueByKey("is_send");
        if (isSend!=null&&isSend.equals("1")) {
            smsService.validateSms(SmsType.BIND_PHONENUMBER.getValue(),phoneNumber,code);
        }

        int k = userMapper.updatePhoneNumberById(userId,user.getVersion(),System.currentTimeMillis(),phoneNumber);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        userMapper.saveUserInfoChangeLog(userId,"phone_number",phoneNumber);

    }

    @Override
    public FuntimeUser getUserInfoByShowId(String showId) {
        return userMapper.getUserInfoByShowId(Long.parseLong(showId));
    }

    @Override
    public Map<String, Object> getUserInfoByShowId2(Long showId, Long userId) {
        return userMapper.getUserInfoByShowId2(showId,userId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public String bindWeixin(Long userId, String code, Integer type) {
        FuntimeUser user = queryUserById(userId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (type == 3){
            if (StringUtils.isBlank(user.getPhoneNumber())){
                throw new BusinessException(ErrorMsgEnum.USER_PHONE_NOT_BIND.getValue(),ErrorMsgEnum.USER_PHONE_NOT_BIND.getDesc());
            }
            unBindWeixin(userId);
            return null;
        }
        JSONObject tokenJson = WeixinLoginUtils.getAccessToken(code);
        String openid = tokenJson.getString("openid");
        String access_token = tokenJson.getString("access_token");
        JSONObject userJson = WeixinLoginUtils.getUserInfo(access_token,openid);
        String nickName ;
        try {
            nickName = new String(userJson.getString("nickname").getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            nickName = Constant.DEFAULT_NICKNAME;
        }
        FuntimeUserThird userThird = queryUserInfoByOpenid(openid,Constant.LOGIN_WX);
        if (type==1){
            if (userThird!=null){
                throw new BusinessException(ErrorMsgEnum.USER_WX_EXISTS.getValue(),ErrorMsgEnum.USER_WX_EXISTS.getDesc());
            }
            saveUserThird(userId,Constant.LOGIN_WX,openid,userJson.getString("unionid"),access_token,nickName);

        }else if (type == 2){
            if (userThird==null){
                throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_WX_NOT_BIND.getValue(),ErrorMsgEnum.WITHDRAWAL_WX_NOT_BIND.getDesc());
            }
            updateUserThird(userThird.getId(),userJson.getString("unionid"),access_token,openid,nickName);

        }else{
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        userMapper.saveUserInfoChangeLog(userId,"openid",Constant.LOGIN_WX+"/"+openid);

        return nickName;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public String bindQQ(Long userId, String accessToken, Integer type) {
        FuntimeUser user = queryUserById(userId);
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (type == 3){
            if (StringUtils.isBlank(user.getPhoneNumber())){
                throw new BusinessException(ErrorMsgEnum.USER_PHONE_NOT_BIND.getValue(),ErrorMsgEnum.USER_PHONE_NOT_BIND.getDesc());
            }
            unBindQQ(userId);
            return null;
        }
        String openid = QqLoginUtils.getOpenId(accessToken);
        JSONObject userJson = QqLoginUtils.getUserInfo(accessToken,openid);
        String nickName = userJson.getString("nickname");
        FuntimeUserThird userThird = queryUserInfoByOpenid(openid,Constant.LOGIN_QQ);
        if (type==1){
            if (userThird!=null){
                throw new BusinessException(ErrorMsgEnum.USER_QQ_EXISTS.getValue(),ErrorMsgEnum.USER_QQ_EXISTS.getDesc());
            }
            saveUserThird(userId,Constant.LOGIN_QQ,openid,userJson.getString("unionid"),accessToken,nickName);

        }else if (type == 2){
            if (userThird==null){
                throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_QQ_NOT_BIND.getValue(),ErrorMsgEnum.WITHDRAWAL_QQ_NOT_BIND.getDesc());
            }
            updateUserThird(userThird.getId(),userJson.getString("unionid"),accessToken,openid,nickName);

        }else{
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        userMapper.saveUserInfoChangeLog(userId,"openid",Constant.LOGIN_QQ+"/"+openid);

        return nickName;
    }

    @Override
    public Map<String, Object> getInstallInfo(Long userId) {
        Map<String,Object> result = new HashMap<>();
        FuntimeUser user = queryUserById(userId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        result.put("realnameAuthenticationFlag",user.getRealnameAuthenticationFlag());
        if (StringUtils.isNotBlank(user.getPhoneNumber())){
            result.put("bindPhone",true);
            result.put("phoneNumber",user.getPhoneNumber());
        }else{
            result.put("bindPhone",false);
        }
        FuntimeUserThird userThird = queryUserThirdIdByType(userId,Constant.LOGIN_WX);
        if (userThird == null){
            result.put("bindWx",false);
        }else{
            result.put("bindWx",true);
            result.put("wxNickname",userThird.getNickname());
        }
        userThird = queryUserThirdIdByType(userId,Constant.LOGIN_QQ);
        if (userThird == null){
            result.put("bindQq",false);
        }else{
            result.put("bindQq",true);
            result.put("qqNickname",userThird.getNickname());
        }
        return result;
    }

    @Override
    public Map<String, Object> getUserBindInfo(Long userId) {
        Map<String,Object> result = new HashMap<>();
        FuntimeUser user = queryUserById(userId);
        if (user == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (StringUtils.isNotBlank(user.getPhoneNumber())){
            result.put("bindPhone",true);
            result.put("phoneNumber",user.getPhoneNumber());
        }else{
            result.put("bindPhone",false);
        }
        FuntimeUserThird userThird = queryUserThirdIdByType(userId,Constant.LOGIN_WX);
        if (userThird == null){
            result.put("bindWx",false);
        }else{
            result.put("bindWx",true);
            result.put("wxNickname",userThird.getNickname());
        }

        return result;
    }

    @Override
    public void heartTask() {
        userMapper.heartTask();
    }

    @Override
    @Transactional
    public void offlineUserAppTask() {
        String val = parameterService.getParameterValueByKey("heart_rate");
        List<Long> users = userMapper.getOfflineUserByApp(Integer.parseInt(val)+5);
        for (Long userId : users){
            if (userMapper.checkUserAllowOffline(userId) == null) {
                log.info("offlineUserAppTask========>updateOnlineState:userId:{}", userId);
                updateOnlineState(userId, 2);
                roomService.roomExitTask(userId);
            }
        }
    }

    @Override
    public void updateImHeartSync(Long userId) {
        userMapper.updateImHeartSync(userId);
    }

    @Override
    public void updateUserCar(Long userId, Integer carId) {
        int k = userMapper.updateUserCar(userId,carId);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public String getDefaultNameBySex(Integer sex) {
        List<String> names = userMapper.getDefaultNameBySex(sex);
        return names.get(RandomUtils.nextInt(0,names.size()-1));
    }


    private void unBindWeixin(Long userId) {
        FuntimeUserThird userThird = queryUserThirdIdByType(userId, Constant.LOGIN_WX);
        if (userThird == null){
            throw new BusinessException(ErrorMsgEnum.USER_WX_NOT_BIND.getValue(),ErrorMsgEnum.USER_WX_NOT_BIND.getDesc());
        }
        int k = userThirdMapper.deleteByPrimaryKey(userThird.getId());
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        userMapper.saveUserInfoChangeLog(userId,"openid","weixin unbind");
    }

    private void unBindQQ(Long userId) {
        FuntimeUserThird userThird = queryUserThirdIdByType(userId, Constant.LOGIN_QQ);
        if (userThird == null){
            throw new BusinessException(ErrorMsgEnum.USER_QQ_NOT_BIND.getValue(),ErrorMsgEnum.USER_QQ_NOT_BIND.getDesc());
        }
        int k = userThirdMapper.deleteByPrimaryKey(userThird.getId());
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        userMapper.saveUserInfoChangeLog(userId,"openid","QQ unbind");
    }

    public void updateUserThird(Long id,String unionId,String token,String openid,String nickname){
        FuntimeUserThird userThird = new FuntimeUserThird();
        userThird.setId(id);
        userThird.setUnionid(unionId);
        userThird.setToken(token);
        userThird.setOpenid(openid);
        userThird.setNickname(nickname);
        if(userThirdMapper.updateByPrimaryKeySelective(userThird)!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }


    public Boolean updateByPrimaryKeySelective(FuntimeUser user){
        if (StringUtils.isNotBlank(user.getNickname())){
            checkSensitive(user.getNickname());
        }
        if (StringUtils.isNotBlank(user.getSignText())){
            checkSensitive(user.getSignText());
        }
        if(userMapper.updateByPrimaryKeySelective(user)!=1){

            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return true;
    }

    public void updateTokenById(Long userId,String token){
        FuntimeUser user = new FuntimeUser();
        user.setId(userId);
        user.setToken(token);
        if(userMapper.updateByPrimaryKeySelective(user)!=1){

            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }
}
