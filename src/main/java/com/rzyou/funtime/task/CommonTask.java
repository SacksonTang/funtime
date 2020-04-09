package com.rzyou.funtime.task;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.entity.FuntimeNotice;
import com.rzyou.funtime.service.*;
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
    @Autowired
    GameService gameService;

    Integer[] sendGroupType = {1,2,3,4,5,6,7,8,10,11,12,13,14,17,18};
    Integer[] sendSingleType = {15,16};
    Integer[] snedAllApp = {9};

    /**
     * 背景资源过去设置
     */
    @Scheduled(fixedRate = 1000*5*60)
    public void setBackgroundTask(){
        try {
            roomService.setBackgroundTask();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 摇摇乐奖池重置
     */
    @Scheduled(cron = "1 0 0 ? * MON")
    public void resetYaoyaoPool(){
        gameService.updateYaoyaoPoolTask();
    }

    /**
     * 心跳合并
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void heartTask(){
        userService.heartTask();
    }

    /**
     * 同步房间用户数
     */
    @Scheduled(fixedRate = 1000*5*60)
    public void updateOnlineNumTask(){
        roomService.updateOnlineNumTask();
    }

    /**
     * 房内离线用户下麦
     */
    @Scheduled(fixedRate = 1000*5)
    public void offlineUserTask(){
        List<Long> users = userService.getOfflineUser();
        if (users!=null&&!users.isEmpty()){
            for (Long userId : users){
                try {

                    roomService.roomExitTask(userId);
                }catch (Exception e){
                    log.error("offlineUserTask ===>异常用户ID:{}",userId);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * APP用户离线
     */
    @Scheduled(fixedRate = 1000*60)
    public void offlineUserAppTask(){
        userService.offlineUserAppTask();
    }

    /**
     * 订单查询
     */
    @Scheduled(fixedRate = 1000*30)
    public void orderQueryTask(){
        try {
            accountService.orderQueryTask();
        }catch (Exception e){
            e.printStackTrace();
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
     * 处理通知
     */

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
                noticeService.sendAllAppNotice(userSig, failNotice.getData(),failNotice.getId());
            }
        }

    }
    @Scheduled(fixedRate = 1000*3)
    public void notice5() {
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        List<FuntimeNotice> failNotices = noticeService.getFailNotice(5);
        if (failNotices != null && !failNotices.isEmpty()) {
            for (FuntimeNotice failNotice : failNotices) {

                noticeService.sendMsgNotice(userSig, failNotice.getData(), failNotice.getId());
            }
        }
    }

}
