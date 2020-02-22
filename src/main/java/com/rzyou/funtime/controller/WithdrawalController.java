package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeUserAccountWithdrawalRecord;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("withdrawal")
public class WithdrawalController {

    @Autowired
    AccountService accountService;
    @Autowired
    ParameterService parameterService;

    /**
     * 申请领赏
     */
    @PostMapping("startWithdrawal")
    public ResultMsg<Object> startWithdrawal(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer withdrawalType = paramJson.getInteger("withdrawalType");
            BigDecimal blackAmount = paramJson.getBigDecimal("blackAmount");
            BigDecimal preRmbAmount = paramJson.getBigDecimal("preRmbAmount");
            BigDecimal preChannelAmount = paramJson.getBigDecimal("preChannelAmount");
            BigDecimal amount = paramJson.getBigDecimal("amount");
            Long userId = paramJson.getLong("userId");
            String ip = HttpHelper.getClientIpAddr(request);
            ip = ip == null?"127.0.0.1":ip;
            if (withdrawalType==null||blackAmount==null||preChannelAmount==null||preRmbAmount==null
                    ||amount==null||userId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            accountService.applyWithdrawal(userId,withdrawalType,blackAmount,preRmbAmount,preChannelAmount,amount,ip);
            Map<String, Object> map = JsonUtil.getMap("weChatSubscriptionDesc", Constant.WXCHATTEMP.replaceAll("#", parameterService.getParameterValueByKey("wechat_subscription")));
            map.put("weChatSubscription",parameterService.getParameterValueByKey("wechat_subscription"));

            result.setData(map);

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
     * 领赏列表
     * @param request
     * @return
     */
    @PostMapping("getWithdrawalForPage")
    public ResultMsg<Object> getWithdrawalForPage(HttpServletRequest request){
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

            PageInfo<FuntimeUserAccountWithdrawalRecord> withdrawalForPage = accountService.getWithdrawalForPage(startPage, pageSize, queryDate, state, userId);

            result.setData(JsonUtil.getMap("pageInfo",withdrawalForPage));
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
