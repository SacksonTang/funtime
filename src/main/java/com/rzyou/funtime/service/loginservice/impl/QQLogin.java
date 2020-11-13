package com.rzyou.funtime.service.loginservice.impl;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.common.jwt.util.JwtHelper;
import com.rzyou.funtime.common.qqutils.QqLoginUtils;
import com.rzyou.funtime.common.wxutils.WeixinLoginUtils;
import com.rzyou.funtime.component.RedisUtil;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserAccount;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service("qqLogin")
public class QQLogin implements LoginStrategy {
    @Autowired
    UserService userService;
    @Autowired
    RedisUtil redisUtil;


    @Override
    @Transactional
    public FuntimeUser login(FuntimeUser user) {

        if (StringUtils.isBlank(user.getCode())) {
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(), ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        String accessToken = user.getCode();
        //String accessToken = QqLoginUtils.getAccessToken(code, "");
        //String refresh_token = tokenJson.getString("refresh_token");
        String openid = QqLoginUtils.getOpenId(accessToken);

        FuntimeUserThird userThird = userService.queryUserInfoByOpenid(openid, user.getLoginType());

        if (userThird == null) {
            //JSONObject refreshTokenJson = WeixinLoginUtils.refreshToken(refresh_token);
            //String access_token = refreshTokenJson.getString("access_token");
            JSONObject userJson = QqLoginUtils.getUserInfo(accessToken, openid);
            //新用户
            user.setOnlineState(1);
            user.setState(1);

            String nickName = userJson.getString("nickname");

            user.setNickname(nickName);
            String url = StringUtils.isBlank(userJson.getString("figureurl_qq_2"))?userJson.getString("figureurl_qq_1"):userJson.getString("figureurl_qq_2") ;
            if (url !=null&&url.startsWith("http:")){
                url = url.replace("http:","https:");
            }
            user.setPortraitAddress(url);
            user.setSex("男".equals(userJson.getString("gender"))?1:2);
            if (StringUtils.isBlank(user.getPortraitAddress())) {
                List<String> userImageDefaultUrls = userService.getUserImageDefaultUrls(user.getSex());
                if (userImageDefaultUrls == null || userImageDefaultUrls.isEmpty()) {
                    if (user.getSex() == 1) {
                        user.setPortraitAddress(Constant.COS_URL_PREFIX + Constant.DEFAULT_MALE_HEAD_PORTRAIT);
                    }
                    if (user.getSex() == 2) {
                        user.setPortraitAddress(Constant.COS_URL_PREFIX + Constant.DEFAULT_FEMALE_HEAD_PORTRAIT);
                    }
                } else {
                    user.setPortraitAddress(userImageDefaultUrls.get(RandomUtils.nextInt(0, userImageDefaultUrls.size())));
                }
            }
            user.setVersion(System.currentTimeMillis());
            user.setSignText("这个人很懒,什么都没有留下");
            if (user.getBirthday()==null){
                user.setBirthday(Integer.parseInt(DateUtil.getCurrentYearAdd(new Date(),-18)));
            }

            userService.saveUser(user, Constant.LOGIN_QQ, openid, userJson.getString("unionid"), accessToken);
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            boolean flag = TencentUtil.accountImport(userSig, user.getId().toString(), user.getNickname(), user.getPortraitAddress());
            if (!flag) {
                throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(), ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
            }
            user.setBlueAmount(0);
            user.setNewUser(true);
            user.setLevel(0);
            return user;
        } else {

            FuntimeUser funtimeUser = userService.queryUserById(userThird.getUserId());

            if (funtimeUser.getState() != 1) {
                throw new BusinessException(ErrorMsgEnum.USER_IS_DELETE.getValue(), ErrorMsgEnum.USER_IS_DELETE.getDesc());
            }

            user.setId(funtimeUser.getId());

            user.setOnlineState(1);

            userService.updateUserInfo(user);
            funtimeUser.setToken(user.getToken());
            FuntimeUserAccount userAccount = userService.getUserAccountInfoById(funtimeUser.getId());
            funtimeUser.setBlueAmount(userAccount.getBlueDiamond().intValue());
            funtimeUser.setLevel(userAccount.getLevel());
            funtimeUser.setNewUser(false);

            return funtimeUser;

        }
    }
}
