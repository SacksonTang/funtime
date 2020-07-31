package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.entity.FuntimeTencentAd;
import com.rzyou.funtime.entity.FuntimeTencentAdMonitor;
import com.rzyou.funtime.mapper.FuntimeAdvertisMapper;
import com.rzyou.funtime.service.AdvertisService;
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
}
