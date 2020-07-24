package com.rzyou.funtime.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.entity.music.FuntimeMusicTag;
import com.rzyou.funtime.entity.music.FuntimeUserMusic;
import com.rzyou.funtime.mapper.FuntimeMusicMapper;
import com.rzyou.funtime.service.MusicService;
import com.rzyou.funtime.utils.FileUtil;
import com.tencentcloudapi.ame.v20190916.AmeClient;
import com.tencentcloudapi.ame.v20190916.models.*;
import com.tencentcloudapi.common.Credential;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2020/3/4
 * LLP-LX
 */
@Service
public class MusicServiceImpl implements MusicService {
    @Autowired
    FuntimeMusicMapper musicMapper;

    @Override
    public DescribeStationsResponse describeStations(Long limit, Long offset) throws Exception{
        Credential credential = new Credential(Constant.TENCENT_YUN_SECRETID,Constant.TENCENT_YUN_SECRETKEY);
        AmeClient ameClient = new AmeClient(credential,null);
        DescribeStationsRequest req = new DescribeStationsRequest();
        req.setLimit(limit);
        req.setOffset(offset);
        DescribeStationsResponse response = ameClient.DescribeStations(req);
        if (response != null){
            Station[] stations = response.getStations();
            if (stations!=null){
                for (Station station : stations){
                    String name = station.getName();
                    if (StringUtils.isNotBlank(name)){
                        String newName = name.replaceAll("素材_", "");
                        station.setName(newName);
                    }
                }
            }
        }
        return response;
    }

    @Override
    public DescribeItemsResponse describeItems(Long limit, Long offset, String categoryId, String categoryCode) throws Exception{
        Credential credential = new Credential(Constant.TENCENT_YUN_SECRETID,Constant.TENCENT_YUN_SECRETKEY);
        AmeClient ameClient = new AmeClient(credential,null);
        DescribeItemsRequest req = new DescribeItemsRequest();
        req.setLimit(limit);
        req.setOffset(offset);
        req.setCategoryCode(categoryCode);
        req.setCategoryId(categoryId);
        DescribeItemsResponse response = ameClient.DescribeItems(req);
        return response;
    }

    @Override
    public DescribeLyricResponse describeLyric(String itemId, String subItemType) throws Exception {
        Credential credential = new Credential(Constant.TENCENT_YUN_SECRETID,Constant.TENCENT_YUN_SECRETKEY);
        AmeClient ameClient = new AmeClient(credential,null);
        DescribeLyricRequest req = new DescribeLyricRequest();
        req.setItemId(itemId);
        req.setSubItemType(subItemType);

        DescribeLyricResponse response = ameClient.DescribeLyric(req);
        return response;
    }

    @Override
    public DescribeMusicResponse describeMusic(String itemId, String subItemType, String identityId, String ssl) throws Exception {
        Credential credential = new Credential(Constant.TENCENT_YUN_SECRETID,Constant.TENCENT_YUN_SECRETKEY);
        AmeClient ameClient = new AmeClient(credential,null);
        DescribeMusicRequest req = new DescribeMusicRequest();
        req.setItemId(itemId);
        req.setSubItemType(subItemType);
        req.setSsl(ssl);
        req.setIdentityId(identityId);

        DescribeMusicResponse response = ameClient.DescribeMusic(req);
        return response;
    }

    @Override
    public Map<String, Object> getLocalMusics(Integer startPage, Integer pageSize, Integer tagId, String content) throws Exception {
        Map<String,Object> result = new HashMap<>();
        PageHelper.startPage(startPage,pageSize);
        if (StringUtils.isNotBlank(content)){
            content = content.toUpperCase();
        }
        List<Map<String, Object>> musics = musicMapper.getLocalMusics(tagId,content);
        if (musics == null){
            result.put("musics",new PageInfo<>());
        }else{
            for (Map<String, Object> map : musics){
                String urlStr = map.get("url").toString();

                URL url = new URL(urlStr);
                URI uri = new URI(url.getProtocol(),  url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                url = uri.toURL();

                map.put("url", url);
            }
            result.put("musics",new PageInfo<>(musics));
        }
        return result;
    }

    @Override
    public void initMusics() {
        List<String> files = FileUtil.getFiles("C:\\Users\\AC\\Desktop\\热门");
        if (files!=null&&!files.isEmpty()){
            Map<String, Object> map ;
            for (String fileName:files){
                String url = "https://music-1300805214.cos.ap-shanghai.myqcloud.com/music/热门/"+fileName;
                map = new HashMap<>();
                map.put("url",url);
                map.put("tagId",79);
                map.put("name",fileName.replaceAll(".mp3",""));
                map.put("searchName",fileName.replaceAll(".mp3","").toUpperCase());
                map.put("type","MP3");
                musicMapper.insertMusic(map);
            }
        }
    }

    @Override
    public Map<String, Object> getLocalMusics2() {
        List<String> musics2 = musicMapper.getLocalMusics2();
        List<String> data = new ArrayList<>();
        for (String url : musics2){
            if (!FileUtil.isConnect(url)){
                data.add(url);
            }
        }
        Map<String,Object> result = new HashMap<>();
        result.put("urls",data);
        return result;
    }

    @Override
    public Long downloadMusic(Integer musicId, Long userId) {
        FuntimeUserMusic userMusic = new FuntimeUserMusic();
        userMusic.setMusicId(musicId);
        userMusic.setUserId(userId);
        userMusic.setState(1);
        int k = musicMapper.insertUserMusic(userMusic);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return userMusic.getId();
    }

    @Override
    public void downloadMusicOver(Long userMusicId) {
        if (musicMapper.getUserMusicById(userMusicId) == null){
            throw new BusinessException(ErrorMsgEnum.USER_MUSIC_NOT_EXIST.getValue(),ErrorMsgEnum.USER_MUSIC_NOT_EXIST.getDesc());
        }
        int k = musicMapper.updateUserMusicState(userMusicId);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void addMusicTag(String tagName, Long userId) {
        if (musicMapper.getMusicTagByName(tagName,userId)>0){
            throw new BusinessException(ErrorMsgEnum.USER_MUSIC_TAG_EXIST.getValue(),ErrorMsgEnum.USER_MUSIC_TAG_EXIST.getDesc());
        }
        if (musicMapper.getMusicTagByName(null,userId)>=50){
            throw new BusinessException(ErrorMsgEnum.USER_MUSIC_TAG_LIMIT.getValue(),ErrorMsgEnum.USER_MUSIC_TAG_LIMIT.getDesc());
        }
        FuntimeMusicTag musicTag = new FuntimeMusicTag();
        musicTag.setTagName(tagName);
        musicTag.setUserId(userId);
        int k = musicMapper.insertMusicTag(musicTag);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void updateMusicTag(String tagIds, Long userMusicId) {
        if(musicMapper.getUserMusicTagCount(userMusicId)>0){
            musicMapper.deleteUserMusicTag(userMusicId);
        }
        if (StringUtils.isNotBlank(tagIds)){
            String[] tagIdArray = tagIds.split(",");
            if (tagIdArray.length>0){
                for (String tagId : tagIdArray){
                    Long musicTagId = Long.parseLong(tagId);
                    musicMapper.insertUserMusicTag(userMusicId,musicTagId);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void delMusicTag(Long musicTagId) {

        musicMapper.deleteMusicTag(musicTagId);
        if (musicMapper.getUserMusicTagCount2(musicTagId)>0){
            musicMapper.deleteUserMusicTag2(musicTagId);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void delMusic(Long userMusicId) {
        if (musicMapper.getUserMusicById(userMusicId) == null){
            throw new BusinessException(ErrorMsgEnum.USER_MUSIC_NOT_EXIST.getValue(),ErrorMsgEnum.USER_MUSIC_NOT_EXIST.getDesc());
        }
        int k = musicMapper.deleteUserMusic(userMusicId);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        musicMapper.deleteUserMusicTag(userMusicId);
    }

    @Override
    public Map<String, Object> getMusicTags(Long userId) {
        List<Map<String, Object>> list = musicMapper.getMusicTagByUser(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("tags",list);
        return result;
    }

    @Override
    public Map<String, Object> editMusicTag(Long userId, Long userMusicId) {
        List<Map<String, Object>> tags = musicMapper.getMusicTagByUser(userId);
        List<Long> userMusicTags = musicMapper.getUserMusicTag(userMusicId);

        Map<String, Object> result = new HashMap<>();
        result.put("tags",tags);
        result.put("userTags",userMusicTags);

        return result;
    }

    @Override
    public void updateMusicTagName(String tagName, Long musicTagId) {
        FuntimeMusicTag musicTag = new FuntimeMusicTag();
        musicTag.setId(musicTagId);
        musicTag.setTagName(tagName);
        int k = musicMapper.updateMusicTagById(musicTag);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public Map<String, Object> getMusicsHot(Integer startPage, Integer pageSize, String content) throws Exception{
        Map<String,Object> result = new HashMap<>();
        PageHelper.startPage(startPage,pageSize);
        if (StringUtils.isNotBlank(content)){
            content = content.toUpperCase();
        }
        List<Map<String,Object>> musics = musicMapper.getMusicList(content);
        if (musics == null){
            result.put("musics",new PageInfo<>());
        }else{
            for (Map<String, Object> map : musics){
                String urlStr = map.get("url").toString();

                URL url = new URL(urlStr);
                URI uri = new URI(url.getProtocol(),  url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                url = uri.toURL();

                map.put("url", url);
            }
            result.put("musics",new PageInfo<>(musics));
        }
        return result;
    }

    @Override
    public Map<String, Object> getMyMusics(Integer startPage, Integer pageSize, String content, Long userId, String musicTagIds) throws Exception{
        Map<String,Object> result = new HashMap<>();
        PageHelper.startPage(startPage,pageSize);
        if (StringUtils.isNotBlank(content)){
            content = content.toUpperCase();
        }
        List<Map<String,Object>> musics = musicMapper.getMyMusics(content,userId,musicTagIds);
        if (musics == null){
            result.put("musics",new PageInfo<>());
        }else{
            for (Map<String, Object> map : musics){
                String urlStr = map.get("url").toString();

                URL url = new URL(urlStr);
                URI uri = new URI(url.getProtocol(),  url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                url = uri.toURL();

                map.put("url", url);
            }
            result.put("musics",new PageInfo<>(musics));
        }
        return result;
    }
}
