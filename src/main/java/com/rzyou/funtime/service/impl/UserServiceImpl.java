package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.cos.CosUtil;
import com.rzyou.funtime.common.im.BankCardVerificationUtil;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.RoomService;
import com.rzyou.funtime.service.SmsService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.DateUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    SmsService smsService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    RoomService roomService;
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
    FuntimeUserAgreementMapper userAgreementMapper;
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


    @Override
    public List<String> getAllUserId() {
        return userMapper.getAllUserId();
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
    public void updateUserInfo(Long id, Integer onlineState, String token, String imei, String ip,String nikename,String loginType,String deviceName){
        FuntimeUser user = new FuntimeUser();
        user.setToken(token);
        user.setPhoneImei(imei);
        user.setOnlineState(onlineState);
        user.setId(id);
        user.setIp(ip);
        updateByPrimaryKeySelective(user);

        //足迹
        saveRecode(id,user.getPhoneImei(),user.getIp(),1, nikename, loginType, deviceName);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void updateUserInfo(FuntimeUser user){

        updateByPrimaryKeySelective(user);

        //足迹
        saveRecode(user.getId(),user.getPhoneImei(),user.getIp(),1, user.getNickname(), user.getLoginType(), user.getDeviceName());
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
                user.setSexColor("#0093FF");
            }else {
                user.setSexColor("#FF0096");
            }
        }
        if (user.getHeight()!=null){
            user.setHeightColor("#FF9500");
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

        user.setBlueAmount(accountMapper.selectByUserId(id).getBlueDiamond().intValue());

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
    @Transactional(rollbackFor = Throwable.class)
    public Boolean saveUser(FuntimeUser user, String openType, String openid, String unionid,String accessToken) {
        insertSelective(user);

        //足迹
        saveRecode(user.getId(),user.getPhoneImei(),user.getIp(),1, null, user.getLoginType(), user.getDeviceName());

        saveUserAccount(user.getId());

        if (openType!=null) {

            saveUserThird(user.getId(), openType, openid, unionid, accessToken);
        }

        return true;
    }

    private void saveUserThird(Long userId, String openType, String openid, String unionid, String accessToken){
        FuntimeUserThird userThird = new FuntimeUserThird();
        userThird.setUserId(userId);
        userThird.setThirdType(openType);
        userThird.setOpenid(openid);
        userThird.setUnionid(unionid);
        userThird.setToken(accessToken);
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

        updateByPrimaryKeySelective(user);
        if (StringUtils.isNotBlank(user.getNickname())||StringUtils.isNotBlank(user.getPortraitAddress())||user.getSex()!=null){
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            boolean flag = TencentUtil.portraitSet(userSig, user.getId().toString(), user.getNickname(), user.getPortraitAddress(),user.getSex()==null?null:user.getSex().toString());
            if (!flag){
                throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(),ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
            }
        }

        return true;
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
    public List<Map<String,Object>> queryTagsByType(String tagType, Integer type) {
        List<Map<String,Object>> list = tagMapper.queryTagsByType(tagType);
        List<Map<String,Object>> results = new ArrayList<>();
        if (type!=null&&type == 1){
            if (list!=null&&!list.isEmpty()){
                Map<String,Object> map = new HashMap<>();
                map.put("id",0);
                map.put("tagName","全部");
                results.add(map);
                map = new HashMap<>();
                map.put("id",-1);
                map.put("tagName","热门");
                results.add(map);
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
    public void updateUserAccountForSub(Long userId,BigDecimal blackDiamond, BigDecimal blueDiamond, Integer hornNumber) {
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

        Long smsId = smsService.validateSms(SmsType.UPDATE_PHONENUMBER.getValue(),newPhoneNumber,code);

        int k = userMapper.updatePhoneNumberById(userId,user.getVersion(),System.currentTimeMillis(),newPhoneNumber);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        smsService.updateSmsInfoById(smsId,1);

    }

    @Override
    public void saveUserValid(Long userId, String fullname, String identityCard, String depositCard, String alipayNo, String wxNo, String code) {
        FuntimeUser user = queryUserById(userId);
        if(user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeUserValid userValid = queryValidInfoByUserId(userId);
        if(userValid!=null){
            throw new BusinessException(ErrorMsgEnum.USERVALID_IS_EXISTS.getValue(),ErrorMsgEnum.USERVALID_IS_EXISTS.getDesc());
        }

        smsService.validateSms(SmsType.REAL_VALID.getValue(),user.getPhoneNumber(),code);

        BankCardVerificationUtil.bankCardVerification(depositCard,fullname,identityCard);


        userValid = new FuntimeUserValid();
        userValid.setAlipayNo(alipayNo);
        userValid.setDepositCard(depositCard);
        userValid.setFullname(fullname);
        userValid.setIdentityCard(identityCard);
        userValid.setWxNo(wxNo);
        userValid.setUserId(userId);
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
    public FuntimeUserValid queryValidInfoByUserId(Long userId) {

        FuntimeUserValid userValid = userValidMapper.selectByUserId(userId);

        return userValid;
    }

    @Override
    public void saveUserAgreement(Long userId, Integer agreementType) {

        FuntimeUserAgreement userAgreement = userAgreementMapper.selectByUserId(userId,agreementType);
        if(userAgreement!=null){
            throw new BusinessException(ErrorMsgEnum.USERAGREEMENT_IS_EXISTS.getValue(),ErrorMsgEnum.USERAGREEMENT_IS_EXISTS.getDesc());
        }
        userAgreement = new FuntimeUserAgreement();
        userAgreement.setType(agreementType);
        userAgreement.setUserId(userId);
        userAgreement.setAgreement(1);
        userAgreement.setCreateTime(new Date());
        int k = userAgreementMapper.insertSelective(userAgreement);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public boolean checkAgreementByuserId(Long userId,Integer agreementType){
        FuntimeUserAgreement userAgreement = userAgreementMapper.selectByUserId(userId,agreementType);
        if (userAgreement==null){
            return false;
        }else{
            return true;
        }
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
    }

    @Override
    public void updateOnlineState(Long userId, Integer onlineState) {
        if(userMapper.updateOnlineState(userId, onlineState)!=1){

            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void updateTokenById(Long userId, String token) {
        userMapper.updateTokenById(userId, token);
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
    public PageInfo<FuntimeUser> queryUserInfoByOnline(Integer startPage, Integer pageSize, Integer sex, Integer ageType) {
        PageHelper.startPage(startPage,pageSize);
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
        List<FuntimeUser> list = userMapper.queryUserInfoByOnline(sex,startAge,endAge);
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
                        user.setSexColor("#0093FF");
                    }else {
                        user.setSexColor("#FF0096");
                    }
                }
                if (user.getHeight()!=null){
                    user.setHeightColor("#FF9500");
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
    public PageInfo<Map<String, Object>> getConcernUserList(Integer startPage, Integer pageSize, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String, Object>> list = userMapper.getConcernUserList(userId);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }
        for (Map<String, Object> map:list){

            if (map.get("birthday")!=null) {

                Integer birthday = Integer.valueOf(map.get("birthday").toString());
                map.put("age",DateUtil.getAgeByBirthday(birthday));
                map.put("constellation",DateUtil.getConstellationByBirthday(birthday));
            }
        }
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map<String, Object>> getFansList(Integer startPage, Integer pageSize, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<Map<String, Object>> list = userMapper.getFansList(userId);
        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }
        for (Map<String, Object> map:list){

            if (map.get("birthday")!=null) {
                Integer birthday = Integer.valueOf(map.get("birthday").toString());
                map.put("age",DateUtil.getAgeByBirthday(birthday));
                map.put("constellation",DateUtil.getConstellationByBirthday(birthday));
            }
        }
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<Map<String, Object>> getRankingList(Integer startPage, Integer pageSize, Integer dateType,Integer type) {
        PageHelper.startPage(startPage,pageSize);
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
        }else{
            return new PageInfo<>();
        }
        List<Map<String, Object>> list;
        if (type == 1){
            list = userMapper.getCharmList(startDate,endDate);
        }else{
            list = userMapper.getContributionList(startDate,endDate);
        }

        if (list==null||list.isEmpty()){
            return new PageInfo<>();
        }



        return new PageInfo<>(list);
    }

    @Override
    @Transactional
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
            accusation.setImg1(CosUtil.generatePresignedUrl(accusation.getImg1()));
        }
        if (accusation.getImg2()!=null){
            accusation.setImg2(CosUtil.generatePresignedUrl(accusation.getImg2()));
        }
        if (accusation.getImg3()!=null){
            accusation.setImg3(CosUtil.generatePresignedUrl(accusation.getImg3()));
        }
        if (accusation.getImg4()!=null){
            accusation.setImg4(CosUtil.generatePresignedUrl(accusation.getImg4()));
        }
        if (accusation.getImg5()!=null){
            accusation.setImg5(CosUtil.generatePresignedUrl(accusation.getImg5()));
        }
        if (accusationMapper.insertSelective(accusation)!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void saveHeart(Long userId) {
        userMapper.saveHeart(userId);
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
    public PageInfo<FuntimeUser> queryUserInfoByIndex(Integer startPage, Integer pageSize, String content) {
        PageHelper.startPage(startPage,pageSize);
        List<FuntimeUser> list = userMapper.queryUserInfoByIndex(content);
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
                        user.setSexColor("#0093FF");
                    }else {
                        user.setSexColor("#FF0096");
                    }
                }
                if (user.getHeight()!=null){
                    user.setHeightColor("#FF9500");
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
    public Map<String, Object> getWithdralInfo(Long userId) {
        Map<String,Object> result = new HashMap<>();
        FuntimeUserValid userValid = queryValidInfoByUserId(userId);
        result.put("userValid",userValid);
        List<FuntimeWithdrawalConf> withdralConf = withdrawalConfMapper.getWithdralConf();
        result.put("withdralConf",withdralConf);
        String black_to_rmb = parameterService.getParameterValueByKey("black_to_rmb");
        result.put("black_to_rmb",black_to_rmb);
        result.put("agreementUrl",Constant.COS_URL_PREFIX+Constant.AGREEMENT_WITHDRAL);

        return result;
    }

    @Override
    public void logout(Long userId) {
        if (userMapper.checkUserExists(userId)==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        updateOnlineState(userId,2);
        FuntimeChatroom room = roomService.getRoomByUserId(userId);
        if (room == null){
            return;
        }
        roomService.roomExit(userId,room.getId());

    }

    @Override
    public void validPhone(Long userId, String code, String oldPhoneNumber) {
        if (!checkUserExists(userId)){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        Long smsId = smsService.validateSms(SmsType.UPDATE_PHONENUMBER.getValue(),oldPhoneNumber,code);
        smsService.updateSmsInfoById(smsId,1);
    }


    public Boolean updateByPrimaryKeySelective(FuntimeUser user){
        if(userMapper.updateByPrimaryKeySelective(user)!=1){

            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return true;
    }
}
