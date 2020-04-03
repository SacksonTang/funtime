package com.rzyou.funtime.utils;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.component.StaticData;
import com.tencentyun.TLSSigAPIv2;

public class UsersigUtil {

    public static String getUsersig(String identifier){
        return getUsersig(identifier,1000*60*60*24);
    }

    public static String getUsersig(String identifier,long expire){
        TLSSigAPIv2 api = new TLSSigAPIv2(StaticData.TENCENT_YUN_SDK_APPID, StaticData.TENCENT_YUN_SDK_APPSECRET);
        return api.genSig(identifier, expire);
    }
}
