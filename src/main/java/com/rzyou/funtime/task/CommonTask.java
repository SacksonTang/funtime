package com.rzyou.funtime.task;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.RoomService;
import com.rzyou.funtime.utils.DateUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class CommonTask {

    @Autowired
    AccountService accountService;
    @Autowired
    RoomService roomService;

    /**
     * 红包失效
     */
    @Scheduled(fixedRate = 1000*60)
    public void redpacketTask(){

        try {
            accountService.updateStateForInvalid();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 同步腾讯群组
     */
    @Scheduled(fixedRate = 1000*2)
    public void syncTencent(){
        try {
            log.debug("同步腾讯群组接口：start:{}", DateUtil.getCurrentDateTimeExtr());
            roomService.syncTencent(UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER));
            log.debug("同步腾讯群组接口：end:{}", DateUtil.getCurrentDateTimeExtr());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
