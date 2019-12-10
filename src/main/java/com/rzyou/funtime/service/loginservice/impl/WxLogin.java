package com.rzyou.funtime.service.loginservice.impl;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.common.wxutils.WeixinLoginUtils;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserThird;
import com.rzyou.funtime.common.jwt.util.JwtHelper;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service("wxLogin")
public class WxLogin implements LoginStrategy {

    @Autowired
    UserService userService;



    @Override
    @Transactional
    public FuntimeUser login(FuntimeUser user) {

        if (StringUtils.isBlank(user.getCode())){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        String code = user.getCode();
        JSONObject tokenJson = WeixinLoginUtils.getAccessToken(code);
        String refresh_token = tokenJson.getString("refresh_token");
        String openid = tokenJson.getString("openid");

        FuntimeUserThird userThird = userService.queryUserInfoByOpenid(openid,user.getLoginType());

        String userId;
        if (userThird==null){
            JSONObject refreshTokenJson = WeixinLoginUtils.refreshToken(refresh_token);
            String access_token = refreshTokenJson.getString("access_token");
            JSONObject userJson = WeixinLoginUtils.getUserInfo(access_token,openid);
            //新用户
            user.setOnlineState(1);
            user.setState(1);
            String nickName = null;
            try {
                nickName = new String(userJson.getString("nickname").getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("wx nick encode error : {}",userJson);
            }
            user.setNickname(nickName);
            user.setPortraitAddress(userJson.getString("headimgurl"));
            user.setSex(userJson.getInteger("sex"));
            user.setVersion(System.currentTimeMillis());
            user.setSignText("这个人很懒,什么都没有留下");
            userService.saveUser(user,"WX",openid,userJson.getString("unionid"));
            userId = user.getId().toString();
            String token = JwtHelper.generateJWT(userId,user.getPhoneImei());
            user.setToken(token);
            userService.updateTokenById(user.getId(),token);
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            boolean flag = TencentUtil.accountImport(userSig,user.getId().toString(),user.getNickname(),user.getPortraitAddress());
            if (!flag){
                throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(),ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
            }
            return user;
        }else{
            userId = userThird.getUserId().toString();

            FuntimeUser funtimeUser = userService.queryUserById(userThird.getUserId());

            if(funtimeUser.getState()!=1){
                throw new BusinessException(ErrorMsgEnum.USER_IS_DELETE.getValue(),ErrorMsgEnum.USER_IS_DELETE.getDesc());
            }
            String token = JwtHelper.generateJWT(userId,user.getPhoneImei());
            user.setId(funtimeUser.getId());
            user.setToken(token);
            user.setOnlineState(1);

            userService.updateUserInfo(user);
            funtimeUser.setToken(token);
            return funtimeUser;

        }





    }



}
