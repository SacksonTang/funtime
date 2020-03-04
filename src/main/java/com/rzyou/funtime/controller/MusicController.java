package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.service.MusicService;
import com.tencentcloudapi.ame.v20190916.models.DescribeItemsResponse;
import com.tencentcloudapi.ame.v20190916.models.DescribeLyricResponse;
import com.tencentcloudapi.ame.v20190916.models.DescribeMusicResponse;
import com.tencentcloudapi.ame.v20190916.models.DescribeStationsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 2020/3/4
 * LLP-LX
 */
@RestController
@RequestMapping("music")
public class MusicController {

    @Autowired
    MusicService musicService;

    /**
     * 获取素材库列表时使用
     * @param request
     * @return
     */
    @PostMapping("describeStations")
    public ResultMsg<Object> describeStations(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long Limit = paramJson.getLong("Limit");
            Long Offset = paramJson.getLong("Offset");
            DescribeStationsResponse data = musicService.describeStations(Limit,Offset);
            result.setData(data);
            return result;

        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 分类内容下歌曲列表获取，根据CategoryID或CategoryCode
     * @param request
     * @return
     */
    @PostMapping("describeItems")
    public ResultMsg<Object> describeItems(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long Limit = paramJson.getLong("Limit");
            Long Offset = paramJson.getLong("Offset");
            String CategoryId = paramJson.getString("CategoryId");
            String CategoryCode = paramJson.getString("CategoryCode");
            DescribeItemsResponse data = musicService.describeItems(Limit,Offset,CategoryId,CategoryCode);
            result.setData(data);
            return result;

        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 根据接口的模式及歌曲ID来取得歌词信息。
     * @param request
     * @return
     */
    @PostMapping("describeLyric")
    public ResultMsg<Object> describeLyric(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String ItemId = paramJson.getString("ItemId");
            String SubItemType = paramJson.getString("SubItemType");
            DescribeLyricResponse data = musicService.describeLyric(ItemId,SubItemType);
            result.setData(data);
            return result;

        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }

    /**
     * 根据接口的模式及歌曲ID来取得对应权限的歌曲播放地址等信息。
     * @param request
     * @return
     */
    @PostMapping("describeMusic")
    public ResultMsg<Object> describeMusic(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String ItemId = paramJson.getString("ItemId");
            String SubItemType = paramJson.getString("SubItemType");
            String IdentityId = paramJson.getString("IdentityId");
            String Ssl = paramJson.getString("Ssl");
            DescribeMusicResponse data = musicService.describeMusic(ItemId,SubItemType,IdentityId,Ssl);
            result.setData(data);
            return result;

        } catch (BusinessException be) {
            be.printStackTrace();
            result.setCode(be.getCode());
            result.setMsg(be.getMsg());
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
            return result;
        }
    }
}
