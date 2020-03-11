package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.encryption.RsaUtils;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.common.wxutils.WeixinLoginUtils;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.common.jwt.util.JwtHelper;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.SmsService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import com.rzyou.funtime.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    UserService userService;
    @Autowired
    SmsService smsService;
    @Autowired
    AccountService accountService;
    @Autowired
    ParameterService parameterService;
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
    //@PostMapping("heart")
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
            JSONObject paramJson = HttpHelper.getParamterJsonDecrypt(request);

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
            userInfo.setImSdkAppId(Constant.TENCENT_YUN_SDK_APPID);
            Map<String, Object> map = JsonUtil.getMap("user", userInfo);
            //String encrypt = AESUtil.aesDecrypt(JSONObject.toJSONString(map),Constant.AES_KEY);
            result.setData(map);
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

    /**
     * 根据showId获取用户信息
     * @return
     */
    @GetMapping("getUserInfoByShowId")
    public ResultMsg<Object> getUserInfoByShowId(String showId){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            if (StringUtils.isBlank(showId)) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            FuntimeUser user = userService.getUserInfoByShowId(showId);
            if (user==null){
                result.setCode(ErrorMsgEnum.USER_NOT_EXISTS.getValue());
                result.setMsg(ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
                return result;
            }
            result.setData(JsonUtil.getMap("user",user));
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

    /**
     * 获取充值配置
     * @param request
     * @return
     */
    @GetMapping("getRechargeConf")
    public ResultMsg<Object> getRechargeConf(HttpServletRequest request) {
        ResultMsg<Object> result = new ResultMsg<>();
        try {

            Map<String, Object> conf = JsonUtil.getMap("conf", accountService.getRechargeConf(1));
            conf.put("rechargeAgreementUrl", Constant.COS_URL_PREFIX + Constant.AGREEMENT_RECHARGE);

            result.setData(conf);

            return result;
        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 获取RSA公钥
     * @param request
     * @return
     */
    @GetMapping("getServerPublicKey")
    public ResultMsg<Object> getServerPublicKey(HttpServletRequest request) {
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            result.setData(JsonUtil.getMap("serverPublicKey",Constant.SERVER_PUBLIC_KEY));
            return result;
        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 获取AES秘钥
     * @return
     */
    @PostMapping("getAesKey")
    public ResultMsg<Object> getAesKey(HttpServletRequest request) {
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String clientPublicKey = paramJson.getString("clientPublicKey");
            if (StringUtils.isBlank(clientPublicKey)){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            log.info("clientPublicKey ==>  "+clientPublicKey);
            clientPublicKey = RsaUtils.decryptHexData(clientPublicKey,Constant.SERVER_PRIVATE_KEY);
            result.setData(JsonUtil.getMap("encryptAesKey", RsaUtils.encryptHexData(Constant.AES_KEY,clientPublicKey)));
            return result;
        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 获取openid
     * @return
     */
    @GetMapping("getOpenid")
    public ResultMsg<Object> getOpenid(String code) {
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            if (StringUtils.isBlank(code)){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            JSONObject tokenForPub = WeixinLoginUtils.getAccessTokenForPub(code);
            String openid = tokenForPub.getString("openid");
            Map<String, Object> data = new HashMap<>();
            data.put("openid", openid);
            result.setData(data);
            return result;
        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 充值生成待支付记录
     */
    @PostMapping("startRecharge")
    public ResultMsg<Object> startRecharge(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            FuntimeUserAccountRechargeRecord record = JSONObject.toJavaObject(paramJson, FuntimeUserAccountRechargeRecord.class);
            Integer id = userService.queryTagsByTypeAndName("recharge_channel","WX");

            if (record==null||record.getUserId()==null||id == null||record.getPayType()==null||StringUtils.isBlank(record.getOpenid())) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            String ip = HttpHelper.getClientIpAddr(request);
            record.setRechargeChannelId(id);
            result.setData(accountService.createRecharge(record,ip,"JSAPI"));

            return result;
        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    @PostMapping("getGlobalConfig")
    public ResultMsg<Object> getGlobalConfig(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Map<String,Object> data = new HashMap<>();
            //IM
            data.put("imSdkAppId",Constant.TENCENT_YUN_SDK_APPID);
            data.put("imAdmin",Constant.TENCENT_YUN_IDENTIFIER);

            //是否显示红包
            data.put("isRedpacketShow",parameterService.getParameterValueByKey("is_redpacket_show"));
            //cos信息
            data.put("cosBucket",Constant.TENCENT_YUN_COS_BUCKET);
            data.put("cosRegion",Constant.TENCENT_YUN_COS_REGION);
            //音乐url
            data.put("musicUrl",Constant.TENCENT_YUN_MUSIC_URL);
            data.put("yaoyaoNeedLevel",parameterService.getParameterValueByKey("yaoyao_need_level"));
            data.put("yaoyaoShow",parameterService.getParameterValueByKey("yaoyao_show"));

            result.setData(data);
            return result;
        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }


}
