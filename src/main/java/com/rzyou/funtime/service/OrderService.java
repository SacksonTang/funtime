package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeOrder;

import java.util.Map;

public interface OrderService {
    /**
     * 发布订单
     * @param order
     */
    void addOrder(FuntimeOrder order);

    /**
     * 下单列表
     * @param lastId
     * @param startPage
     * @param pageSize
     * @param tagId
     * @param sex
     * @return
     */
    Map<String, Object> getOrderList(Long lastId, Integer startPage, Integer pageSize, Integer tagId, Integer sex);

    /**
     * 订单详情
     * @param userId
     * @return
     */
    Map<String,Object> getOrderById(Long userId);

    /**
     * 推荐列表
     * @return
     * @param tagId
     */
    Map<String, Object> getRecommendationOrderList(Integer tagId);
}
