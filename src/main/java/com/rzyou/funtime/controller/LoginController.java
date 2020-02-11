package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.common.jwt.util.JwtHelper;
import com.rzyou.funtime.service.SmsService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import com.rzyou.funtime.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    UserService userService;
    @Autowired
    SmsService smsService;
    @Autowired
    LoginStrategy telLogin;
    @Autowired
    LoginStrategy wxLogin;
    @Autowired
    LoginStrategy qqLogin;
    @Autowired
    LoginStrategy onekeyLogin;

    private static Map<String,LoginStrategy> context = new HashMap<>();

    @PostConstruct
    public void init(){

        context.put(Constant.LOGIN_TEL,telLogin);
        context.put(Constant.LOGIN_WX,wxLogin);
        context.put(Constant.LOGIN_QQ,qqLogin);
        context.put(Constant.LOGIN_ONEKEY,onekeyLogin);

    }

    /**
     * 心跳
     */
    @PostMapping("heart")
    public ResultMsg<Object> heart(HttpServletRequest request){
        JSONObject paramJson = HttpHelper.getParamterJson(request);
        Long userId = paramJson.getLong("userId");
        userService.saveHeart(userId);
        return new ResultMsg<>();
    }

    /**
     * 检测版本
     * @param request
     * @return
     */
    @PostMapping("checkVersion")
    public ResultMsg<Object> checkVersion(HttpServletRequest request) {
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String platform = paramJson.getString("platform");
            String appVersion = paramJson.getString("appVersion");
            if (StringUtils.isBlank(appVersion)||StringUtils.isBlank(platform)) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            result.setData(userService.checkVersion(platform,appVersion));
            return result;
        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

        return result;
    }



    /**
     * 登录 loginType
     *
     * @param request
     * @return
     */
    @PostMapping("doLogin")
    public ResultMsg<Object> login(HttpServletRequest request) {
        ResultMsg<Object> result = new ResultMsg<>();

        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            FuntimeUser user = JSONObject.toJavaObject(paramJson, FuntimeUser.class);
            if (user == null
                    || StringUtils.isBlank(user.getLoginType())) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            user.setIp(HttpHelper.getClientIpAddr(request));
            user.setLastLoginTime(new Date());
            LoginStrategy strategy = context.get(user.getLoginType());
            if(strategy==null){
                result.setCode(ErrorMsgEnum.USER_LOGINTYPE_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.USER_LOGINTYPE_ERROR.getDesc());
                return result;
            }
            FuntimeUser userInfo = strategy.login(user);

            if (!userService.checkAgreementByuserId(userInfo.getId(),UserAgreementType.PRIVACY_AGREEMENT.getValue())){
                userInfo.setPrivacyAgreementUrl(Constant.COS_URL_PREFIX+Constant.AGREEMENT_PRIVACY);
                userInfo.setUserAgreementUrl(Constant.COS_URL_PREFIX+Constant.AGREEMENT_USER);
            }
            userInfo.setImSdkaAppId(Constant.TENCENT_YUN_SDK_APPID);
            result.setData(JsonUtil.getMap("user",userInfo));
        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

        return result;
    }

    /**
     * 获取token
     * @param request
     * @return
     */
    @PostMapping("getToken")
    public ResultMsg<Object> getToken(HttpServletRequest request) {
        JSONObject paramJson = HttpHelper.getParamterJson(request);
        String userId = paramJson.getString("userId");
        String imei = paramJson.getString("imei");
        if (StringUtils.isBlank(userId)) {
            return new ResultMsg<>(ErrorMsgEnum.USER_ID_NOT_EXIST.getValue(), ErrorMsgEnum.USER_ID_NOT_EXIST.getDesc());
        }
        try {
            String token = JwtHelper.generateJWT(userId,imei);
            return new ResultMsg<>(JsonUtil.getMap("token",token));
        } catch (Exception e) {
            return new ResultMsg<>(ErrorMsgEnum.USER_GETTOKEN_FAIL.getValue(), ErrorMsgEnum.USER_GETTOKEN_FAIL.getDesc());
        }
    }

    /**
     * 发送短信
     * @param request
     * @return
     */
    @PostMapping("sendSms")
    public ResultMsg<Object> sendSms(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String phone = paramJson.getString("phoneNumber");
            String resend = paramJson.getString("resend");
            int smsType = paramJson.getInteger("smsType");
            String ip = HttpHelper.getClientIpAddr(request);

            smsService.sendSms(phone,resend,ip,smsType);
        }catch (BusinessException be){
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }
        return result;

    }


}
