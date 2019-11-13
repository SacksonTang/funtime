package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.payment.wxpay.MyWxPay;
import com.rzyou.funtime.common.payment.wxpay.sdk.WXPayUtil;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.service.AccountService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("pay")
public class PayController {

    private static Logger log = LoggerFactory.getLogger(PayController.class);

    @Value("app.pay.notifyUrl")
    private String notifyUrl ;
    @Value("app.pay.ip")
    private String ip ;
    @Autowired
    AccountService accountService;


    /**
     * 统一下单
     * @param request
     * @return
     */
    @PostMapping("unifiedorder")
    public ResultMsg<Object> unifiedorder(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String totalFee = paramJson.getString("totalFee");
            String orderNo = paramJson.getString("orderNo");
            String imei = paramJson.getString("imei");
            String orderId = paramJson.getString("orderId");
            if(StringUtils.isBlank(totalFee)||StringUtils.isBlank(orderNo)||StringUtils.isBlank(orderId)){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            if(new BigDecimal(totalFee).intValue()<=0){
                result.setCode(ErrorMsgEnum.TOTAL_FEE_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.TOTAL_FEE_ERROR.getDesc());
                return result;
            }

            Map<String, String> resultMap = MyWxPay.unifiedOrder(totalFee, ip, orderNo, imei, notifyUrl, orderId);
            if("SUCCESS".equals(resultMap.get("return_code"))){
                return result;
            }else{
                result.setCode(resultMap.get("return_code"));
                result.setMsg(resultMap.get("return_msg"));
                return result;
            }

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


    @RequestMapping(value = "notifyWxPay", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String notifyWxPay(HttpServletRequest request) throws Exception {
        log.info("微信支付回调");
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        String resultxml = new String(outSteam.toByteArray(), "utf-8");
        Map<String, String> params = WXPayUtil.xmlToMap(resultxml);
        outSteam.close();
        inStream.close();


        Map<String,String> return_data = new HashMap<>();
        if (!MyWxPay.isPayResultNotifySignatureValid(params)) {
            // 支付失败
            return_data.put("return_code", "FAIL");
            return_data.put("return_msg", "签名验证失败");
            return WXPayUtil.mapToXml(return_data);
        } else {
            log.info("===============签名验证成功==============");
            // ------------------------------
            // 处理业务开始
            // ------------------------------
            // 此处处理订单状态，结合自己的订单数据完成订单状态的更新
            // ------------------------------

            try {

                String attach = params.get("attach");
                Long orderId = Long.parseLong(attach);

                accountService.paySuccess(orderId);
                return_data.put("return_code", "SUCCESS");
                return_data.put("return_msg", "OK");
                return WXPayUtil.mapToXml(return_data);
            }catch (BusinessException e1){
                e1.printStackTrace();
                return_data.put("return_code", "FAIL");
                return_data.put("return_msg", e1.getMsg());
                return WXPayUtil.mapToXml(return_data);
            }catch (Exception e2){
                e2.printStackTrace();
                return_data.put("return_code", "FAIL");
                return_data.put("return_msg", "SYSTEM ERROR");
                return WXPayUtil.mapToXml(return_data);
            }
        }
    }


}
