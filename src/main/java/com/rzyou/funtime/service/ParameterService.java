package com.rzyou.funtime.service;

public interface ParameterService {

    String getParameterValueByKey(String key);

    void updateValueByKey(String parameterKey,String parameterValue);
}
