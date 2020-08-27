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

    String getCallBackUrlForKS(@Param("imei") String imei, @Param("androidId") String androidId, @Param("oaid") String oaid);

    String getCallBackUrlForQTT(@Param("imei") String imei, @Param("androidId") String androidId, @Param("oaid") String oaid);

    String getCallBackUrlForKSApple(String idfa);

    String getCallBackUrlForQTTApple(String idfa);

    String getCallBackUrlForKS2(String ip);

    String getCallBackUrlForQTT2(String ip);

    int saveToutiaoAdMonitor(FuntimeToutiaoAdMonitor ad);
}
