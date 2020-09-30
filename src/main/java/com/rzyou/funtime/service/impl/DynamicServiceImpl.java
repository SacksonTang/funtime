package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.entity.FuntimeComment;
import com.rzyou.funtime.entity.FuntimeDynamic;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.mapper.FuntimeDynamicMapper;
import com.rzyou.funtime.service.DynamicService;
import com.rzyou.funtime.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DynamicServiceImpl implements DynamicService {

    @Autowired
    FuntimeDynamicMapper dynamicMapper;
    @Autowired
    UserService userService;

    @Override
    public void addDynamic(FuntimeDynamic dynamic) {
        if (StringUtils.isNotBlank(dynamic.getDynamic())) {
            userService.checkSensitive(dynamic.getDynamic());
        }
        dynamicMapper.insertDynamic(dynamic);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Long addComment(FuntimeComment comment) {
        if (StringUtils.isNotBlank(comment.getComment())) {
            userService.checkSensitive(comment.getComment());
        }
        Long dyUserId = dynamicMapper.getDynamicById(comment.getDynamicId());
        if (dyUserId==null){
            throw new BusinessException(ErrorMsgEnum.DYNAMIC_NOT_EXISTS.getValue(), ErrorMsgEnum.DYNAMIC_NOT_EXISTS.getDesc());
        }
        dynamicMapper.insertComment(comment);

        if (comment.getToUserId()==null){
            addCounts(dyUserId);
        }else{
            addCounts(comment.getToUserId());
            if (!dyUserId.equals(comment.getToUserId())){
                addCounts(dyUserId);
            }
        }

        return comment.getId();
    }

    public void addCounts(Long userId){
        Integer counts = dynamicMapper.getNoticeCounts(userId);
        if (counts == null){
            dynamicMapper.insertDyCounts(userId);
        }else{
            dynamicMapper.updateDyCounts(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void addLike(Long userId, Long dynamicId) {
        Long dyUserId = dynamicMapper.getDynamicById(dynamicId);
        if (dyUserId==null){
            throw new BusinessException(ErrorMsgEnum.DYNAMIC_NOT_EXISTS.getValue(), ErrorMsgEnum.DYNAMIC_NOT_EXISTS.getDesc());
        }
        if (dynamicMapper.checkDynamicLike(userId,dynamicId)==null){
            dynamicMapper.insertDynamicLike(userId, dynamicId);
        }
        addCounts(dyUserId);
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

    @Override
    public List<Map<String, Object>> getOtherDynamicList(Long lastId, Integer startPage, Integer pageSize, Long userId, Long toUserId) {
        if (startPage == 1){
            lastId = null;
        }

        return dynamicMapper.getOtherDynamicList(pageSize,lastId,userId,toUserId);
    }

    @Override
    public List<Map<String, Object>> getLikeList(Long lastId, Long dynamicId, Integer startPage, Integer pageSize) {
        if (startPage == 1){
            lastId = null;
        }

        return dynamicMapper.getLikeList(pageSize,lastId,dynamicId);
    }

    @Override
    public Map<String, Object> getDynamicById(Long userId, Long dynamicId) {
        Map<String,Object> resultMap = new HashMap<>();
        Map<String,Object> dynamicMap = dynamicMapper.getDynamicDetailById(userId,dynamicId);
        resultMap.put("dynamic",dynamicMap);

        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getDynamicNoticeList(Long lastId, Long userId, Integer startPage, Integer pageSize) {
        if (startPage == 1){
            dynamicMapper.delDyCounts(userId);
            lastId = null;
        }

        return dynamicMapper.getDynamicNoticeList(pageSize,lastId,userId);
    }

    @Override
    public Map<String, Object> getDynamicNoticeCounts(Long userId) {
        Integer noticeCounts = dynamicMapper.getNoticeCounts(userId);

        Map<String, Object> map = dynamicMapper.getDynamicNotice(userId);
        if (map!=null) {
            map.put("counts",noticeCounts==null?"":(noticeCounts>99?"99+":noticeCounts));
        }else{
            map = new HashMap<>();
            FuntimeUser user = userService.queryUserById(userId);
            map.put("portraitAddress",user.getPortraitAddress());
            map.put("sex",user.getSex());
            map.put("counts","");
        }
        return map;
    }
}
