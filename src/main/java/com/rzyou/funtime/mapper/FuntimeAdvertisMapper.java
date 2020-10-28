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

    int saveBstationAdMonitor(FuntimeBstationAdMonitor ad);

    int saveSohuAdMonitor(FuntimeSohuAdMonitor ad);

    String getCallBackUrlForKS(@Param("imei") String imei, @Param("androidId") String androidId, @Param("oaid") String oaid);

    String getCallBackUrlForQTT(@Param("imei") String imei, @Param("androidId") String androidId, @Param("oaid") String oaid);

    String getCallBackUrlForKSApple(String idfa);

    String getCallBackUrlForQTTApple(String idfa);

    Map<String,String> getCallBackInfoForWifiApple(String idfa);

    String getTrackidForBstationApple(String idfa);

    String getCallBackUrlForZhihuApple(String idfa);

    String getCallBackForSohuApple(String idfa);

    String getCallBackUrlForMeipaiApple(String idfa);

    String getCallBackUrlForKS2(String ip);

    String getCallBackUrlForQTT2(String ip);

    Map<String,String> getCallBackInfoForWIFI(String ip);

    String getCallBackUrlForZhihu(String ip);

    String getCallBackForSohu(String ip);

    String getCallBackUrlForMeipai(String ip);

    String getTrackidForBstation(@Param("ip") String ip, @Param("channel") Integer channel);

    int saveToutiaoAdMonitor(FuntimeToutiaoAdMonitor ad);

    int saveWifiAdMonitor(FuntimeWifiAdMonitor ad);

    int saveZhihuAdMonitor(FuntimeZhihuAdMonitor ad);

    int saveMeipaiAdMonitor(FuntimeMeipaiAdMonitor ad);

    int saveChubaoAdMonitor(FuntimeChubaoAdMonitor ad);

    int saveZuiyouAdMonitor(FuntimeZuiyouAdMonitor ad);

    String getCallBackUrlForChubaoApple(String idfa);

    String getCallBackUrlForChubao(String ip);

    String getCallBackForZuiyou(String ip);

    String getCallBackForZuiyouApple(String idfa);
}
