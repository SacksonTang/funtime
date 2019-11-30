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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
     * 登录 loginType
     *
     * @param request
     * @return
     */
    @PostMapping("doLogin")
    public ResultMsg<Object> login(HttpServletRequest request) {
        ResultMsg<Object> result = new ResultMsg<>();

        try {
            JSONObject paramJson = HttpHelper.getParamterJsonNoToken(request);

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

    @PostMapping("getToken")
    public ResultMsg<Object> getToken(HttpServletRequest request) {
        JSONObject paramJson = HttpHelper.getParamterJsonNoToken(request);
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

    @PostMapping("sendSms")
    public ResultMsg<Object> sendSms(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJsonNoToken(request);
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
