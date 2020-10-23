package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.FuntimeAdvertisMapper;
import com.rzyou.funtime.service.AdvertisService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

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
    public String getCallBackUrlForZhihuApple(String idfa) {
        if (StringUtils.isNotBlank(idfa)) {
            idfa = DigestUtils.md5Hex(idfa).toUpperCase();
        }
        return advertisMapper.getCallBackUrlForZhihuApple(idfa);
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
    public String getCallBackUrlForZhihu(String ip) {
        return advertisMapper.getCallBackUrlForZhihu(ip);
    }

    @Override
    public Map<String, String> getCallBackInfoForWIFI(String ip) {
        return advertisMapper.getCallBackInfoForWIFI(ip);
    }

    @Override
    public void saveToutiaoAdMonitor(FuntimeToutiaoAdMonitor ad) {
        advertisMapper.saveToutiaoAdMonitor(ad);
    }

    @Override
    public void saveWifiAdMonitor(FuntimeWifiAdMonitor ad) {
        advertisMapper.saveWifiAdMonitor(ad);
    }

    @Override
    public void saveZhihuAdMonitor(FuntimeZhihuAdMonitor ad) {
        advertisMapper.saveZhihuAdMonitor(ad);
    }

    @Override
    public void saveBstationAdMonitor(FuntimeBstationAdMonitor ad) {
        advertisMapper.saveBstationAdMonitor(ad);
    }

    @Override
    public void saveSohuAdMonitor(FuntimeSohuAdMonitor ad) {
        advertisMapper.saveSohuAdMonitor(ad);
    }

    @Override
    public void saveMeipaiAdMonitor(FuntimeMeipaiAdMonitor ad) {
        advertisMapper.saveMeipaiAdMonitor(ad);
    }

    @Override
    public Map<String, String> getCallBackInfoForWifiApple(String idfa) {
        if (StringUtils.isNotBlank(idfa)) {
            idfa = DigestUtils.md5Hex(idfa);
        }
        return advertisMapper.getCallBackInfoForWifiApple(idfa);
    }

    @Override
    public String getTrackidForBstationApple(String idfa) {
        return advertisMapper.getTrackidForBstationApple(idfa);
    }

    @Override
    public String getTrackidForBstation(String ip, Integer channel) {
        return advertisMapper.getTrackidForBstation(ip,channel);
    }

    @Override
    public String getCallBackForSohuApple(String idfa) {
        return advertisMapper.getCallBackForSohuApple(idfa);
    }

    @Override
    public String getCallBackForSohu(String ip) {
        return advertisMapper.getCallBackForSohu(ip);
    }

    @Override
    public String getCallBackUrlForMeipai(String ip) {
        return advertisMapper.getCallBackUrlForMeipai(ip);
    }

    @Override
    public String getCallBackUrlForMeipaiApple(String idfa) {
        return advertisMapper.getCallBackUrlForMeipaiApple(idfa);
    }
}
