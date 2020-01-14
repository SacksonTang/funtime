package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

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

            if (record==null||record.getUserId()==null||record.getRechargeConfId()==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(accountService.createRecharge(record));

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
    @PostMapping("getRechargeDetailForPage")
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

            PageInfo<FuntimeUserAccountRechargeRecord> rechargeDetailForPage = accountService.getRechargeDetailForPage(startPage, pageSize, queryDate, state, userId);

            result.setData(JsonUtil.getMap("pageInfo",rechargeDetailForPage));
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


    @PostMapping("getRechargeConf")
    public ResultMsg<Object> getRechargeConf(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Map<String, Object> conf = JsonUtil.getMap("conf", accountService.getRechargeConf());
            conf.put("rechargeAgreementUrl", Constant.COS_URL_PREFIX+Constant.AGREEMENT_RECHARGE);

            result.setData(conf);

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
