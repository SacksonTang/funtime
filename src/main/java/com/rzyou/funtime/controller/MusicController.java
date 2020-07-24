package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.service.MusicService;
import com.rzyou.funtime.utils.JsonUtil;
import com.tencentcloudapi.ame.v20190916.models.DescribeItemsResponse;
import com.tencentcloudapi.ame.v20190916.models.DescribeLyricResponse;
import com.tencentcloudapi.ame.v20190916.models.DescribeMusicResponse;
import com.tencentcloudapi.ame.v20190916.models.DescribeStationsResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

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
     * 热门音乐列表
     * @param request
     * @return
     */
    @PostMapping("getMusicsHot")
    public ResultMsg<Object> getMusicsHot(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer startPage = paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize");
            String content = paramJson.getString("content");
            startPage = startPage == null?1:startPage;
            pageSize = pageSize == null?20:pageSize;
            Map<String,Object> map = musicService.getMusicsHot(startPage,pageSize,content);
            result.setData(map);
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
     * 本地音乐列表
     * @param request
     * @return
     */
    @PostMapping("getMyMusics")
    public ResultMsg<Object> getMyMusics(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer startPage = paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize");
            String content = paramJson.getString("content");
            String musicTagIds = paramJson.getString("musicTagIds");
            Long userId = HttpHelper.getUserId();
            startPage = startPage == null?1:startPage;
            pageSize = pageSize == null?20:pageSize;
            Map<String,Object> map = musicService.getMyMusics(startPage,pageSize,content,userId,musicTagIds);
            result.setData(map);
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
     * 下载音乐
     * @param request
     * @return
     */
    @PostMapping("downloadMusic")
    public ResultMsg<Object> downloadMusic(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer musicId = paramJson.getInteger("musicId");
            Long userId = HttpHelper.getUserId();
            if (musicId == null||userId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Long userMusicId = musicService.downloadMusic(musicId,userId);
            Map<String, Object> map = JsonUtil.getMap("userMusicId", userMusicId);
            result.setData(map);
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
     * 下载音乐完成
     * @param request
     * @return
     */
    @PostMapping("downloadMusicOver")
    public ResultMsg<Object> downloadMusicOver(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userMusicId = paramJson.getLong("userMusicId");
            if (userMusicId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            musicService.downloadMusicOver(userMusicId);
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
     * 删除音乐
     * @param request
     * @return
     */
    @PostMapping("delMusic")
    public ResultMsg<Object> delMusic(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userMusicId = paramJson.getLong("userMusicId");
            if (userMusicId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            musicService.delMusic(userMusicId);
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
     * 获取音乐标签
     * @param request
     * @return
     */
    @PostMapping("getMusicTags")
    public ResultMsg<Object> getMusicTags(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if (userId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String,Object> map = musicService.getMusicTags(userId);
            result.setData(map);
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
     * 编辑音乐标签
     * @param request
     * @return
     */
    @PostMapping("editMusicTag")
    public ResultMsg<Object> editMusicTag(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Long userMusicId = paramJson.getLong("userMusicId");
            if (userMusicId == null||userId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            Map<String,Object> map = musicService.editMusicTag(userId,userMusicId);
            result.setData(map);
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
     * 新增音乐标签
     * @param request
     * @return
     */
    @PostMapping("addMusicTag")
    public ResultMsg<Object> addMusicTag(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String tagName = paramJson.getString("tagName");
            Long userId = HttpHelper.getUserId();
            if (StringUtils.isBlank(tagName)||userId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            musicService.addMusicTag(tagName,userId);
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
     * 变更音乐标签
     * @param request
     * @return
     */
    @PostMapping("updateMusicTag")
    public ResultMsg<Object> updateMusicTag(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String tagIds = paramJson.getString("tagIds");
            Long userMusicId = paramJson.getLong("userMusicId");
            if (userMusicId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            musicService.updateMusicTag(tagIds,userMusicId);
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
     * 修改音乐标签名称
     * @param request
     * @return
     */
    @PostMapping("updateMusicTagName")
    public ResultMsg<Object> updateMusicTagName(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String tagName = paramJson.getString("tagName");
            Long musicTagId = paramJson.getLong("musicTagId");
            if (musicTagId == null||StringUtils.isBlank(tagName)){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            musicService.updateMusicTagName(tagName,musicTagId);
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
     * 删除音乐标签
     * @param request
     * @return
     */
    @PostMapping("delMusicTag")
    public ResultMsg<Object> delMusicTag(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long musicTagId = paramJson.getLong("musicTagId");
            if (musicTagId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            musicService.delMusicTag(musicTagId);
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
     * 初始化音乐
     * @param request
     * @return
     */
    //@PostMapping("initMusics")
    public ResultMsg<Object> initMusics(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {

            musicService.initMusics();
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
     * 获取本地列表时使用
     * @param request
     * @return
     */
    @PostMapping("getLocalMusics")
    public ResultMsg<Object> getLocalMusics(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer startPage = paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize");
            Integer tagId = paramJson.getInteger("tagId");
            String content = paramJson.getString("content");
            startPage = startPage == null?1:startPage;
            pageSize = pageSize == null?20:pageSize;
            Map<String, Object> data = musicService.getLocalMusics(startPage,pageSize,tagId,content);
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
     * 获取本地列表时使用
     * @param request
     * @return
     */
    @PostMapping("getLocalMusics2")
    public ResultMsg<Object> getLocalMusics2(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {

            Map<String, Object> data = musicService.getLocalMusics2();
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
     * 获取素材库列表时使用
     * @param request
     * @return
     */
    //@PostMapping("describeStations")
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
    //@PostMapping("describeItems")
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
    //@PostMapping("describeLyric")
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
    //@PostMapping("describeMusic")
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
