package com.rzyou.funtime.common.payment.alipay;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.factory.Factory.Payment;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.payment.alipay.config.MyAliPayConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
@Slf4j
public class MyAlipay {


    public static Map<String, Object> alipay(String subject, String outTradeNo, String totalAmount) {
        boolean success ;
        Map<String, Object> map ;
        AlipayTradeAppPayResponse response = null;
        try {
            Factory.setOptions(getOptions());
            log.info("支付宝发起支付====== : {},{},{}",subject,outTradeNo,totalAmount);
            response = Payment.App().pay(subject, outTradeNo, totalAmount);
            log.info("alipay =======resp :{}",response.toMap());
            success = ResponseChecker.success(response);
            map = response.toMap();
        }catch (Exception e){
            log.error("alipay response:{}",outTradeNo);
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.ALIPAY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_ERROR.getDesc());
        }
        if (success){
            return map;
        }else {
            log.error("alipay response:{}",map);
            throw new BusinessException(ErrorMsgEnum.ALIPAY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_ERROR.getDesc());
        }
    }

    public static Map<String, Object> alipayH5(String subject, String outTradeNo, String totalAmount,String quitUrl,String returnUrl) {
        boolean success ;
        Map<String, Object> map ;
        AlipayTradeWapPayResponse response = null;
        try {
            Factory.setOptions(getOptions());
            log.info("支付宝发起支付====== : {},{},{}",subject,outTradeNo,totalAmount);
            response = Payment.Wap().pay(subject,outTradeNo,totalAmount,quitUrl,returnUrl);
            log.info("alipay =======resp :{}",response.toMap());
            success = ResponseChecker.success(response);
            map = response.toMap();
        }catch (Exception e){
            log.error("alipay response:{}",outTradeNo);
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.ALIPAY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_ERROR.getDesc());
        }
        if (success){
            return map;
        }else {
            log.error("alipay response:{}",map);
            throw new BusinessException(ErrorMsgEnum.ALIPAY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_ERROR.getDesc());
        }
    }


    public static AlipayTradeQueryResponse query(String outTradeNo){
        log.info("支付宝发起支付查询====== : {}",outTradeNo);
        boolean success ;
        Map<String, Object> map ;
        AlipayTradeQueryResponse response = null;
        try {
            Factory.setOptions(getOptions());
            response = Payment.Common().query(outTradeNo);
            success = ResponseChecker.success(response);
            map = response.toMap();
        }catch (Exception e){
            log.error("alipayQuery response:{}",outTradeNo);
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.ALIPAY_QUERY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_QUERY_ERROR.getDesc());
        }
        if (success){
            return response;
        }else {
            log.error("alipay response:{}",map);
            throw new BusinessException(ErrorMsgEnum.ALIPAY_QUERY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_QUERY_ERROR.getDesc());
        }

    }

    /*
    public static AlipayTradeQueryResponse query(String outTradeNo){
        log.info("支付宝发起支付查询====== : {}",outTradeNo);
        MyAliPayConfig aliPayConfig = new MyAliPayConfig();
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",aliPayConfig.getAppId(),aliPayConfig.getMerchantPrivateKey(),"json","utf-8",aliPayConfig.getAlipayPublicKey(),"RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model=new AlipayTradeQueryModel();
        model.setOutTradeNo(outTradeNo);
        //model.setTradeNo(trade_no);
        request.setBizModel(model);
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("alipayQuery response:{}",outTradeNo);
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.ALIPAY_QUERY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_QUERY_ERROR.getDesc());
        }
        if(response.isSuccess()){
            return response;
        } else {
            throw new BusinessException(ErrorMsgEnum.ALIPAY_QUERY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_QUERY_ERROR.getDesc());
        }
    }*/


    public static Map<String,Object> closeOrder(String outTradeNo){
        log.info("支付宝发起支付关闭====== : {}",outTradeNo);
        boolean success ;
        Map<String, Object> map ;
        AlipayTradeCloseResponse response = null;
        try {
            Factory.setOptions(getOptions());
            response = Payment.Common().close(outTradeNo);
            success = ResponseChecker.success(response);
            map = response.toMap();
        }catch (Exception e){
            log.error("alipayQuery response:{}",outTradeNo);
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.ALIPAY_QUERY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_QUERY_ERROR.getDesc());
        }
        if (success){
            return map;
        }else {
            log.error("alipay response:{}",map);
            throw new BusinessException(ErrorMsgEnum.ALIPAY_QUERY_ERROR.getValue(),response.msg+","+response.subMsg);
        }

    }

    /*
    public static void closeOrder(String outTradeNo){
        log.info("支付宝发起支付关闭====== : {}",outTradeNo);
        MyAliPayConfig aliPayConfig = new MyAliPayConfig();
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",aliPayConfig.getAppId(),aliPayConfig.getMerchantPrivateKey(),"json","utf-8",aliPayConfig.getAlipayPublicKey(),"RSA2");
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject object = new JSONObject();
        object.put("out_trade_no",outTradeNo);
        request.setBizContent(JSONObject.toJSONString(object));
        AlipayTradeCloseResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorMsgEnum.ALIPAY_QUERY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_QUERY_ERROR.getDesc());
        }
        if(!response.isSuccess()){
            throw new BusinessException(ErrorMsgEnum.ALIPAY_QUERY_ERROR.getValue(),response.getMsg()+","+response.getSubMsg());
        }
    }*/

    private static Config getOptions() {
        Config config = new Config();
        MyAliPayConfig aliPayConfig = new MyAliPayConfig();
        config.protocol = aliPayConfig.getProtocol();
        config.gatewayHost = aliPayConfig.getGatewayHost();
        config.signType = aliPayConfig.getSignType();

        config.appId = aliPayConfig.getAppId();

        // 为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
        config.merchantPrivateKey = aliPayConfig.getMerchantPrivateKey();

        //注：证书文件路径支持设置为文件系统中的路径或CLASS_PATH中的路径，优先从文件系统中加载，加载失败后会继续尝试从CLASS_PATH中加载
        config.merchantCertPath = aliPayConfig.getMerchantCertPath();
        config.alipayCertPath = aliPayConfig.getAlipayCertPath();
        config.alipayRootCertPath = aliPayConfig.getAlipayRootCertPath();

        //注：如果采用非证书模式，则无需赋值上面的三个证书路径，改为赋值如下的支付宝公钥字符串即可
        // config.alipayPublicKey = "<-- 请填写您的支付宝公钥，例如：MIIBIjANBg... -->";

        //可设置异步通知接收服务地址（可选）
        config.notifyUrl = aliPayConfig.getNotifyUrl();

        //可设置AES密钥，调用AES加解密相关接口时需要（可选）
        config.encryptKey = aliPayConfig.getEncryptKey();

        return config;
    }
}
