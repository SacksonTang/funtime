package com.rzyou.funtime.service;

import java.util.List;
import java.util.Map;

public interface ParameterService {

    String getParameterValueByKey(String key);

    void updateValueByKey(String parameterKey,String parameterValue);

    List<Map<String,Object>> getStaticResource();
}
