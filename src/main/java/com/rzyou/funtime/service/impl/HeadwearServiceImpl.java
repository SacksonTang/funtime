package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserAccount;
import com.rzyou.funtime.entity.FuntimeUserAccountHeadwearRecord;
import com.rzyou.funtime.mapper.FuntimeHeadwearMapper;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.HeadwearService;
import com.rzyou.funtime.service.RoomService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.JsonUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HeadwearServiceImpl implements HeadwearService {

    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;
    @Autowired
    RoomService roomService;
    @Autowired
    FuntimeHeadwearMapper headwearMapper;


    @Override
    public Integer getCurrnetHeadwear(Long userId) {
        return headwearMapper.getCurrnetHeadwear(userId);
    }

    @Override
    public Map<String, Object> getHeadwearList(Long userId) {
        Map<String, Object> resultMap = new HashMap<>();

        FuntimeUser user = userService.queryUserById(userId);

        Map<String, Object> userInfoMap = headwearMapper.getUserInfoById(userId);
        if (userInfoMap!=null&&!userInfoMap.isEmpty()){
            resultMap.put("userLevelMap",userInfoMap);
        }

        List<Map<String, Object>> headwearList = headwearMapper.getHeadwearList(userId);
        if (headwearList!=null){
            for (Map<String, Object> map : headwearList) {
                Integer headwearNumber = Integer.parseInt(map.get("headwearNumber").toString());

                map.put("portraitAddress",user.getPortraitAddress());
                map.put("priceTag",headwearMapper.getPriceTagByHeadwearNumber(headwearNumber));
            }
        }
        resultMap.put("headwearList",headwearList);
        return resultMap;
    }

    @Override
    public ResultMsg<Object> buyHeadwear(Long userId, Integer id) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);
        if (userAccount==null){
            resultMsg.setCode(ErrorMsgEnum.USER_NOT_EXISTS.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
            return resultMsg;
        }
        Map<String, Object> headwearInfoMap = headwearMapper.getHeadwearInfoById(id);
        if (headwearInfoMap == null){
            resultMsg.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
            resultMsg.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
            return resultMsg;
        }
        BigDecimal price = new BigDecimal(headwearInfoMap.get("price").toString());
        if (userAccount.getBlueDiamond().subtract(price).doubleValue()<0){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            Map<String, Object> map = new HashMap<>();
            map.put("userBlueAmount",userAccount.getBlueDiamond().intValue());
            map.put("price",price.intValue());
            resultMsg.setData(map);
            return resultMsg;
        }


        FuntimeUserAccountHeadwearRecord record = new FuntimeUserAccountHeadwearRecord();
        record.setHeadwearId(Integer.parseInt(headwearInfoMap.get("headwearId").toString()));
        record.setDays(Integer.parseInt(headwearInfoMap.get("days").toString()));
        record.setPrice(new BigDecimal(headwearInfoMap.get("price").toString()));
        record.setUserId(userId);

        int k = headwearMapper.insertHeadwearRecord(record);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        userService.updateUserAccountForSub(userId,null,price,null);
        accountService.saveUserAccountBlueLog(userId,price,record.getId(), OperationType.BUY_HEADWEAR.getAction(),OperationType.BUY_HEADWEAR.getOperationType(), null);
        Long userHeadwearId = headwearMapper.getUserHeadwearById(userId, record.getHeadwearId());

        headwearInfoMap.put("userId",userId);
        if (userHeadwearId == null){
            k = headwearMapper.insertUserHeadwear(headwearInfoMap);
            if (k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
        }else{
            headwearInfoMap.put("userHeadwearId",userHeadwearId);
            k = headwearMapper.updateUserHeadwear(headwearInfoMap);
            if (k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
        }

        Integer type = headwearMapper.getCurrnetHeadwear(userId);
        if (type !=null) {
            k = headwearMapper.updateUserHeadwearCurrent(userId, record.getHeadwearId());
        }else{

            k = headwearMapper.insertUserHeadwearCurrent(userId,record.getHeadwearId(),2);
        }
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        resultMsg.setData(JsonUtil.getMap("content","剩余"+record.getDays()+"天"));
        return resultMsg;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void setHeadwear(Long userId, Integer headwearId) {
        Long userHeadwearId = headwearMapper.getUserHeadwearById(userId, headwearId);
        if (userHeadwearId == null){
            throw new BusinessException(ErrorMsgEnum.USER_HEADWEAR_NOT_EXIST.getValue(),ErrorMsgEnum.USER_HEADWEAR_NOT_EXIST.getDesc());
        }
        Integer type = headwearMapper.getCurrnetHeadwear(userId);
        int k;
        if (type !=null) {
            k = headwearMapper.updateUserHeadwearCurrent(userId, headwearId);
        }else{

            k = headwearMapper.insertUserHeadwearCurrent(userId,headwearId,2);
        }
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        accountService.portraitSetLevelUrl(userId,headwearMapper.getUrlByHeadwearNumber(headwearId));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void setHeadwear(Long userId) {
        Integer type = headwearMapper.getCurrnetHeadwear(userId);
        int k;
        if (type!=null) {
            k = headwearMapper.updateUserHeadwearCurrent2(userId);
        }else{
            k = headwearMapper.insertUserHeadwearCurrent(userId,null,1);
        }
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        String url = headwearMapper.getUrlByUserId(userId);
        if (StringUtils.isNotBlank(url)) {
            accountService.portraitSetLevelUrl(userId, url);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void cancelHeadwear(Long userId, Integer headwearId) {
        Long userHeadwearId = headwearMapper.getUserHeadwearById(userId, headwearId);
        if (userHeadwearId == null){
            throw new BusinessException(ErrorMsgEnum.USER_HEADWEAR_NOT_EXIST.getValue(),ErrorMsgEnum.USER_HEADWEAR_NOT_EXIST.getDesc());
        }
        headwearMapper.deleteUserHeadwearCurrent(userId);
        accountService.portraitSetLevelUrl(userId,"");
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void cancelHeadwear(Long userId) {
        headwearMapper.deleteUserHeadwearCurrent(userId);
        accountService.portraitSetLevelUrl(userId,"");
    }

    @Override
    public void setHeadwearTask() {
        List<Map<String,Object>> list = headwearMapper.getHeadwearInfoForExpire();
        if (list!=null&&!list.isEmpty()){
            for (Map<String,Object> map : list){
                Long id = Long.parseLong(map.get("id").toString());
                Long userId = Long.parseLong(map.get("userId").toString());
                Integer isCurrent = Integer.parseInt(map.get("isCurrent").toString());
                try {
                    setHeadwearTask(id,userId,isCurrent);
                }catch (Exception e){
                    log.error("setHeadwearTask  error userId === {}",userId);
                    continue;
                }
            }
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    void setHeadwearTask(Long id,Long userId,Integer isCurrent){
        headwearMapper.deleteUserHeadwearById(id);
        if (isCurrent == 1){
            headwearMapper.deleteUserHeadwearCurrent(userId);
            FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);
            String levelUrl = userAccount.getLevelUrl();
            if (userAccount.getLevel()>0){
                headwearMapper.insertUserHeadwearCurrent(userId,null,1);
            }else{
                levelUrl = "";
            }
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            TencentUtil.portraitSet(userSig, userId.toString(), levelUrl);

            Long roomId = roomService.checkUserIsInMic(userId);
            if (roomId != null) {
                roomService.sendRoomInfoNotice(roomId);

            }
        }
    }
}
