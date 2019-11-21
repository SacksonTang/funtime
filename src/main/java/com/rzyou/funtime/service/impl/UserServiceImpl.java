package com.rzyou.funtime.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.SmsType;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.SmsService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.DateUtil;
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
    @Transactional
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
    @Transactional
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

        List<Integer> tags = queryTagsByUserId(id);
        user.setTags(tags);
        return user;
    }

    @Override
    public FuntimeUserThird queryUserInfoByOpenid(String openid) {
        return userThirdMapper.queryUserByOpenid(openid);
    }

    @Override
    @Transactional
    public Boolean saveUser(FuntimeUser user, String openType, String openid, String unionid) {
        insertSelective(user);

        //足迹
        saveRecode(user.getId(),user.getPhoneImei(),user.getIp(),1, null, user.getLoginType(), user.getDeviceName());

        saveUserAccount(user.getId());

        saveUserThird(user.getId(),openType,openid,unionid);

        return true;
    }

    private void saveUserThird(Long userId, String openType, String openid, String unionid){
        FuntimeUserThird userThird = new FuntimeUserThird();
        userThird.setUserId(userId);
        userThird.setThirdType(openType);
        userThird.setOpenid(openid);
        userThird.setUnionid(unionid);
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
        if(userMapper.insertSelective(user)!=1){

            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean updateUserBasicInfoById(FuntimeUser user) {
        FuntimeUser funtimeUser = userMapper.selectByPrimaryKey(user.getId());
        if(funtimeUser==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        List<Integer> tags = new ArrayList<>();
        if (user.getTags()!=null){
            tags.addAll(user.getTags());
            updateTagsByUserId(tags,user.getId());
        }

        return updateByPrimaryKeySelective(user);
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
    @Transactional
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
    @Transactional
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
    public List<FuntimeTag> queryTagsByType(String tagType) {
        return tagMapper.queryTagsByType(tagType);
    }

    @Override
    public FuntimeUserAccount getUserAccountInfoById(Long userId) {
        return accountMapper.selectByUserId(userId);
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
    @Transactional
    public void updatePhoneNumber(Long userId, String newPhoneNumber, String code) {

        FuntimeUser user = userMapper.queryUserInfoByPhone(newPhoneNumber);
        if (user!=null){
            throw new BusinessException(ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getValue(),ErrorMsgEnum.PHONE_NUMBER_IS_REGISTER.getDesc());
        }
        user = userMapper.selectByPrimaryKey(userId);
        if(user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        Long smsId = smsService.validateSms(SmsType.UPDATE_PHONENUMBER.getValue(),newPhoneNumber,code);

        int k = userMapper.updatePhoneNumberById(userId,user.getVersion(),System.currentTimeMillis(),newPhoneNumber);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        smsService.updateSmsInfoById(smsId,1);

    }

    @Override
    public void saveUserValid(Long userId, String fullname, String identityCard, String depositCard, String alipayNo, String wxNo) {
        FuntimeUserValid userValid = queryValidInfoByUserId(userId);
        if(userValid!=null){
            throw new BusinessException(ErrorMsgEnum.USERVALID_IS_EXISTS.getValue(),ErrorMsgEnum.USERVALID_IS_EXISTS.getDesc());
        }
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

    public void checkAgreementByuserId(Long userId,Integer agreementType){
        FuntimeUserAgreement userAgreement = userAgreementMapper.selectByUserId(userId,agreementType);
        if (userAgreement==null){
            throw new BusinessException(ErrorMsgEnum.USERAGREEMENT_IS_NOT_EXISTS.getValue(),ErrorMsgEnum.USERAGREEMENT_IS_NOT_EXISTS.getDesc());
        }
    }

    @Override
    @Transactional
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
    @Transactional
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
    public PageInfo<FuntimeUser> queryUserInfoByOnline(Integer startPage, Integer pageSize, Integer sex, Integer ageType) {
        PageHelper.startPage(startPage,pageSize);
        String startAge = null;
        String endAge = null;
        if (ageType.intValue()==1){//小于23
            endAge = DateUtil.getCurrentYearAdd(new Date(),-23);
        }else if (ageType.intValue()==2){
            startAge = DateUtil.getCurrentYearAdd(new Date(),-24);
            endAge = DateUtil.getCurrentYearAdd(new Date(),-29);
        }else if (ageType.intValue()==3){
            startAge = DateUtil.getCurrentYearAdd(new Date(),-30);
            endAge = DateUtil.getCurrentYearAdd(new Date(),-39);
        }else if (ageType.intValue()==4){
            startAge = DateUtil.getCurrentYearAdd(new Date(),-40);
            endAge = DateUtil.getCurrentYearAdd(new Date(),-49);
        }else if (ageType.intValue()==5){
            startAge = DateUtil.getCurrentYearAdd(new Date(),-50);
            endAge = DateUtil.getCurrentYearAdd(new Date(),-59);
        }else if (ageType.intValue()==6){
            startAge = DateUtil.getCurrentYearAdd(new Date(),-60);
        }else{
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        List<FuntimeUser> list = userMapper.queryUserInfoByOnline(sex,startAge,endAge);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            return new PageInfo<>(list);
        }
    }


    public Boolean updateByPrimaryKeySelective(FuntimeUser user){
        if(userMapper.updateByPrimaryKeySelective(user)!=1){

            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return true;
    }
}
