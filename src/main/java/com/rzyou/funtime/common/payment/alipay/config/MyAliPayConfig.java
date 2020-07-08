package com.rzyou.funtime.common.payment.alipay.config;

import com.rzyou.funtime.component.StaticData;

public class MyAliPayConfig extends AliPayConfig{


    @Override
    public String getNotifyUrl() {
        return StaticData.ALIPAYNOTIFYURL;
    }

    @Override
    public String getMerchantCertPath() {
        return StaticData.MERCHANTCERTPATH;
    }

    @Override
    public String getAlipayCertPath() {
        return StaticData.ALIPAYCERTPATH;
    }

    @Override
    public String getAlipayRootCertPath() {
        return StaticData.ALIPAYROOTCERTPATH;
    }
}
