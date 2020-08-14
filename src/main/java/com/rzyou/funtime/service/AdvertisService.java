package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeKuaishouAdMonitor;
import com.rzyou.funtime.entity.FuntimeTencentAd;
import com.rzyou.funtime.entity.FuntimeTencentAdMonitor;

public interface AdvertisService {


    void saveTencentAd(FuntimeTencentAd tencentAd);

    void saveTencentAdMonitor(FuntimeTencentAdMonitor tencentAd);

    void saveKuaishouAdMonitor(FuntimeKuaishouAdMonitor ad);

    String getCallBackUrl(String idfa, String androidId);
}
