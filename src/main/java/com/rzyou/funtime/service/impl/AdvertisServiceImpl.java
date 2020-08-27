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
    public String getCallBackUrlForKS(String imei, String androidId, String oaid) {
        if (StringUtils.isNotBlank(imei)) {
            imei = DigestUtils.sha1Hex(imei);
        }

        if (StringUtils.isNotBlank(androidId)){
            androidId = DigestUtils.sha1Hex(androidId);
        }
        return advertisMapper.getCallBackUrlForKS(imei,androidId,oaid);
    }

    @Override
    public String getCallBackUrlForQTT(String imei, String androidId, String oaid) {
        if (StringUtils.isNotBlank(imei)) {
            imei = DigestUtils.md5Hex(imei);
        }
        if (StringUtils.isNotBlank(androidId)){
            androidId = DigestUtils.md5Hex(androidId);
        }
        return advertisMapper.getCallBackUrlForQTT(imei,androidId,oaid);
    }

    @Override
    public String getCallBackUrlForKSApple(String idfa) {
        if (StringUtils.isNotBlank(idfa)) {
            idfa = DigestUtils.sha1Hex(idfa).toUpperCase();
        }

        return advertisMapper.getCallBackUrlForKSApple(idfa);
    }

    @Override
    public String getCallBackUrlForQTTApple(String idfa) {
        return advertisMapper.getCallBackUrlForQTTApple(idfa);
    }

    @Override
    public String getCallBackUrlForKS2(String ip) {
        return advertisMapper.getCallBackUrlForKS2(ip);
    }

    @Override
    public String getCallBackUrlForQTT2(String ip) {
        return advertisMapper.getCallBackUrlForQTT2(ip);
    }

    @Override
    public void saveToutiaoAdMonitor(FuntimeToutiaoAdMonitor ad) {
        advertisMapper.saveToutiaoAdMonitor(ad);
    }
}
