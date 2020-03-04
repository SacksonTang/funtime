package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.service.MusicService;
import com.tencentcloudapi.ame.v20190916.AmeClient;
import com.tencentcloudapi.ame.v20190916.models.*;
import com.tencentcloudapi.common.Credential;
import org.springframework.stereotype.Service;

/**
 * 2020/3/4
 * LLP-LX
 */
@Service
public class MusicServiceImpl implements MusicService {
    @Override
    public DescribeStationsResponse describeStations(Long limit, Long offset) throws Exception{
        Credential credential = new Credential(Constant.TENCENT_YUN_SECRETID,Constant.TENCENT_YUN_SECRETKEY);
        AmeClient ameClient = new AmeClient(credential,null);
        DescribeStationsRequest req = new DescribeStationsRequest();
        req.setLimit(limit);
        req.setOffset(offset);
        DescribeStationsResponse response = ameClient.DescribeStations(req);
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
}
