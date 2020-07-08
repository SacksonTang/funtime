package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.encryption.AESUtil;
import com.rzyou.funtime.common.encryption.RsaUtils;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.common.wxutils.WeixinLoginUtils;
import com.rzyou.funtime.component.RedisUtil;
import com.rzyou.funtime.component.StaticData;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.SmsService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import com.rzyou.funtime.utils.JsonUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
            JSONObject paramJson;
            String flag = parameterService.getParameterValueByKey("is_encrypt");
            if (flag!=null&&flag.equals("1")){
                paramJson = HttpHelper.getParamterJsonDecrypt(request);
            }else{
                paramJson = HttpHelper.getParamterJson(request);
            }

            FuntimeUser user = JSONObject.toJavaObject(paramJson, FuntimeUser.class);
            if (user == null
                    || StringUtils.isBlank(user.getLoginType())) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            user.setAppVersion(HttpHelper.ver);
            user.setIp(HttpHelper.getClientIpAddr(request));
            user.setLastLoginTime(new Date());
            LoginStrategy strategy = StaticData.context.get(user.getLoginType());
            if(strategy==null){
                result.setCode(ErrorMsgEnum.USER_LOGINTYPE_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.USER_LOGINTYPE_ERROR.getDesc());
                return result;
            }
            FuntimeUser userInfo = strategy.login(user);

            if (user.getPlatform() == 0&&userInfo.getNewUser()){
                log.info("苹果新用户登录");
                String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_SYSTEMUSER);
                List<String> toAccounts = new ArrayList<>();
                toAccounts.add(userInfo.getId().toString());

                TencentUtil.batchsendmsg(userSig,toAccounts,Constant.APPLENEWUSERRETURN,Constant.TENCENT_YUN_SYSTEMUSER);
            }

            userInfo.setImSdkAppId(StaticData.TENCENT_YUN_SDK_APPID);
            Map<String, Object> map = JsonUtil.getMap("user", userInfo);
            if (flag!=null&&flag.equals("1")){
                String encrypt = AESUtil.aesDecrypt(JSONObject.toJSONString(map),Constant.AES_KEY);
                result.setData(encrypt);
            }else {
                result.setData(map);
            }

        } catch (BusinessException be) {
            log.error("login BusinessException==========>{}",be.getMsg());
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
            if (StringUtils.isBlank(phone)) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            FuntimeUser user = userService.queryUserInfoByPhone(phone);
            if (user!=null&&user.getState() == 2){
                result.setCode(ErrorMsgEnum.USER_IS_DELETE.getValue());
                result.setMsg(ErrorMsgEnum.USER_IS_DELETE.getDesc());
                return result;
            }
            smsService.sendSms(phone,resend,ip,smsType);
        }catch (BusinessException be){
            log.error("sendSms BusinessException==========>{}",be.getMsg());
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
            log.error("startRecharge BusinessException==========>{}",be.getMsg());
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
            data.put("imSdkAppId",StaticData.TENCENT_YUN_SDK_APPID);
            data.put("iosPushCertDevId",StaticData.IOS_PUSHCERTDEVID);
            data.put("iosPushCertProdId",StaticData.IOS_PUSHCERTPRODID);
            //data.put("androidPushCertDevId",StaticData.IOS_PUSHCERTPRODID);
            //data.put("androidPushCertProdId",StaticData.IOS_PUSHCERTPRODID);
            data.put("imAdmin",Constant.TENCENT_YUN_IDENTIFIER);
            data.put("imAdminSystemUser",Constant.TENCENT_YUN_SYSTEMUSER);

            //是否显示红包
            data.put("isRedpacketShow",parameterService.getParameterValueByKey("is_redpacket_show"));
            //data.put("isFishShow",parameterService.getParameterValueByKey("is_fish_show"));
            //cos信息
            data.put("cosBucket",Constant.TENCENT_YUN_COS_BUCKET);
            data.put("cosRegion",Constant.TENCENT_YUN_COS_REGION);
            //音乐url
            data.put("musicUrl",Constant.TENCENT_YUN_MUSIC_URL);
            //data.put("yaoyaoNeedLevel",parameterService.getParameterValueByKey("yaoyao_need_level"));
            //data.put("yaoyaoShow",parameterService.getParameterValueByKey("yaoyao_show"));
            data.put("heartRate",parameterService.getParameterValueByKey("heart_rate"));
            data.put("hornLength",parameterService.getParameterValueByKey("horn_length"));
            data.put("isEncrypt",parameterService.getParameterValueByKey("is_encrypt"));
            //data.put("roomGameTag",parameterService.getParameterValueByKey("room_game_tag"));
            //data.put("roomGameIcon",parameterService.getParameterValueByKey("room_game_icon"));
            data.put("roomGameEggUrl",parameterService.getParameterValueByKey("room_game_egg_url"));
            data.put("sysIcon",parameterService.getParameterValueByKey("sys_icon"));
            data.put("staticResource",parameterService.getStaticResource());
            data.put("userUrl",Constant.COS_URL_PREFIX+Constant.AGREEMENT_USER);
            data.put("priveteUrl",Constant.COS_URL_PREFIX+Constant.AGREEMENT_PRIVACY);
            data.put("boxRuleUrl",Constant.COS_URL_PREFIX+Constant.BOX_RULE);

            data.put("roomNotice",Constant.ROOM_NOTICE);
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

    /**
     * 获取客服
     * @param request
     * @return
     */
    @PostMapping("getCustomerService")
    public ResultMsg<Object> getCustomerService(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {

            Map<String,Object> resultMap = userService.getCustomerService();
            result.setData(JsonUtil.getMap("customerService",resultMap));
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



    /**
     * 删除用户
     */
    //@GetMapping("delUser")
    public ResultMsg<Object> delUser() {
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            List<String> userIds = userService.getAllUserIdByApp();
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            if (userIds.size()>100){
                int size = userIds.size();
                int fromIndex = 0;
                int toIndex = 100;
                int k = size%toIndex == 0?size/toIndex:size/toIndex+1;
                for (int j = 1;j<k+1;j++){
                    List<String> spList = userIds.subList(fromIndex,toIndex);
                    fromIndex = j*toIndex;
                    toIndex =  Math.min((j+1)*toIndex,size) ;
                    TencentUtil.accountDelete(userSig,spList);
                }
            }else {
                TencentUtil.accountDelete(userSig, userIds);
            }
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

}
