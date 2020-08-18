package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.FuntimeKuaishouAdMonitor;
import com.rzyou.funtime.entity.FuntimeTencentAd;
import com.rzyou.funtime.entity.FuntimeTencentAdMonitor;
import com.rzyou.funtime.entity.FuntimeToutiaoAdMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FuntimeAdvertisMapper {

    int saveTencentAd(FuntimeTencentAd tencentAd);

    int saveTencentAdMonitor(FuntimeTencentAdMonitor tencentAdMonitor);

    int saveKuaishouAdMonitor(FuntimeKuaishouAdMonitor ad);

    String getCallBackUrlForKS(@Param("idfa") String idfa, @Param("androidId") String androidId);

    String getCallBackUrlForQTT(@Param("idfa") String idfa, @Param("androidId") String androidId);

    int saveToutiaoAdMonitor(FuntimeToutiaoAdMonitor ad);
}
