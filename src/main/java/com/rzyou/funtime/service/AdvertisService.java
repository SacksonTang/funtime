package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeKuaishouAdMonitor;
import com.rzyou.funtime.entity.FuntimeTencentAd;
import com.rzyou.funtime.entity.FuntimeTencentAdMonitor;
import com.rzyou.funtime.entity.FuntimeToutiaoAdMonitor;

public interface AdvertisService {


    void saveTencentAd(FuntimeTencentAd tencentAd);

    void saveTencentAdMonitor(FuntimeTencentAdMonitor tencentAd);

    void saveKuaishouAdMonitor(FuntimeKuaishouAdMonitor ad);

    String getCallBackUrlForKS(String imei, String androidId, String oaid);

    String getCallBackUrlForQTT(String imei, String androidId, String oaid);

    String getCallBackUrlForKSApple(String idfa);

    String getCallBackUrlForQTTApple(String idfa);

    String getCallBackUrlForKS2(String ip);

    String getCallBackUrlForQTT2(String ip);

    void saveToutiaoAdMonitor(FuntimeToutiaoAdMonitor ad);
}
