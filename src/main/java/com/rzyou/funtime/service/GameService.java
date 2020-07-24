package com.rzyou.funtime.service;

import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.entity.FuntimeGameYaoyaoPool;

import java.util.List;
import java.util.Map;

/**
 * 2020/2/27
 * LLP-LX
 */
public interface GameService {

    /**
     * 摇摇乐是否显示
     * @param type
     * @param userId
     * @return
     */
    boolean getYaoyaoShowConf(int type, Long userId);
    /**
     * 捕鱼是否显示
     * @param type
     * @param userId
     * @return
     */
    boolean getFishShowConf(int type, Long userId);
    /**
     * 砸蛋是否显示
     * @param type
     * @param userId
     * @param level
     * @return
     */
    boolean getSmasheggShowConf(int type, Long userId, Integer level);
    /**
     * 转盘抽奖是否显示
     * @param type
     * @param userId
     * @param level
     * @return
     */
    boolean getCircleShowConf(int type, Long userId, Integer level);

    /**
     * 初始化摇摇乐
     * @param type
     * @return
     */
    List<FuntimeGameYaoyaoPool> getYaoyaoPool(Integer type);

    /**
     * 摇摇乐抽奖
     * @param id
     * @param userId
     * @param roomId
     * @return
     */
    Map<String,Object> drawing(Integer id, Long userId, Long roomId);

    /**
     * 修改摇摇乐配置
     */
    void updateYaoyaoPoolTask();

    /**
     * 获取捕鱼得分和子弹数
     * @param userId
     * @param roomId
     * @return
     */
    Map<String,Object> getBulletOfFish(Long userId, Long roomId);

    /**
     * 保存捕鱼得分
     * @param userId
     * @param score
     * @param bullet
     */
    void saveScoreOfFish(Long userId, Integer score, Integer bullet);

    /**
     * 购买子弹
     * @param userId
     * @param bullet
     * @param type
     * @param roomId
     */
    ResultMsg<Object> buyBullet(Long userId, Integer bullet, Integer type, Long roomId);

    /**
     * 捕鱼排行榜
     * @param curUserId
     * @param type
     * @return
     */
    Map<String,Object> getFishRanklist(Long curUserId, Integer type);

    /**
     * 获取游戏配置
     * @param userId
     * @param roomId
     * @return
     */
    List<Map<String,Object>> getGameList(Long userId, Long roomId);

    /**
     * 砸蛋配置
     * @param userId
     * @return
     */
    Map<String,Object> getSmashEggConf(Long userId);

    /**
     * 砸蛋
     * @param userId
     * @param counts
     * @param type
     * @param roomId
     * @return
     */
    ResultMsg<Object> eggDrawing(Long userId, Integer counts, Integer type, Long roomId);

    /**
     * 转盘配置
     * @param userId
     * @return
     */
    Map<String,Object> getCircleConf(Long userId);

    /**
     * 活动转盘配置
     * @return
     * @param activityNo
     * @param channelNo
     * @param ip
     */
    ResultMsg<Object> getCircleActivityConf(String activityNo, String channelNo, String ip);

    /**
     * 转盘
     * @param userId
     * @param counts
     * @param roomId
     * @return
     */
    ResultMsg<Object> circleDrawing(Long userId, Integer counts, Long roomId);

    /**
     * 活动夺宝
     * @param userId
     * @param activityNo
     * @param channelNo
     * @return
     */
    ResultMsg<Object> circleActivityDrawing(Long userId, String activityNo, String channelNo);

    /**
     * 跳一跳得分
     * @param userId
     * @param score
     * @param roomId
     */
    void saveScoreOfTyt(Long userId, Integer score, Long roomId);

    /**
     * 跳一跳排行榜
     * @param userId
     * @param type
     * @param roomId
     * @return
     */
    Map<String,Object> getTytRanklist(Long userId, Integer type, Long roomId);

    /**
     * 定时摇摇乐
     */
    void updateYaoyaoPoolTask2();
}
