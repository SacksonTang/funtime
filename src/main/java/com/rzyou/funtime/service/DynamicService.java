package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeComment;
import com.rzyou.funtime.entity.FuntimeDynamic;

import java.util.List;
import java.util.Map;

public interface DynamicService {

    /**
     * 发布动态
     * @param dynamic
     */
    void addDynamic(FuntimeDynamic dynamic);

    /**
     * 发表评论
     * @param comment
     *
     */
    Long addComment(FuntimeComment comment);

    /**
     * 点赞
     * @param userId
     * @param dynamicId
     */
    void addLike(Long userId, Long dynamicId);

    /**
     * 动态列表
     * @param lastId
     * @param startPage
     * @param pageSize
     * @param userId
     * @return
     */
    List<Map<String,Object>> getDynamicList(Long lastId, Integer startPage, Integer pageSize, Long userId);

    /**
     * 评论列表
     * @param lastId
     * @param dynamicId
     * @param startPage
     * @param pageSize
     * @return
     */
    List<Map<String,Object>> getCommentList(Long lastId,Long dynamicId,Integer startPage,Integer pageSize);

    /**
     * 删除动态
     * @param dynamicId
     * @param userId
     */
    void delDynamic(Long dynamicId, Long userId);

    /**
     * 删除评论
     * @param commentId
     * @param userId
     * @param dynamicId
     */
    void delComment(Long commentId, Long userId, Long dynamicId);

    /**
     * 取消点赞
     * @param userId
     * @param dynamicId
     */
    void delLike(Long userId, Long dynamicId);

    /**
     * 我的动态
     * @param lastId
     * @param startPage
     * @param pageSize
     * @param userId
     * @return
     */
    List<Map<String, Object>> getMyDynamicList(Long lastId, Integer startPage, Integer pageSize, Long userId);

    /**
     * 其他人的动态
     * @param lastId
     * @param startPage
     * @param pageSize
     * @param userId
     * @param toUserId
     * @return
     */
    List<Map<String, Object>> getOtherDynamicList(Long lastId, Integer startPage, Integer pageSize, Long userId, Long toUserId);

    /**
     * 点赞列表
     * @param lastId
     * @param dynamicId
     * @param startPage
     * @param pageSize
     * @return
     */
    List<Map<String, Object>> getLikeList(Long lastId, Long dynamicId, Integer startPage, Integer pageSize);

    /**
     * 动态详情
     * @param userId
     * @param dynamicId
     * @return
     */
    Map<String, Object> getDynamicById(Long userId, Long dynamicId);

    /**
     * 动态消息列表
     * @param lastId
     * @param userId
     * @param startPage
     * @param pageSize
     * @return
     */
    List<Map<String, Object>> getDynamicNoticeList(Long lastId, Long userId, Integer startPage, Integer pageSize);

    /**
     * 动态通知
     * @param userId
     * @return
     */
    Map<String, Object> getDynamicNoticeCounts(Long userId);
}
