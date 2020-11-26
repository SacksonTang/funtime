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

import java.time.LocalDateTime;
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
    Game21Service game21Service;
    @Autowired
    Game123Service game123Service;
    @Autowired
    GameService gameService;
    @Autowired
    HeadwearService headwearService;

    /**
     * 匹配
     */
    @Scheduled(fixedRate = 6000)
    public void doMatchTask(){
        try {
            roomService.doMatchTask();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 房间热度重置
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetRoomHotsTask(){
        log.info("房间热度重置 resetRoomHots:{}",DateUtil.getCurrentDateTimeExtr());
        try {
            roomService.resetRoomHotsTask();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 18秒后自动停牌
     */
    @Scheduled(fixedRate = 1000)
    public void game21Task(){
        long time = System.currentTimeMillis();
        try {
            game21Service.game21Task(time);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 数值游戏定时任务
     */
    @Scheduled(fixedRate = 1000*60)
    public void game123Task(){
        try {
            game123Service.game123Task();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 定时解散房间
     */
    @Scheduled(fixedRate = 1000*60*30)
    public void roomCloseTask(){
        try {
            roomService.roomCloseTask();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 管理员过期删除
     */
    @Scheduled(fixedRate = 1000*60)
    public void deleteChatroomManagerTask(){
        log.debug("管理员过期删除 deleteChatroomManagerTask:{}",DateUtil.getCurrentDateTimeExtr());
        try {
            roomService.deleteChatroomManagerTask();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 背景资源过去设置
     */
    @Scheduled(fixedRate = 1000*5*60)
    public void setBackgroundTask(){
        log.debug("背景资源过去设置 setBackgroundTask:{}",DateUtil.getCurrentDateTimeExtr());
        try {
            roomService.setBackgroundTask();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 设置座驾
     */
    @Scheduled(fixedRate = 1000*5*60)
    public void setCarTask(){
        log.debug("设置座驾过期 setCarTask:{}",DateUtil.getCurrentDateTimeExtr());
        try {
            accountService.setCarTask();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 设置头饰
     */
    @Scheduled(fixedRate = 1000*5*60)
    public void setHeadwearTask(){
        log.debug("设置头饰过期 setHeadwearTask:{}",DateUtil.getCurrentDateTimeExtr());
        try {
            headwearService.setHeadwearTask();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 摇摇乐奖池重置
     */
    @Scheduled(cron = "1 0 0 ? * MON")
    public void resetYaoyaoPool(){
        log.info("摇摇乐奖池重置 resetYaoyaoPool:{}",DateUtil.getCurrentDateTimeExtr());
        try {
            gameService.updateYaoyaoPoolTask();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Scheduled(cron = "0 0 5 * * ?")
    public void resetYaoyaoPool2(){
        log.info("摇摇乐奖池重置 resetYaoyaoPool2:{}",DateUtil.getCurrentDateTimeExtr());
        try {
            gameService.updateYaoyaoPoolTask2();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 心跳合并
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void heartTask(){
        log.debug("心跳合并 heartTask:{}",DateUtil.getCurrentDateTimeExtr());
        try {
            userService.heartTask();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 同步房间用户数
     */
    //@Scheduled(fixedRate = 1000*5*60)
    public void updateOnlineNumTask(){
        log.debug("同步房间用户数 updateOnlineNumTask:{}",DateUtil.getCurrentDateTimeExtr());
        roomService.updateOnlineNumTask();
    }

    /**
     * 房内离线用户下麦
     */
    //@Scheduled(fixedRate = 1000*30)
    public void offlineUserTask(){
        log.debug("offlineUserTask:{}",DateUtil.getCurrentDateTimeExtr());
        List<Long> users = userService.getOfflineUser();
        if (users!=null&&!users.isEmpty()){
            for (Long userId : users){
                try {
                    roomService.roomMicLowerTask(userId);
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
        log.debug("offlineUserAppTask:{}",DateUtil.getCurrentDateTimeExtr());
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
     * 发送房间麦位信息
     */
    @Scheduled(fixedRate = 1000*5)
    public void sendRoomMicInfoTask(){
        log.debug("sendRoomMicInfoTask:{}",DateUtil.getCurrentDateTimeExtr());
        roomService.sendRoomMicInfoTask();

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
