package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeKuaishouAdMonitor;
import com.rzyou.funtime.entity.FuntimeTencentAd;
import com.rzyou.funtime.entity.FuntimeTencentAdMonitor;
import com.rzyou.funtime.entity.FuntimeToutiaoAdMonitor;

public interface AdvertisService {


    void saveTencentAd(FuntimeTencentAd tencentAd);

    void saveTencentAdMonitor(FuntimeTencentAdMonitor tencentAd);

    void saveKuaishouAdMonitor(FuntimeKuaishouAdMonitor ad);

    String getCallBackUrlForKS(String idfa, String androidId);

    String getCallBackUrlForQTT(String idfa, String androidId);

    void saveToutiaoAdMonitor(FuntimeToutiaoAdMonitor ad);
}
