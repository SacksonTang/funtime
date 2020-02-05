package com.rzyou.funtime.task;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.entity.FuntimeNotice;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.NoticeService;
import com.rzyou.funtime.service.RoomService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.DateUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CommonTask {

    @Autowired
    AccountService accountService;
    @Autowired
    RoomService roomService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    UserService userService;

    Integer[] sendGroupType = {1,2,3,4,5,6,7,8,10,11,12,13,14,17,18};
    Integer[] sendSingleType = {15,16};
    Integer[] snedAllApp = {9};

    /**
     * 房内离线用户退房
     */
    @Scheduled(fixedRate = 1000*5)
    public void offlineUserTask(){
        List<Long> users = userService.getOfflineUser();
        if (users!=null&&!users.isEmpty()){
            for (Long userId : users){
                roomService.roomExitTask(userId);
            }
        }
    }

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

    /**
     * 处理通知
     */
    @Scheduled(fixedRate = 1000*3)
    public void notice1() {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        List<FuntimeNotice> failNotices = noticeService.getFailNotice(1);
        if (failNotices != null && !failNotices.isEmpty()) {
            for (FuntimeNotice failNotice : failNotices) {

                noticeService.sendGroupNotice(userSig, failNotice.getData(), failNotice.getId());
            }
        }
    }
    @Scheduled(fixedRate = 1000*3)
    public void notice2() {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        List<FuntimeNotice> failNotices = noticeService.getFailNotice(2);
        if (failNotices != null && !failNotices.isEmpty()) {
            for (FuntimeNotice failNotice : failNotices) {

                noticeService.sendSingleNotice(userSig, failNotice.getData(), failNotice.getId());
            }
        }
    }
    @Scheduled(fixedRate = 1000*3)
    public void notice3() {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        List<FuntimeNotice> failNotices = noticeService.getFailNotice(3);
        if (failNotices != null && !failNotices.isEmpty()) {
            for (FuntimeNotice failNotice : failNotices) {
                noticeService.snedAllRoomAppNotice(userSig, failNotice.getData(), failNotice.getId());
            }
        }
    }
    @Scheduled(fixedRate = 1000*3)
    public void notice4(){
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        List<FuntimeNotice> failNotices = noticeService.getFailNotice(4);
        if (failNotices != null&&!failNotices.isEmpty()){
            for (FuntimeNotice failNotice : failNotices){
                noticeService.snedAllAppNotice(userSig, failNotice.getData(),failNotice.getId());
            }
        }

    }

}
