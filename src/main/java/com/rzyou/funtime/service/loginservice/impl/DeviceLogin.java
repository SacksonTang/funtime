package com.rzyou.funtime.service.loginservice.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.common.jwt.util.JwtHelper;
import com.rzyou.funtime.component.RedisUtil;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.RedisUser;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.SmsService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service("deviceLogin")
public class DeviceLogin implements LoginStrategy {
    @Autowired
    SmsService smsService;

    @Autowired
    UserService userService;

    @Autowired
    ParameterService parameterService;
    @Autowired
    RedisUtil redisUtil;

    @Override
    @Transactional
    public FuntimeUser login(FuntimeUser user) {
        if (StringUtils.isBlank(user.getPhoneImei())){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        FuntimeUser funtimeUser = userService.queryUserInfoByPhoneImei(user.getPhoneImei());
        boolean isNewUser = false;
        Long userId;
        if(funtimeUser==null){

            //新用户
            user.setOnlineState(1);
            user.setState(1);
            user.setNickname(userService.getDefaultNameBySex(1));

            user.setSignText("这个人很懒,什么都没有留下");

            if (user.getBirthday()==null){
                user.setBirthday(Integer.parseInt(DateUtil.getCurrentYearAdd(new Date(),-18)));
            }
            if (user.getSex()==null){
                user.setSex(RandomUtils.nextInt(1,3));
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
            user.setVersion(System.currentTimeMillis());
            userService.saveUser(user, null, null, null,null);
            userId = user.getId();
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            boolean flag = TencentUtil.accountImport(userSig,user.getId().toString(),user.getNickname(),user.getPortraitAddress());
            if (!flag){
                throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(),ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
            }
            isNewUser = true;
        }else{
            if(funtimeUser.getState()!=1){
                throw new BusinessException(ErrorMsgEnum.USER_IS_DELETE.getValue(),ErrorMsgEnum.USER_IS_DELETE.getDesc());
            }
            userService.updateUserInfo(funtimeUser.getId(),1,user.getPhoneImei(),user.getIp(),funtimeUser.getNickname(),user.getLoginType(),user.getDeviceName());
            userId = funtimeUser.getId();
        }
        FuntimeUser info = userService.getUserBasicInfoById(userId);
        info.setBlueAmount(userService.getUserAccountInfoById(userId).getBlueDiamond().intValue());
        info.setNewUser(isNewUser);
        return info;
    }
}
