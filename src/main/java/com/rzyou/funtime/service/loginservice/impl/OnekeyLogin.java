package com.rzyou.funtime.service.loginservice.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.common.jwt.util.JwtHelper;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("onekeyLogin")
public class OnekeyLogin implements LoginStrategy {

    @Autowired
    UserService userService;

    @Override
    public FuntimeUser login(FuntimeUser user) {
        String userId ;
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
            String token = JwtHelper.generateJWT(userId,user.getPhoneImei());
            user.setToken(token);
            userService.updateTokenById(user.getId(),token);

        }else{
            userId = funtimeUser.getId().toString();
            if(funtimeUser.getState().intValue()!=1){
                throw new BusinessException(ErrorMsgEnum.USER_IS_DELETE.getValue(),ErrorMsgEnum.USER_IS_DELETE.getDesc());
            }
            String token = JwtHelper.generateJWT(userId,user.getPhoneImei());
            userService.updateUserInfo(funtimeUser.getId(),1,token,user.getPhoneImei(),user.getIp(),funtimeUser.getNickname(),user.getLoginType(),user.getDeviceName());

        }
        return userService.getUserBasicInfoById(Long.parseLong(userId));
    }
}
