package com.rzyou.funtime.service.loginservice.impl;

import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.service.loginservice.LoginStrategy;
import org.springframework.stereotype.Service;

@Service("wxLogin")
public class WxLogin implements LoginStrategy {
    @Override
    public FuntimeUser login(FuntimeUser user) {
        return null;
    }
}
