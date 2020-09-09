package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.entity.FuntimeComment;
import com.rzyou.funtime.entity.FuntimeDynamic;
import com.rzyou.funtime.mapper.FuntimeDynamicMapper;
import com.rzyou.funtime.service.DynamicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DynamicServiceImpl implements DynamicService {

    @Autowired
    FuntimeDynamicMapper dynamicMapper;

    @Override
    public void addDynamic(FuntimeDynamic dynamic) {
        dynamicMapper.insertDynamic(dynamic);
    }

    @Override
    public void addComment(FuntimeComment comment) {
        dynamicMapper.insertComment(comment);
    }

    @Override
    public void addLike(Long userId, Long dynamicId) {
        if (dynamicMapper.checkDynamicLike(userId,dynamicId)==null){
            dynamicMapper.insertDynamicLike(userId, dynamicId);
        }
    }

    @Override
    public List<Map<String, Object>> getDynamicList(Long lastId, Integer startPage, Integer pageSize, Long userId) {
        if (startPage == 1){
            lastId = null;
        }

        return dynamicMapper.getDynamicList(pageSize,lastId,userId);
    }

    @Override
    public List<Map<String, Object>> getCommentList(Long lastId, Long dynamicId, Integer startPage, Integer pageSize) {
        if (startPage == 1){
            lastId = null;
        }
        return dynamicMapper.getCommentList(pageSize,lastId,dynamicId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void delDynamic(Long dynamicId, Long userId) {
        Long createUserId = dynamicMapper.getDynamicById(dynamicId);
        if (createUserId == null){
            throw new BusinessException(ErrorMsgEnum.DYNAMIC_NOT_EXISTS.getValue(), ErrorMsgEnum.DYNAMIC_NOT_EXISTS.getDesc());
        }
        if (!createUserId.equals(userId)){
            throw new BusinessException(ErrorMsgEnum.DYNAMIC_DEL_ERROR.getValue(), ErrorMsgEnum.DYNAMIC_DEL_ERROR.getDesc());
        }
        dynamicMapper.delDynamic(dynamicId);
        dynamicMapper.delCommentByDynamicId(dynamicId);

    }

    @Override
    public void delComment(Long commentId, Long userId, Long dynamicId) {
        Long createUserId = dynamicMapper.getCommentById(commentId);
        if (createUserId == null){
            throw new BusinessException(ErrorMsgEnum.COMMENT_NOT_EXISTS.getValue(), ErrorMsgEnum.COMMENT_NOT_EXISTS.getDesc());
        }
        Long createUserId2 = dynamicMapper.getDynamicById(dynamicId);
        if (createUserId2 == null){
            throw new BusinessException(ErrorMsgEnum.DYNAMIC_NOT_EXISTS.getValue(), ErrorMsgEnum.DYNAMIC_NOT_EXISTS.getDesc());
        }
        if (!createUserId.equals(userId)&&!createUserId2.equals(userId)){
            throw new BusinessException(ErrorMsgEnum.COMMENT_DEL_ERROR.getValue(), ErrorMsgEnum.COMMENT_DEL_ERROR.getDesc());
        }
        dynamicMapper.delCommentById(commentId);
    }

    @Override
    public void delLike(Long userId, Long dynamicId) {
        if (dynamicMapper.checkDynamicLike(userId,dynamicId)!=null){
            dynamicMapper.delDynamicLike(userId, dynamicId);
        }
    }

    @Override
    public List<Map<String, Object>> getMyDynamicList(Long lastId, Integer startPage, Integer pageSize, Long userId) {
        if (startPage == 1){
            lastId = null;
        }

        return dynamicMapper.getMyDynamicList(pageSize,lastId,userId);
    }
}
