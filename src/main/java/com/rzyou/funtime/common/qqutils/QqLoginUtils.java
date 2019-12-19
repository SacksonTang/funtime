package com.rzyou.funtime.common.qqutils;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.httputil.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class QqLoginUtils {

    /**
     * 获取access_token
     * @param code
     * @param redirectUri
     * @return
     */
    public static String getAccessToken(String code,String redirectUri){
        try {
            redirectUri = URLEncoder.encode(redirectUri,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BusinessException(ErrorMsgEnum.USER_QQLOGIN_GETTOKEN_ERROR.getValue(),ErrorMsgEnum.USER_QQLOGIN_GETTOKEN_ERROR.getDesc());
        }
        String url = Constant.QQ_GET_TOKEN_URL
                + "&client_id=" + Constant.QQ_APPID + "&redirect_uri=" + redirectUri
                + "&client_secret=" + Constant.QQ_APPSECRET + "&code=" + code;
        String result = HttpClientUtil.doGet(url);
        log.debug("getAccessToken result : {}",result);
        if (result.indexOf("callback") == -1){
            throw new BusinessException(ErrorMsgEnum.USER_QQLOGIN_GETTOKEN_ERROR.getValue(),ErrorMsgEnum.USER_QQLOGIN_GETTOKEN_ERROR.getDesc());
        }
        Pattern p = Pattern.compile("access_token=(\\w*)&");
        Matcher m = p.matcher(result);
        m.find();
        //得到access_token
        String access_token = m.group(1);
        return access_token;

    }

    /**
     * 根据token获取openId
     * @param accessToken
     * @return
     */
    public static String getOpenId(String accessToken){
        //构建请求数据
        String url = Constant.QQ_GET_OPENID_URL+"?access_token=" + accessToken;

        //调用httpclient处理请求得到返回json数据
        String result = HttpClientUtil.doGet(url);
        log.debug("getOpenId result : {}",result);
        if (result.indexOf("callback") == -1){
            throw new BusinessException(ErrorMsgEnum.USER_QQLOGIN_GETOPENID_ERROR.getValue(),ErrorMsgEnum.USER_QQLOGIN_GETOPENID_ERROR.getDesc());
        }
        Pattern p = Pattern.compile("openid\":\"(\\w*)\"");
        Matcher m = p.matcher(result);
        m.find();
        //得到openid
        String openId = m.group(1);

        return openId;
    }

    /**
     * 获取用户信息
     * @param accessToken
     * @param openId
     * @return
     */
    public static JSONObject getUserInfo(String accessToken,String openId){
        String url = Constant.QQ_GET_USERINFO_URL+"?access_token="+accessToken+"&oauth_consumer_key="+Constant.QQ_APPID+"&openid="+openId;
        // 获取用户昵称、头像等信息，{ret: 0, msg: '', nickname: '', ...} ret不为0表示失败
        String returnJson = HttpClientUtil.doGet(url);
        JSONObject resultObj = JSONObject.parseObject(returnJson);
        log.debug("getUserInfo result : {}",returnJson);
        Integer ret = resultObj.getInteger("ret");
        if(ret != 0){
            throw  new BusinessException(ErrorMsgEnum.USER_QQLOGIN_GETUSERINFO_ERROR.getValue(),ErrorMsgEnum.USER_QQLOGIN_GETUSERINFO_ERROR.getDesc());
        }
        return resultObj;

    }

}
