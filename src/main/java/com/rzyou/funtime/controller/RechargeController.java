package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.PayState;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("recharge")
public class RechargeController {

    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;


    /**
     * 充值生成待支付记录
     */
    @PostMapping("startRecharge")
    public ResultMsg<Object> startRecharge(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            FuntimeUserAccountRechargeRecord record = JSONObject.toJavaObject(paramJson, FuntimeUserAccountRechargeRecord.class);

            if (record==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            accountService.recharge(record);

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
     * 获取充值明细
     */
    public ResultMsg<Object> getRechargeDetailForPage(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?0:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?0:paramJson.getInteger("pageSize");
            String queryDate = paramJson.getString("queryDate");
            Integer state = paramJson.getInteger("state");
            Long userId = paramJson.getLong("userId");
            if(StringUtils.isBlank(queryDate)||state==null||userId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            accountService.getRechargeDetailForPage(startPage,pageSize,queryDate,state,userId);

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
