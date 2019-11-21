package com.rzyou.funtime.service.loginservice.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.jwt.util.JwtHelper;
import com.rzyou.funtime.service.SmsService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("telLogin")
public class TelLogin implements LoginStrategy {
    @Autowired
    SmsService smsService;

    @Autowired
    UserService userService;


    @Override
    @Transactional
    public FuntimeUser login(FuntimeUser user) {
        if (StringUtils.isBlank(user.getPhoneNumber())){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        //校验验证码
        //smsService.validateSms(user.getPhoneNumber(),user.getCode());

        String userId;
        FuntimeUser funtimeUser = userService.queryUserInfoByPhone(user.getPhoneNumber());
        if(funtimeUser==null){
            //新用户
            user.setOnlineState(1);
            user.setState(1);
            user.setNickname("大侠");
            user.setPortraitAddress("https://");

            user.setVersion(System.currentTimeMillis());

            userService.saveUser(user, null, null, null);
            userId = user.getId().toString();
            String token = JwtHelper.generateJWT(userId);
            user.setToken(token);
            userService.updateTokenById(user.getId(),token);
        }else{
            userId = funtimeUser.getId().toString();
            if(funtimeUser.getState().intValue()!=1){
                throw new BusinessException(ErrorMsgEnum.USER_IS_DELETE.getValue(),ErrorMsgEnum.USER_IS_DELETE.getDesc());
            }
            String token = JwtHelper.generateJWT(userId);
            userService.updateUserInfo(funtimeUser.getId(),1,token,user.getPhoneImei(),user.getIp(),funtimeUser.getNickname(),user.getLoginType(),user.getDeviceName());

        }
        return userService.queryUserById(Long.parseLong(userId));
    }
}
