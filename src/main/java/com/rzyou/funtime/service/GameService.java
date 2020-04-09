package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeGameYaoyaoPool;

import java.util.List;
import java.util.Map;

/**
 * 2020/2/27
 * LLP-LX
 */
public interface GameService {

    /**
     * 是否显示
     * @param type
     * @param userId
     * @return
     */
    boolean getYaoyaoShowConf(int type, Long userId);

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
     * @return
     */
    Map<String,Object> drawing(Integer id, Long userId);

    /**
     * 修改摇摇乐配置
     */
    void updateYaoyaoPoolTask();

    /**
     * 获取捕鱼得分和子弹数
     * @param userId
     * @return
     */
    Map<String,Object> getBulletOfFish(Long userId);

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
     */
    void buyBullet(Long userId, Integer bullet);

    /**
     * 捕鱼排行榜
     * @param curUserId
     * @return
     */
    Map<String,Object> getFishRanklist(Long curUserId);
}
