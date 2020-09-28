package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.*;

import java.util.Map;

public interface AdvertisService {


    void saveTencentAd(FuntimeTencentAd tencentAd);

    void saveTencentAdMonitor(FuntimeTencentAdMonitor tencentAd);

    void saveKuaishouAdMonitor(FuntimeKuaishouAdMonitor ad);

    String getCallBackUrlForKS(String imei, String androidId, String oaid);

    String getCallBackUrlForQTT(String imei, String androidId, String oaid);

    String getCallBackUrlForKSApple(String idfa);

    String getCallBackUrlForQTTApple(String idfa);

    String getCallBackUrlForZhihuApple(String idfa);

    String getCallBackUrlForKS2(String ip);

    String getCallBackUrlForQTT2(String ip);

    String getCallBackUrlForZhihu(String ip);

    Map<String,String> getCallBackInfoForWIFI(String ip);

    void saveToutiaoAdMonitor(FuntimeToutiaoAdMonitor ad);

    void saveWifiAdMonitor(FuntimeWifiAdMonitor ad);

    void saveZhihuAdMonitor(FuntimeZhihuAdMonitor ad);

    Map<String,String> getCallBackInfoForWifiApple(String idfa);
}
