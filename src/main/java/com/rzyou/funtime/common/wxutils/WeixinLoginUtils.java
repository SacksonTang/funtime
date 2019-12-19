package com.rzyou.funtime.common.wxutils;
import com.alibaba.fastjson.JSONObject;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.httputil.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * @description: app用户登陆
 **/
@Slf4j
public class WeixinLoginUtils {
    /**
     * 通过code获取token
     * @return
     */
    public static JSONObject getAccessToken(String code){
        //构建请求数据
        String url = Constant.WX_GET_TOKEN_URL+"?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";

        url = url.replaceAll("APPID",Constant.WX_APPID)
                .replaceAll("SECRET",Constant.WX_APPSECRET)
                .replaceAll("CODE",code);

        //调用httpclient处理请求得到返回json数据
        String returnJson = HttpClientUtil.doGet(url);
        JSONObject resultObj = JSONObject.parseObject(returnJson);
        log.debug("getAccessToken result : {}",returnJson);
        if (resultObj.getString("errcode")!=null){
            throw new BusinessException(ErrorMsgEnum.USER_WXLOGIN_TOKEN_ERROR.getValue(),ErrorMsgEnum.USER_WXLOGIN_TOKEN_ERROR.getDesc());
        }
        if (StringUtils.isBlank(resultObj.getString("access_token"))||StringUtils.isBlank(resultObj.getString("openid"))){
            throw new BusinessException(ErrorMsgEnum.USER_WXLOGIN_TOKEN_ERROR.getValue(),ErrorMsgEnum.USER_WXLOGIN_TOKEN_ERROR.getDesc());
        }
        return resultObj;
    }

    public static JSONObject refreshToken(String refresh_token){
        //构建请求数据
        String url = Constant.WX_REFRESH_TOKEN_URL+"?appid=APPID&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";

        url = url.replaceAll("APPID",Constant.WX_APPID)
                .replaceAll("REFRESH_TOKEN",refresh_token);

        //调用httpclient处理请求得到返回json数据
        String returnJson = HttpClientUtil.doGet(url);
        log.debug("refreshToken result : {}",returnJson);
        JSONObject resultObj = JSONObject.parseObject(returnJson);
        if (resultObj.getString("errcode")!=null){
            throw new BusinessException(ErrorMsgEnum.USER_WXLOGIN_REFRESHTOKEN_ERROR.getValue(),ErrorMsgEnum.USER_WXLOGIN_REFRESHTOKEN_ERROR.getDesc());
        }
        return resultObj;
    }

    public static JSONObject isAccessTokenIsInvalid(String access_token,String openid){
        //构建请求数据
        String url = Constant.WX_CHECK_TOKEN_URL+"?access_token=ACCESS_TOKEN&openid=OPENID";

        url = url.replaceAll("ACCESS_TOKEN",access_token)
                .replaceAll("OPENID",openid);

        //调用httpclient处理请求得到返回json数据
        String returnJson = HttpClientUtil.doGet(url);
        log.debug("isAccessTokenIsInvalid result : {}",returnJson);
        JSONObject resultObj = JSONObject.parseObject(returnJson);
        if (!resultObj.getInteger("errcode").equals(0)){
            throw new BusinessException(ErrorMsgEnum.USER_WXLOGIN_VALIDTOKEN_ERROR.getValue(),ErrorMsgEnum.USER_WXLOGIN_VALIDTOKEN_ERROR.getDesc());
        }
        return resultObj;
    }

    public static JSONObject getUserInfo(String access_token,String openid){
        //构建请求数据
        String url = Constant.WX_USERINFO_URL+"?access_token=ACCESS_TOKEN&openid=OPENID";

        url = url.replaceAll("ACCESS_TOKEN",access_token)
                .replaceAll("OPENID",openid);

        //调用httpclient处理请求得到返回json数据
        String returnJson = HttpClientUtil.doGet(url);
        log.debug("getUserInfo result : {}",returnJson);
        JSONObject resultObj = JSONObject.parseObject(returnJson);
        if (resultObj.getString("errcode")!=null){
            throw new BusinessException(ErrorMsgEnum.USER_WXLOGIN_USERINFO_ERROR.getValue(),ErrorMsgEnum.USER_WXLOGIN_USERINFO_ERROR.getDesc());
        }
        return resultObj;
    }

}
