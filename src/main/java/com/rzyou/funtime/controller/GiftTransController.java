package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("gift")
public class GiftTransController {

    @Autowired
    AccountService accountService;

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
            Long toUserId = paramJson.getLong("toUserId");
            Integer giftId = paramJson.getInteger("giftId");
            Integer giftNum = paramJson.getInteger("giftNum");
            Integer giveChannel = paramJson.getInteger("giveChannel");
            Long roomId = paramJson.getLong("roomId");

            accountService.createGiftTrans(userId,toUserId,giftId,giftNum,"送礼物",giveChannel,roomId);


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
            Integer pageSize = paramJson.getInteger("pageSize")==null?0:paramJson.getInteger("pageSize");
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


}
