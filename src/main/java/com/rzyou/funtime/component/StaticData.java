package com.rzyou.funtime.component;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 2020/4/2
 * LLP-LX
 */
@Component
public class StaticData {
    @Value("${app.im.imSdkAppId}")
    public  long imSdkAppId ;
    @Value("${app.im.imSdkAppSecret}")
    public  String imSdkAppSecret ;
    @Value("${app.pay.certPath}")
    public String certPath ;
    @Value("${app.pay.appleUrl}")
    public String appleUrl ;
    @Autowired
    LoginStrategy telLogin;
    @Autowired
    LoginStrategy wxLogin;
    @Autowired
    LoginStrategy qqLogin;
    @Autowired
    LoginStrategy onekeyLogin;

    public static Map<String,LoginStrategy> context = new HashMap<>();
    public static long TENCENT_YUN_SDK_APPID ;
    public static String TENCENT_YUN_SDK_APPSECRET ;
    public static String APPLE_URL;
    public static String CERPATH;

    @PostConstruct
    public void init(){
        TENCENT_YUN_SDK_APPID = imSdkAppId;
        TENCENT_YUN_SDK_APPSECRET = imSdkAppSecret;
        APPLE_URL = appleUrl;
        CERPATH = certPath;

        context.put(Constant.LOGIN_TEL,telLogin);
        context.put(Constant.LOGIN_WX,wxLogin);
        context.put(Constant.LOGIN_QQ,qqLogin);
        context.put(Constant.LOGIN_ONEKEY,onekeyLogin);

    }


}
