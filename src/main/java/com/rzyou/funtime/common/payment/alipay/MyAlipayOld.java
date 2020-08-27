package com.rzyou.funtime.common.payment.alipay;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.payment.alipay.config.MyAliPayConfig;
import com.rzyou.funtime.component.StaticData;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class MyAlipayOld {

    public static Map<String,Object> alipay(String subject, String outTradeNo, String totalAmount) {
        AlipayTradeAppPayResponse response;
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(getClientParams());
            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setSubject(subject);
            model.setOutTradeNo(outTradeNo);
            model.setTotalAmount(totalAmount);
            request.setBizModel(model);
            request.setNotifyUrl(StaticData.ALIPAYNOTIFYURL);
            response = alipayClient.sdkExecute(request);

        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.ALIPAY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_ERROR.getDesc());
        }
        if (response.isSuccess()){
            Map<String,Object> result = (JSONObject)JSONObject.toJSON(response);
            return result;
        }else {
            throw new BusinessException(ErrorMsgEnum.ALIPAY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_ERROR.getDesc());
        }
    }



    public static Map<String,Object> alipayH5(String subject, String outTradeNo, String totalAmount, String quitUrl, String returnUrl) {
        com.alipay.api.response.AlipayTradeWapPayResponse response = null;
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(getClientParams());
            AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
            AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
            model.setSubject(subject);
            model.setOutTradeNo(outTradeNo);
            model.setTotalAmount(totalAmount);
            model.setQuitUrl(quitUrl);
            model.setProductCode("QUICK_WAP_WAY");
            request.setBizModel(model);
            request.setReturnUrl(returnUrl);
            request.setNotifyUrl(StaticData.ALIPAYNOTIFYURL);
            response = alipayClient.pageExecute(request);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(response.isSuccess()){
            Map<String,Object> result = (JSONObject)JSONObject.toJSON(response);
            return result;
        } else {
            throw new BusinessException(ErrorMsgEnum.ALIPAY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_ERROR.getDesc());
        }
    }


    public static AlipayTradeQueryResponse query(String outTradeNo){
        log.info("支付宝发起支付查询====== : {}",outTradeNo);
        AlipayTradeQueryResponse response;
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(getClientParams());
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model=new AlipayTradeQueryModel();
            model.setOutTradeNo(outTradeNo);
            request.setBizModel(model);
            response = alipayClient.certificateExecute(request);
            log.info("支付宝发起支付查询结果====== : {}",JSONObject.toJSONString(response));
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.ALIPAY_QUERY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_QUERY_ERROR.getDesc());
        }
        if(response.isSuccess()){
            return response;
        } else {
            throw new BusinessException(ErrorMsgEnum.ALIPAY_QUERY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_QUERY_ERROR.getDesc());
        }
    }


    public static void closeOrder(String outTradeNo){
        log.info("支付宝发起支付关闭====== : {}",outTradeNo);
        AlipayTradeCloseResponse response;
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(getClientParams());
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(outTradeNo);
            request.setBizModel(model);
            response = alipayClient.certificateExecute(request);
            log.info("支付宝发起支付关闭结果====== : {}",JSONObject.toJSONString(response));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.ALIPAY_CLOSE_ERROR.getValue(),ErrorMsgEnum.ALIPAY_CLOSE_ERROR.getDesc());
        }
        if(!response.isSuccess()){
            throw new BusinessException(ErrorMsgEnum.ALIPAY_CLOSE_ERROR.getValue(),response.getMsg()+","+response.getSubMsg());
        }
    }

    private static CertAlipayRequest getClientParams() {
        MyAliPayConfig aliPayConfig = new MyAliPayConfig();
        CertAlipayRequest certParams = new CertAlipayRequest();
        certParams.setServerUrl(aliPayConfig.getServerUrl());
        //请更换为您的AppId
        certParams.setAppId(aliPayConfig.getAppId());
        //请更换为您的PKCS8格式的应用私钥
        certParams.setPrivateKey(aliPayConfig.getMerchantPrivateKey());
        //请更换为您使用的字符集编码，推荐采用utf-8
        certParams.setCharset("utf-8");
        certParams.setFormat("json");
        certParams.setSignType(aliPayConfig.getSignType());
        //请更换为您的应用公钥证书文件路径
        certParams.setCertPath(aliPayConfig.getMerchantCertPath());
        //请更换您的支付宝公钥证书文件路径
        certParams.setAlipayPublicCertPath(aliPayConfig.getAlipayCertPath());
        //更换为支付宝根证书文件路径
        certParams.setRootCertPath(aliPayConfig.getAlipayRootCertPath());
        return certParams;
    }

}
