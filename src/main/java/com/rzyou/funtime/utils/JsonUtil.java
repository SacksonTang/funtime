package com.rzyou.funtime.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static JSONObject getParamterJson(String str){
        JSONObject obj = JSONObject.parseObject(str);
        JSONObject paramJson = obj.getJSONObject("param");
        return paramJson;
    }

    public static Map<String,Object> getMap(String key,Object value){
        Map<String,Object> map = new HashMap<>();
        map.put(key,value);
        return map;

    }
}
