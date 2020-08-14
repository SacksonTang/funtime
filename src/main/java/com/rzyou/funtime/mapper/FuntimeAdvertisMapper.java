package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeKuaishouAdMonitor;
import com.rzyou.funtime.entity.FuntimeTencentAd;
import com.rzyou.funtime.entity.FuntimeTencentAdMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FuntimeAdvertisMapper {

    int saveTencentAd(FuntimeTencentAd tencentAd);

    int saveTencentAdMonitor(FuntimeTencentAdMonitor tencentAdMonitor);

    int saveKuaishouAdMonitor(FuntimeKuaishouAdMonitor ad);

    String getCallBackUrl(@Param("idfa") String idfa, @Param("androidId") String androidId);
}
