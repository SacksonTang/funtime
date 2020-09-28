package com.rzyou.funtime.mapper;

import com.rzyou.funtime.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface FuntimeAdvertisMapper {

    int saveTencentAd(FuntimeTencentAd tencentAd);

    int saveTencentAdMonitor(FuntimeTencentAdMonitor tencentAdMonitor);

    int saveKuaishouAdMonitor(FuntimeKuaishouAdMonitor ad);

    String getCallBackUrlForKS(@Param("imei") String imei, @Param("androidId") String androidId, @Param("oaid") String oaid);

    String getCallBackUrlForQTT(@Param("imei") String imei, @Param("androidId") String androidId, @Param("oaid") String oaid);

    String getCallBackUrlForKSApple(String idfa);

    String getCallBackUrlForQTTApple(String idfa);

    Map<String,String> getCallBackInfoForWifiApple(String idfa);

    String getCallBackUrlForZhihuApple(String idfa);

    String getCallBackUrlForKS2(String ip);

    String getCallBackUrlForQTT2(String ip);

    Map<String,String> getCallBackInfoForWIFI(String ip);

    String getCallBackUrlForZhihu(String ip);

    int saveToutiaoAdMonitor(FuntimeToutiaoAdMonitor ad);

    int saveWifiAdMonitor(FuntimeWifiAdMonitor ad);

    int saveZhihuAdMonitor(FuntimeZhihuAdMonitor ad);
}
