package com.rzyou.funtime.common.payment.wxpay.sdk;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.component.StaticData;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MyWxPayConfig extends WXPayConfig {

    private byte[] certData;
    private int payType;

    public MyWxPayConfig(int payType) throws Exception {
        this.payType = payType;
        File file = new File(StaticData.CERPATH);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    public InputStream getCertStream() {
        ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }

    String getAppID() {
        if (payType == 1) {
            return Constant.WX_APPID;
        }else if (payType == 2){
            return Constant.WX_SMALL_PROGRAM_APPID;
        }else if (payType == 3){
            return Constant.WX_PUBLIC_APPID;
        }else{
            return null;
        }
    }

    public String getMchID() {
        return Constant.WX_MCHID;
    }

    public String getKey() {
        return Constant.WX_PAY_APPSECRET;
    }



    public int getHttpConnectTimeoutMs() {
        return 8000;
    }

    public int getHttpReadTimeoutMs() {
        return 10000;
    }


    public IWXPayDomain getWXPayDomain() {
        IWXPayDomain iwxPayDomain = new IWXPayDomain() {
            public void report(String domain, long elapsedTimeMillis, Exception ex) {
            }
            public DomainInfo getDomain(WXPayConfig config) {
                return new IWXPayDomain.DomainInfo(WXPayConstants.DOMAIN_API, true);
            }
        };
        return iwxPayDomain;
    }


}
