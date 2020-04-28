package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.GiveChannel;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("gift")
@Slf4j
public class GiftTransController {

    @Autowired
    AccountService accountService;


    /**
     * 送礼物
     * @param request
     * @return
     */
    @PostMapping("sendGiftForKnapsack")
    public ResultMsg<Object> sendGiftForKnapsack(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            String toUserIds = paramJson.getString("toUserIds");
            Integer giftId = paramJson.getInteger("giftId");
            Integer giftNum = paramJson.getInteger("giftNum");
            Integer giveChannel = paramJson.getInteger("giveChannel");//1-房间2-单发
            Long roomId = paramJson.getLong("roomId");
            if (userId == null || StringUtils.isBlank(toUserIds)||giftId == null || giftNum == null || giveChannel == null
                    ||(giveChannel.equals(GiveChannel.ROOM.getValue())&&roomId==null||giftNum<1)
            ){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            return accountService.sendGiftForKnapsack(userId,toUserIds,giftId,giftNum,"送礼物",giveChannel,roomId);
        } catch (BusinessException be) {
            log.error("sendGift BusinessException==========>{}",be.getMsg());
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 送礼物
     * @param request
     * @return
     */
    @PostMapping("sendGift")
    public ResultMsg<Object> sendGift(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            String toUserIds = paramJson.getString("toUserIds");
            Integer giftId = paramJson.getInteger("giftId");
            Integer giftNum = paramJson.getInteger("giftNum");
            Integer giveChannel = paramJson.getInteger("giveChannel");//1-房间2-单发
            Long roomId = paramJson.getLong("roomId");
            if (userId == null || StringUtils.isBlank(toUserIds)||giftId == null || giftNum == null || giveChannel == null
                    ||(giveChannel.equals(GiveChannel.ROOM.getValue())&&roomId==null||giftNum<1)
                    ){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            return accountService.createGiftTrans(userId,toUserIds,giftId,giftNum,"送礼物",giveChannel,roomId);
        } catch (BusinessException be) {
            log.error("sendGift BusinessException==========>{}",be.getMsg());
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 全麦送礼物
     * @param request
     * @return
     */
    @PostMapping("sendGiftForMic")
    public ResultMsg<Object> sendGiftForMic(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Integer giftId = paramJson.getInteger("giftId");
            Integer giftNum = paramJson.getInteger("giftNum");
            Long roomId = paramJson.getLong("roomId");
            if (userId == null || giftId == null || giftNum == null || roomId == null||giftNum<1){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            return accountService.sendGiftForMic(userId,giftId,giftNum,"送礼物",1,roomId);

        } catch (BusinessException be) {
            log.error("sendGiftForMic BusinessException==========>{}",be.getMsg());
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 全房送礼物
     * @param request
     * @return
     */
    @PostMapping("sendGiftForRoom")
    public ResultMsg<Object> sendGiftForRoom(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Integer giftId = paramJson.getInteger("giftId");
            Integer giftNum = paramJson.getInteger("giftNum");
            Long roomId = paramJson.getLong("roomId");
            if (userId == null || giftId == null || giftNum == null || roomId == null||giftNum<1){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            return accountService.sendGiftForRoom(userId,giftId,giftNum,"送礼物",1,roomId);

        } catch (BusinessException be) {
            log.error("sendGiftForRoom BusinessException==========>{}",be.getMsg());
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 礼物列表
     * @param request
     * @return
     */
    @PostMapping("getGiftTransForPage")
    public ResultMsg<Object> getGiftTransForPage(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?0:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?20:paramJson.getInteger("pageSize");
            String queryDate = paramJson.getString("queryDate");
            Integer type = paramJson.getInteger("type");
            Long userId = paramJson.getLong("userId");
            if(type==null||userId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            //发出
            if(type.intValue()==1){
                result.setData(JsonUtil.getMap("pageInfo",accountService.getGiftOfSendForPage(startPage, pageSize, queryDate, userId)));
            }else{
                result.setData(JsonUtil.getMap("pageInfo",accountService.getGiftOfRecieveForPage(startPage, pageSize, queryDate, userId)));
            }

            return result;
        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }


    /**
     * 礼物列表统计
     * @param request
     * @return
     */
    @PostMapping("getGiftsByUserId")
    public ResultMsg<Object> getGiftsByUserId(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?0:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?20:paramJson.getInteger("pageSize");
            Long userId = paramJson.getLong("userId");
            if(userId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("pageInfo",accountService.getGiftsByUserId(startPage, pageSize, userId)));

            return result;
        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }


}
