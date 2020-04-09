package com.rzyou.funtime.common.payment.iospay;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.httputil.HttpClientUtil;
import com.rzyou.funtime.component.StaticData;
import lombok.extern.slf4j.Slf4j;

/**
 * 2020/3/1
 * LLP-LX
 */
@Slf4j
public class IosPayUtil {

    /**
     * 苹果内购校验
     * @param transactionId 苹果内购交易ID
     * @param payload 校验体（base64字符串）
     * @return
     */
    public static void iosPay(String transactionId, String payload) {
        log.debug("苹果内购校验开始，交易ID：" + transactionId + " base64校验体：" + payload);

        //线上环境验证
        JSONObject paramMap = new JSONObject();
        paramMap.put("receipt-data",payload);
        String verifyResult = HttpClientUtil.doPost(StaticData.APPLE_URL,paramMap, Constant.CONTENT_TYPE);
        if (verifyResult == null) {
            throw new BusinessException(ErrorMsgEnum.IOSPAY_VALID_ERROR.getValue(),ErrorMsgEnum.IOSPAY_VALID_ERROR.getDesc());
        } else {
            log.info("线上，苹果平台返回JSON:" + verifyResult);
            JSONObject appleReturn = JSONObject.parseObject(verifyResult);
            String states = appleReturn.getString("status");
            //无数据则沙箱环境验证
            if ("21007".equals(states)) {
                verifyResult = HttpClientUtil.doPost(Constant.APPLE_URL_SANDBOX,paramMap, Constant.CONTENT_TYPE);
                log.info("沙盒环境，苹果平台返回JSON:" + verifyResult);
                appleReturn = JSONObject.parseObject(verifyResult);
                states = appleReturn.getString("status");
            }
            // 前端所提供的收据是有效的    验证成功
            if (states.equals("0")) {
                String receipt = appleReturn.getString("receipt");
                JSONObject returnJson = JSONObject.parseObject(receipt);
                String inApp = returnJson.getString("in_app");
                JSONArray inApps = JSONObject.parseArray(inApp);
                if (inApp!=null&&!inApp.isEmpty()) {
                    for (int i = 0;i<inApps.size();i++) {
                        JSONObject app = inApps.getJSONObject(i);
                        if (transactionId.equals(app.getString("transaction_id"))){
                            return ;
                        }
                    }
                    throw new BusinessException(ErrorMsgEnum.IOSPAY_NOT_THIS.getValue(),ErrorMsgEnum.IOSPAY_NOT_THIS.getDesc());
                }
                throw new BusinessException(ErrorMsgEnum.IOSPAY_TRANSFERS_EMPTY.getValue(),ErrorMsgEnum.IOSPAY_TRANSFERS_EMPTY.getDesc());
            } else {
                throw new BusinessException(ErrorMsgEnum.IOSPAY_ERROR.getValue(),ErrorMsgEnum.IOSPAY_ERROR.getDesc()+ states);
            }
        }
    }



}
