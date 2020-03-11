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
}
