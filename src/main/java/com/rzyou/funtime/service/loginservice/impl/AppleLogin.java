package com.rzyou.funtime.service.loginservice.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.appleutils.JwtUtils;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.common.jwt.util.JwtHelper;
import com.rzyou.funtime.component.RedisUtil;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserThird;
import com.rzyou.funtime.entity.RedisUser;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import com.rzyou.funtime.utils.DateUtil;
import com.rzyou.funtime.utils.StringUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service("appleLogin")
public class AppleLogin implements LoginStrategy {
    @Autowired
    UserService userService;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public FuntimeUser login(FuntimeUser user) {

        if(!JwtUtils.isValid(user.getToken())){
            throw new BusinessException(ErrorMsgEnum.USER_APPLELOGIN_ERROR.getValue(),ErrorMsgEnum.USER_APPLELOGIN_ERROR.getDesc());
        }
        FuntimeUserThird userThird = userService.queryUserInfoByOpenid(user.getAppleUserId(),user.getLoginType());
        String uuid = StringUtil.createNonceStr();
        String userId;
        if (userThird==null){
            //新用户
            user.setOnlineState(1);
            user.setState(1);
            if (StringUtils.isBlank(user.getFullname())){
                user.setNickname(userService.getDefaultNameBySex(1));
            }else {
                user.setNickname(user.getFullname());
            }
            if (user.getSex()==null){
                user.setSex(1);
            }
            List<String> userImageDefaultUrls = userService.getUserImageDefaultUrls(user.getSex());
            if (userImageDefaultUrls==null||userImageDefaultUrls.isEmpty()) {
                if (user.getSex() == 1) {
                    user.setPortraitAddress(Constant.COS_URL_PREFIX + Constant.DEFAULT_MALE_HEAD_PORTRAIT);
                }
                if (user.getSex() == 2) {
                    user.setPortraitAddress(Constant.COS_URL_PREFIX + Constant.DEFAULT_FEMALE_HEAD_PORTRAIT);
                }
            }else{
                user.setPortraitAddress(userImageDefaultUrls.get(RandomUtils.nextInt(0, userImageDefaultUrls.size())));
            }
            if (user.getBirthday()==null){
                user.setBirthday(Integer.parseInt(DateUtil.getCurrentYearAdd(new Date(),-18)));
            }
            user.setSignText("这个人很懒,什么都没有留下");
            user.setVersion(System.currentTimeMillis());
            user.setToken(uuid);
            userService.saveUser(user,Constant.LOGIN_APPLE,user.getAppleUserId(),null,null);
            //userService.updateShowIdById(user.getId());
            userId = user.getId().toString();
            String token = JwtHelper.generateJWT(userId,uuid);
            user.setToken(token);
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            boolean flag = TencentUtil.accountImport(userSig,user.getId().toString(),user.getNickname(),user.getPortraitAddress());
            if (!flag){
                throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(),ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
            }
            user.setBlueAmount(0);
            user.setNewUser(true);
            RedisUser redisUser = new RedisUser();
            redisUser.onlineState = 1;
            redisUser.uuid = uuid;
            redisUtil.set(Constant.REDISUSER_PREFIX+user.getId(),redisUser);
            return user;
        }else{
            userId = userThird.getUserId().toString();

            FuntimeUser funtimeUser = userService.queryUserById(userThird.getUserId());

            if(funtimeUser.getState()!=1){
                throw new BusinessException(ErrorMsgEnum.USER_IS_DELETE.getValue(),ErrorMsgEnum.USER_IS_DELETE.getDesc());
            }
            String token = JwtHelper.generateJWT(userId,uuid);
            user.setId(funtimeUser.getId());
            user.setToken(uuid);
            user.setOnlineState(1);

            userService.updateUserInfo(user);
            funtimeUser.setToken(token);
            funtimeUser.setBlueAmount(userService.getUserAccountInfoById(funtimeUser.getId()).getBlueDiamond().intValue());
            funtimeUser.setNewUser(false);
            RedisUser redisUser = new RedisUser();
            redisUser.onlineState = 1;
            redisUser.uuid = uuid;
            redisUtil.set(Constant.REDISUSER_PREFIX+funtimeUser.getId(),redisUser);
            return funtimeUser;
        }

    }



}
