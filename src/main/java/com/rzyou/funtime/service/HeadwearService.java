package com.rzyou.funtime.service;

import com.rzyou.funtime.common.ResultMsg;

import java.util.List;
import java.util.Map;

public interface HeadwearService {

    /**
     * 当前头饰
     * @param userId
     * @return
     */
    Integer getCurrnetHeadwear(Long userId);

    /**
     * 头饰列表
     * @param userId
     * @return
     */
    Map<String, Object> getHeadwearList(Long userId);

    /**
     * 购买头饰
     * @param userId
     * @param id
     * @return
     */
    ResultMsg<Object> buyHeadwear(Long userId, Integer id);

    /**
     * 设置头饰
     * @param userId
     * @param headwearId
     */
    void setHeadwear(Long userId, Integer headwearId);

    /**
     * 设置头饰(等级)
     * @param userId
     */
    void setHeadwear(Long userId);

    /**
     * 定时头饰清理
     */
    void setHeadwearTask();

    /**
     * 取消头饰
     * @param userId
     * @param headwearId
     */
    void cancelHeadwear(Long userId, Integer headwearId);

    /**
     * 取消头饰
     * @param userId
     */
    void cancelHeadwear(Long userId);
}
