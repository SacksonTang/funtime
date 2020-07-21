package com.rzyou.funtime.service;


import java.util.List;
import java.util.Map;

public interface Game123Service {
    /**
     * 状态
     * @param roomId
     * @return
     */
    Integer getStateByRoomId(Long roomId);
    /**
     * 游戏创建者
     * @param roomId
     * @return
     */
    Long getUserByRoomId(Long roomId);
    /**
     * 开始游戏
     * @param userId
     * @param roomId
     * @return
     */
    void startGame(Long userId, Long roomId);

    /**
     * 清空数值
     * @param userId
     * @param roomId
     */
    void clearVal(Long userId, Long roomId);

    /**
     * 结束游戏
     * @param roomId
     */
    void exitGame(Long roomId);

    /**
     * 关闭房间退出游戏
     * @param roomId
     */
    void exitGameForRoomClose(Long roomId);

    /**
     * 退出房间重置过期时间
     * @param userId
     * @param roomId
     */
    void setExitTimeByExit(Long userId, Long roomId);
    /**
     * 进入房间重置过期时间
     * @param userId
     * @param roomId
     */
    void setExitTimeByJoin(Long userId, Long roomId);

    /**
     * 保存数值
     * @param userIds
     * @param roomId
     * @param blueAmount
     */
    void saveGame123Val(List<Long> userIds, Long roomId, Integer blueAmount);
    /**
     * 保存数值
     * @param userIdsMap
     * @param roomId
     */
    void saveGame123Val(Map<Long,Integer> userIdsMap, Long roomId);
    /**
     * 开启游戏
     * @param userId
     * @param roomId
     */
    void openGame(Long userId, Long roomId);

    /**
     * 定时任务
     */
    void game123Task();





}
