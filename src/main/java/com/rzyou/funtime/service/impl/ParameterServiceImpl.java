package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.mapper.FuntimeParameterMapper;
import com.rzyou.funtime.service.ParameterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParameterServiceImpl implements ParameterService {

    @Autowired
    FuntimeParameterMapper parameterMapper;

    @Override
    public String getParameterValueByKey(String key) {
        String val = parameterMapper.getParameterValueByKey(key);
        if (StringUtils.isBlank(key)){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
        }
        return val;
    }
}
