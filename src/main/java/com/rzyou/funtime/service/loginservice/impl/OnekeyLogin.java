package com.rzyou.funtime.service.loginservice.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.common.sms.linkme.LinkmeUtil;
import com.rzyou.funtime.component.RedisUtil;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.common.jwt.util.JwtHelper;
import com.rzyou.funtime.entity.RedisUser;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import com.rzyou.funtime.utils.DateUtil;
import com.rzyou.funtime.utils.StringUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("onekeyLogin")
public class OnekeyLogin implements LoginStrategy {

    @Autowired
    UserService userService;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public FuntimeUser login(FuntimeUser user) {
        if (StringUtils.isBlank(user.getToken())||user.getPlatform()==null||user.getChannel()==null){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }

        String phoneNumber = LinkmeUtil.getPhone(user.getToken(),user.getChannel(),user.getPlatform(),user.getCode());
        String uuid = StringUtil.createNonceStr();
        String userId ;
        String token;
        boolean isNewUser = true;
        FuntimeUser funtimeUser = userService.queryUserInfoByPhone(phoneNumber);
        if(funtimeUser==null){
            //新用户
            user.setOnlineState(1);
            user.setState(1);
            if (user.getSex()==null){
                user.setSex(1);
            }
            if (user.getBirthday()==null){
                user.setBirthday(Integer.parseInt(DateUtil.getCurrentYearAdd(new Date(),-18)));
            }
            user.setNickname(Constant.DEFAULT_NICKNAME);

            user.setSignText("这个人很懒,什么都没有留下");
            user.setVersion(System.currentTimeMillis());
            user.setPhoneNumber(phoneNumber);
            user.setToken(uuid);
            userService.saveUser(user, null, null, null,null);
            userId = user.getId().toString();

            token = JwtHelper.generateJWT(userId,uuid);
            userService.updateShowIdById(user.getId());

            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            boolean flag = TencentUtil.accountImport(userSig,user.getId().toString(),user.getNickname(),user.getPortraitAddress());
            if (!flag){
                throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(),ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
            }

        }else{
            isNewUser = false;
            userId = funtimeUser.getId().toString();
            if(funtimeUser.getState().intValue()!=1){
                throw new BusinessException(ErrorMsgEnum.USER_IS_DELETE.getValue(),ErrorMsgEnum.USER_IS_DELETE.getDesc());
            }
            token = JwtHelper.generateJWT(userId,uuid);
            userService.updateUserInfo(funtimeUser.getId(),1,uuid,user.getPhoneImei(),user.getIp(),funtimeUser.getNickname(),user.getLoginType(),user.getDeviceName());

        }
        FuntimeUser info = userService.getUserBasicInfoById(Long.parseLong(userId));
        info.setBlueAmount(userService.getUserAccountInfoById(Long.parseLong(userId)).getBlueDiamond().intValue());
        info.setNewUser(isNewUser);
        info.setToken(token);
        RedisUser redisUser = new RedisUser();
        redisUser.onlineState = 1;
        redisUser.uuid = uuid;
        redisUtil.set(Constant.REDISUSER_PREFIX+info.getId(),redisUser);
        return info;
    }
}
