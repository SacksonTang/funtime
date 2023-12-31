package com.rzyou.funtime.service.loginservice.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.SmsType;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.component.RedisUtil;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserAccount;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.SmsService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import com.rzyou.funtime.utils.DateUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service("dituiLogin")
public class DituiLogin implements LoginStrategy {
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
        if (StringUtils.isBlank(user.getPhoneNumber())){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        String isSend = parameterService.getParameterValueByKey("is_send");
        if (isSend != null && isSend.equals("1")) {
            //校验验证码
            smsService.validateSms(SmsType.REGISTER_LOGIN.getValue(), user.getPhoneNumber(), user.getCode());
        }

        FuntimeUser funtimeUser = userService.queryUserInfoByPhone(user.getPhoneNumber());
        if(funtimeUser==null){

            //新用户
            user.setOnlineState(1);
            user.setState(1);
            user.setNickname(userService.getDefaultNameBySex(1));
            user.setPlatform(0);
            user.setSignText("这个人很懒,什么都没有留下");

            if (user.getBirthday()==null){
                user.setBirthday(Integer.parseInt(DateUtil.getCurrentYearAdd(new Date(),-18)));
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
            user.setVersion(System.currentTimeMillis());

            userService.saveUser(user, null, null, null,null);
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            boolean flag = TencentUtil.accountImport(userSig,user.getId().toString(),user.getNickname(),user.getPortraitAddress());
            if (!flag){
                throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(),ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
            }
        }else{
            throw new BusinessException(ErrorMsgEnum.USER_CHANNEL_ERROR.getValue(),ErrorMsgEnum.USER_CHANNEL_ERROR.getDesc());

        }


        return new FuntimeUser();
    }
}
