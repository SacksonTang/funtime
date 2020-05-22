package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("recharge")
@Slf4j
public class RechargeController {

    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;
    @Autowired
    ParameterService parameterService;

    /**
     * 苹果内购
     * @param request
     * @return
     */
    @PostMapping("iosRecharge")
    public ResultMsg<Object> iosRecharge(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String transactionId = paramJson.getString("transactionId");
            String payload = paramJson.getString("payload");
            Long userId = paramJson.getLong("userId");
            String productId = paramJson.getString("productId");

            if (userId==null||StringUtils.isBlank(transactionId)||StringUtils.isBlank(payload)
                       ||StringUtils.isBlank(productId)) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            accountService.iosRecharge(userId,transactionId,payload,productId);

            return result;
        } catch (BusinessException be) {
            log.error("iosRecharge BusinessException==========>{}",be.getMsg());
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
     * 充值生成待支付记录
     */
    @PostMapping("startRecharge")
    public ResultMsg<Object> startRecharge(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson;
            String flag = parameterService.getParameterValueByKey("is_encrypt");
            if (flag!=null&&flag.equals("1")){
                paramJson = HttpHelper.getParamterJsonDecrypt(request);
            }else{
                paramJson = HttpHelper.getParamterJson(request);
            }
            FuntimeUserAccountRechargeRecord record = JSONObject.toJavaObject(paramJson, FuntimeUserAccountRechargeRecord.class);

            if (record==null||record.getUserId()==null||record.getRechargeConfId()==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            String ip = HttpHelper.getClientIpAddr(request);
            if (record.getPayType()==null) {
                record.setPayType(1);
            }
            result.setData(accountService.createRecharge(record,ip, "APP"));

            return result;
        } catch (BusinessException be) {
            log.error("APP startRecharge BusinessException==========>{}",be.getMsg());
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

            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?20:paramJson.getInteger("pageSize");
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
            Integer platform = paramJson.getInteger("platform");
            if(platform==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String, Object> conf = JsonUtil.getMap("conf", accountService.getRechargeConf(platform));
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
