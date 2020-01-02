package com.rzyou.funtime.common.im;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;

import com.tencentcloudapi.faceid.v20180301.FaceidClient;
import com.tencentcloudapi.faceid.v20180301.models.BankCardVerificationRequest;
import com.tencentcloudapi.faceid.v20180301.models.BankCardVerificationResponse;
import com.tencentcloudapi.tcaplusdb.v20190823.models.DescribeZonesRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BankCardVerificationUtil {


    public static void bankCardVerification(String bankCard,String name,String idCard){
        try{
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey
            Credential cred = new Credential(Constant.TENCENT_YUN_COS_SECRETID, Constant.TENCENT_YUN_COS_SECRETKEY);

            // 实例化要请求产品(以cvm为例)的client对象
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setSignMethod(ClientProfile.SIGN_TC3_256);
            FaceidClient client = new FaceidClient(cred, Constant.TENCENT_YUN_COS_REGION, clientProfile);

            // 实例化一个请求对象
            BankCardVerificationRequest req = new BankCardVerificationRequest();

            req.setBankCard(bankCard);
            req.setCertType(0);
            req.setName(name);
            req.setIdCard(idCard);
            // 通过client对象调用想要访问的接口，需要传入请求对象
            BankCardVerificationResponse resp = client.BankCardVerification(req);

            if (resp == null){
                throw new BusinessException(ErrorMsgEnum.USER_BANKCARD_VALID_ERROR.getValue(),ErrorMsgEnum.USER_BANKCARD_VALID_ERROR.getDesc());
            }

            if (!"1".equals(resp.getResult())){
                throw new BusinessException(ErrorMsgEnum.USER_BANKCARD_VALID_ERROR.getValue(),resp.getDescription());
            }
        } catch (TencentCloudSDKException e) {
            log.info("银行卡三要素 error : {}",e.toString());
            throw new BusinessException(ErrorMsgEnum.USER_BANKCARD_VALID_ERROR.getValue(),ErrorMsgEnum.USER_BANKCARD_VALID_ERROR.getDesc());
        }catch (Exception e){
            log.info("银行卡三要素 error : {}",e.toString());
            throw new BusinessException(ErrorMsgEnum.USER_BANKCARD_VALID_ERROR.getValue(),ErrorMsgEnum.USER_BANKCARD_VALID_ERROR.getDesc());
        }

    }

}
