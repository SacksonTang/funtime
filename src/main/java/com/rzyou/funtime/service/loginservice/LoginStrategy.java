package com.rzyou.funtime.service.loginservice;

import com.rzyou.funtime.entity.FuntimeUser;


public interface LoginStrategy {

    FuntimeUser login(FuntimeUser user);

}
