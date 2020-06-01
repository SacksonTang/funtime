package com.rzyou.funtime.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.mapper.FuntimeMusicMapper;
import com.rzyou.funtime.service.MusicService;
import com.tencentcloudapi.ame.v20190916.AmeClient;
import com.tencentcloudapi.ame.v20190916.models.*;
import com.tencentcloudapi.common.Credential;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Map<String, Object> getLocalMusics(Integer startPage, Integer pageSize, Integer tagId, String content) {
        Map<String,Object> result = new HashMap<>();
        PageHelper.startPage(startPage,pageSize);
        if (StringUtils.isNotBlank(content)){
            content = content.toUpperCase();
        }
        List<Map<String, Object>> musics = musicMapper.getLocalMusics(startPage, pageSize,tagId,content);
        if (musics == null){
            result.put("musics",new PageInfo<>());
        }else{
            result.put("musics",new PageInfo<>(musics));
        }
        return result;
    }
}
