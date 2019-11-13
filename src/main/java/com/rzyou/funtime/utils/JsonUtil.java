package com.rzyou.funtime.utils;

import com.alibaba.fastjson.JSONObject;

public class JsonUtil {

    public static JSONObject getParamterJson(String str){
        JSONObject obj = JSONObject.parseObject(str);
        JSONObject paramJson = obj.getJSONObject("param");
        return paramJson;
    }
}
