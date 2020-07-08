package com.rzyou.funtime.component;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import org.omg.CORBA.NO_IMPLEMENT;
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
    @Value("${app.im.iosPushCertDevId}")
    public  Integer iosPushCertDevId ;
    @Value("${app.im.iosPushCertProdId}")
    public  Integer iosPushCertProdId ;
    @Value("${app.pay.certPath}")
    public String certPath ;
    @Value("${app.pay.appleUrl}")
    public String appleUrl ;

    @Value("${alipay.merchantCertPath}")
    public String merchantCertPath ;
    @Value("${alipay.alipayCertPath}")
    public String alipayCertPath ;
    @Value("${alipay.alipayRootCertPath}")
    public String alipayRootCertPath ;
    @Value("${alipay.notifyUrl}")
    public String notifyUrl ;

    @Autowired
    LoginStrategy telLogin;
    @Autowired
    LoginStrategy wxLogin;
    @Autowired
    LoginStrategy qqLogin;
    @Autowired
    LoginStrategy onekeyLogin;
    @Autowired
    LoginStrategy appleLogin;

    public static Map<String,LoginStrategy> context = new HashMap<>();
    public static long TENCENT_YUN_SDK_APPID ;
    public static String TENCENT_YUN_SDK_APPSECRET ;
    public static String APPLE_URL;
    public static String CERT_PATH;
    public static Integer IOS_PUSHCERTDEVID;
    public static Integer IOS_PUSHCERTPRODID;

    public static String MERCHANTCERTPATH;
    public static String ALIPAYCERTPATH;
    public static String ALIPAYROOTCERTPATH;
    public static String ALIPAYNOTIFYURL;

    @PostConstruct
    public void init(){
        TENCENT_YUN_SDK_APPID = imSdkAppId;
        TENCENT_YUN_SDK_APPSECRET = imSdkAppSecret;
        APPLE_URL = appleUrl;
        CERT_PATH = certPath;
        IOS_PUSHCERTDEVID = iosPushCertDevId;
        IOS_PUSHCERTPRODID = iosPushCertProdId;

        MERCHANTCERTPATH = merchantCertPath;
        ALIPAYCERTPATH = alipayCertPath;
        ALIPAYROOTCERTPATH = alipayRootCertPath;
        ALIPAYNOTIFYURL = notifyUrl;

        context.put(Constant.LOGIN_TEL,telLogin);
        context.put(Constant.LOGIN_WX,wxLogin);
        context.put(Constant.LOGIN_QQ,qqLogin);
        context.put(Constant.LOGIN_ONEKEY,onekeyLogin);
        context.put(Constant.LOGIN_APPLE,appleLogin);

    }


}
