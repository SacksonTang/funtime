package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.entity.FuntimeKuaishouAdMonitor;
import com.rzyou.funtime.entity.FuntimeTencentAd;
import com.rzyou.funtime.entity.FuntimeTencentAdMonitor;
import com.rzyou.funtime.entity.FuntimeToutiaoAdMonitor;
import com.rzyou.funtime.mapper.FuntimeAdvertisMapper;
import com.rzyou.funtime.service.AdvertisService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdvertisServiceImpl implements AdvertisService {

    @Autowired
    FuntimeAdvertisMapper advertisMapper;

    @Override
    public void saveTencentAd(FuntimeTencentAd tencentAd) {
        advertisMapper.saveTencentAd(tencentAd);
    }

    @Override
    public void saveTencentAdMonitor(FuntimeTencentAdMonitor tencentAd) {
        advertisMapper.saveTencentAdMonitor(tencentAd);
    }

    @Override
    public void saveKuaishouAdMonitor(FuntimeKuaishouAdMonitor ad) {
        advertisMapper.saveKuaishouAdMonitor(ad);
    }

    @Override
    public String getCallBackUrlForKS(String idfa, String androidId) {
        if (StringUtils.isNotBlank(idfa)) {
            idfa = DigestUtils.sha1Hex(idfa).toUpperCase();
        }

        if (StringUtils.isNotBlank(androidId)){
            androidId = DigestUtils.sha1Hex(androidId).toUpperCase();
        }
        return advertisMapper.getCallBackUrlForKS(idfa,androidId);
    }

    @Override
    public String getCallBackUrlForQTT(String idfa, String androidId) {
        if (StringUtils.isNotBlank(androidId)){
            androidId = DigestUtils.md5Hex(androidId);
        }
        return advertisMapper.getCallBackUrlForQTT(idfa,androidId);
    }

    @Override
    public void saveToutiaoAdMonitor(FuntimeToutiaoAdMonitor ad) {
        advertisMapper.saveToutiaoAdMonitor(ad);
    }
}
