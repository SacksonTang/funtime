package com.rzyou.funtime.service;

import com.rzyou.funtime.common.ResultMsg;
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

    /**
     * 官网下单列表
     * @param lastId
     * @param startPage
     * @param pageSize
     * @param tagId
     * @return
     */
    Map<String, Object> getOrderList(Long lastId, Integer startPage, Integer pageSize, Integer tagId);

    /**
     * 接单列表
     * @param type
     * @param userId
     * @param lastId
     * @param startPage
     * @param pageSize
     * @return
     */
    Map<String, Object> getReceiveOrders(Integer type, Long userId, Long lastId, Integer startPage, Integer pageSize);

    /**
     * 我的订单列表
     * @param type
     * @param userId
     * @param lastId
     * @param startPage
     * @param pageSize
     * @return
     */
    Map<String, Object> getMyOrders(Integer type, Long userId, Long lastId, Integer startPage, Integer pageSize);

    /**
     * 下单
     * @param userId
     * @param toUserId
     * @param tagId
     * @param counts
     * @param remark
     * @param tagName
     */
    ResultMsg<Object> createOrder(Long userId, Long toUserId, Integer tagId, Integer counts, String remark, String tagName);

    /**
     * 取消订单
     * @param userId
     * @param orderId
     */
    void cancelOrder(Long userId, Long orderId);

    /**
     * 接单
     * @param userId
     * @param orderId
     */
    void receiveOrder(Long userId, Long orderId);

    /**
     * 拒绝接单
     * @param userId
     * @param orderId
     * @param rejectionReason
     */
    void refuseOrder(Long userId, Long orderId, String rejectionReason);

    /**
     * 服务完成
     * @param userId
     * @param orderId
     */
    void completeOrder(Long userId, Long orderId);

    /**
     * 订单详情
     * @param id
     * @return
     */
    Map<String, Object> getRecordInfoById(Long id);

    /**
     * 退款
     * @param userId
     * @param orderId
     * @param reason
     */
    void refundOrder(Long userId, Long orderId, String reason);
}
