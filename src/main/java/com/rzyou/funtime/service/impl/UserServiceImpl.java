package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

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
    public void updateUserInfo(Long id, Integer onlineState, String token, String imei, String ip, Long version,String nikename,String loginType,String deviceName){
        FuntimeUser user = new FuntimeUser();
        user.setToken(token);
        user.setPhoneImei(imei);
        user.setOnlineState(onlineState);
        user.setId(id);
        user.setIp(ip);
        user.setVersion(version);
        updateByPrimaryKeySelective(user);

        //足迹
        saveRecode(id,user.getPhoneImei(),user.getIp(),1, nikename, loginType, deviceName);
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
    @Transactional
    public Boolean saveUser(FuntimeUser user) {
        insertSelective(user);

        //足迹
        saveRecode(user.getId(),user.getPhoneImei(),user.getIp(),1, null, user.getLoginType(), user.getDeviceName());

        saveUserAccount(user.getId());

        return true;
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

        user.setVersion(funtimeUser.getVersion());
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

        user.setVersion(funtimeUser.getVersion());
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

        user.setVersion(funtimeUser.getVersion());
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
        int k = accountMapper.updateUserAccountForSub(info.getId(),blackDiamond,blueDiamond,hornNumber,info.getVersion(),System.currentTimeMillis());
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }


    }

    public Boolean updateByPrimaryKeySelective(FuntimeUser user){
        user.setNewVersion(System.currentTimeMillis());
        if(userMapper.updateByPrimaryKeySelective(user)!=1){

            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return true;
    }
}
