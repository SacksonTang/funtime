package com.rzyou.funtime.utils;

import com.rzyou.funtime.common.Constant;
import com.tencentyun.TLSSigAPIv2;

public class UsersigUtil {

    public static String getUsersig(String identifier,long expire){
        TLSSigAPIv2 api = new TLSSigAPIv2(Constant.TENCENT_YUN_SDK_APPID, Constant.TENCENT_YUN_SDK_APPSECRET);
        return api.genSig(identifier, expire);
    }
}
