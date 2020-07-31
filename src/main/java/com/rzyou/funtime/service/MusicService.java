package com.rzyou.funtime.service;

import com.tencentcloudapi.ame.v20190916.models.DescribeItemsResponse;
import com.tencentcloudapi.ame.v20190916.models.DescribeLyricResponse;
import com.tencentcloudapi.ame.v20190916.models.DescribeMusicResponse;
import com.tencentcloudapi.ame.v20190916.models.DescribeStationsResponse;

import java.util.Map;

/**
 * 2020/3/4
 * LLP-LX
 */
public interface MusicService {
    /**
     * 获取素材库列表时使用
     * @param limit
     * @param offset
     * @return
     */
    DescribeStationsResponse describeStations(Long limit, Long offset) throws Exception;

    /**
     * 分类内容下歌曲列表获取，根据CategoryID或CategoryCode
     * @param limit
     * @param offset
     * @param categoryId
     * @param categoryCode
     * @return
     */
    DescribeItemsResponse describeItems(Long limit, Long offset, String categoryId, String categoryCode) throws Exception;

    /**
     * 根据接口的模式及歌曲ID来取得歌词信息。
     * @param itemId
     * @param subItemType
     * @return
     */
    DescribeLyricResponse describeLyric(String itemId, String subItemType) throws Exception;

    /**
     * 根据接口的模式及歌曲ID来取得对应权限的歌曲播放地址等信息。
     * @param itemId
     * @param subItemType
     * @param identityId
     * @param ssl
     * @return
     */
    DescribeMusicResponse describeMusic(String itemId, String subItemType, String identityId, String ssl) throws Exception;

    /**
     * 获取本地音乐列表
     * @param startPage
     * @param pageSize
     * @param tagId
     * @param content
     * @return
     */
    Map<String, Object> getLocalMusics(Integer startPage, Integer pageSize, Integer tagId, String content) throws Exception;

    /**
     * 初始化音乐
     */
    void initMusics();

    Map<String, Object> getLocalMusics2();

    /**
     * 下载音乐
     * @param musicId
     * @param userId
     */
    Long downloadMusic(Integer musicId, Long userId);


    /**
     * 新增音乐标签
     * @param tagName
     * @param userId
     */
    Long addMusicTag(String tagName, Long userId);

    /**
     * 变更音乐标签
     * @param tagIds
     * @param userMusicId
     */
    void updateMusicTag(String tagIds, Long userMusicId);

    /**
     * 删除音乐标签
     * @param musicTagId
     */
    void delMusicTag(Long musicTagId);

    /**
     * 删除音乐
     * @param userMusicId
     */
    void delMusic(Long userMusicId);

    /**
     * 获取用户音乐标签
     * @param userId
     * @return
     */
    Map<String, Object> getMusicTags(Long userId);

    /**
     * 编辑音乐标签
     * @param userId
     * @param userMusicId
     * @return
     */
    Map<String, Object> editMusicTag(Long userId, Long userMusicId);

    /**
     * 修改标签名称
     * @param tagName
     * @param musicTagId
     */
    void updateMusicTagName(String tagName, Long musicTagId);

    /**
     * 热门音乐列表
     * @return
     * @param startPage
     * @param pageSize
     * @param content
     * @param userId
     */
    Map<String, Object> getMusicsHot(Integer startPage, Integer pageSize, String content, Long userId) throws Exception;

    /**
     * 本地音乐
     * @param startPage
     * @param pageSize
     * @param content
     * @param userId
     * @param musicTagIds
     * @return
     */
    Map<String, Object> getMyMusics(Integer startPage, Integer pageSize, String content, Long userId, String musicTagIds) throws Exception;
}
