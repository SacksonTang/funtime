package com.rzyou.funtime.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 2020/2/5
 * LLP-LX
 */
@Mapper
public interface FuntimeAppVersionMapper {

    /**
     * 检查后续版本是否有强制更新
     * @param cId
     * @return
     */
    Integer checkVersion(Integer cId);

    /**
     * 获取当前版本
     * @param platform
     * @param appVersion
     * @return
     */
    Map<String,String> getVersionInfoByVerAndPlatform(@Param("platform") String platform
            , @Param("appVersion") String appVersion);

    /**
     * 获取最新版本
     * @param platform
     * @return
     */
    Map<String,String> getNewVersionInfoByPlatform(String platform);
}
