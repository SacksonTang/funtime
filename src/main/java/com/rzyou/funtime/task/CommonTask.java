package com.rzyou.funtime.task;

import com.rzyou.funtime.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CommonTask {

    @Autowired
    AccountService accountService;

    @Scheduled(cron = "0 /1 ?")
    public void redpacketTask(){

        try {
            accountService.updateStateForInvalid();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

}
