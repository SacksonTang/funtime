package com.rzyou.funtime.service;

import java.util.Map;

public interface Game21Service {

    /**
     * 开始游戏
     * @param userId
     * @param roomId
     * @return
     */
    Map<String,Object> startGame(Long userId, Long roomId);

    /**
     * 结束游戏
     * @param roomId
     */
    void exitGame(Long roomId);

    /**
     * 要牌
     * @param userId
     * @param roomId
     * @param micLocation
     * @return
     */
    Map<String,Object> doGetPoker(Long userId, Long roomId, Integer micLocation);

    /**
     * 开启游戏
     * @param userId
     * @param roomId
     */
    void openGame(Long userId, Long roomId);


    /**
     * 停牌
     * @param userId
     * @param roomId
     * @param micLocation
     * @return
     */
    Map<String,Object> doStopPoker(Long userId, Long roomId, Integer micLocation);

    /**
     * 定时任务
     * @param time
     */
    void game21Task(long time);

    /**
     * 游戏数据
     * @param userId
     * @param roomId
     * @return
     */
    Map<String,Object> getGameInfo(Long userId, Long roomId);

    /**
     * 查询游戏中的麦位用户
     * @param roomId
     * @param mic
     * @return
     */
    Long getUserByRoomAndMic(Long roomId,Integer mic);

    /**
     * 下麦操作
     * @param roomId
     * @param mic
     */
    void delMicInfoForlowerWheat(Long roomId, Integer mic);
}
