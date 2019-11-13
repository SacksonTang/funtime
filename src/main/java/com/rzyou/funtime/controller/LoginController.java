package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.common.sms.SmsUtil;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.jwt.util.JwtHelper;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    UserService userService;
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

        context.put(LoginType.TEL,telLogin);
        context.put(LoginType.WX,wxLogin);
        context.put(LoginType.QQ,qqLogin);
        context.put(LoginType.ONEKEY,onekeyLogin);

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
            if (user == null || StringUtils.isBlank(user.getPhoneNumber())
                    || StringUtils.isBlank(user.getCode()) || StringUtils.isBlank(user.getLoginType())) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            user.setIp(HttpHelper.getClientIpAddr(request));
            LoginStrategy strategy = context.get(user.getLoginType());
            if(strategy==null){
                result.setCode(ErrorMsgEnum.USER_LOGINTYPE_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.USER_LOGINTYPE_ERROR.getDesc());
                return result;
            }
            FuntimeUser userInfo = strategy.login(user);

            result.setData(userInfo);
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
        JSONObject paramJson = HttpHelper.getParamterJson(request);
        String userId = paramJson.getString("userId");
        if (StringUtils.isBlank(userId)) {
            return new ResultMsg<>(ErrorMsgEnum.USER_ID_NOT_EXIST.getValue(), ErrorMsgEnum.USER_ID_NOT_EXIST.getDesc());
        }
        try {
            String token = JwtHelper.generateJWT(userId);
            return new ResultMsg<>(token);
        } catch (Exception e) {
            return new ResultMsg<>(ErrorMsgEnum.USER_GETTOKEN_FAIL.getValue(), ErrorMsgEnum.USER_GETTOKEN_FAIL.getDesc());
        }
    }

    @PostMapping("sendSms")
    public ResultMsg<Object> sendSms(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String phone = paramJson.getString("phoneNumber");
            SmsUtil.sendSms(phone);
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


    public class LoginType {
        private static final String TEL = "TEL";
        private static final String WX = "WX";
        private static final String QQ = "QQ";
        private static final String ONEKEY = "ONEKEY";
        private static final String WEIBO = "WEIBO";


    }
}
