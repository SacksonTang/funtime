package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeComment;
import com.rzyou.funtime.entity.FuntimeDynamic;
import com.rzyou.funtime.service.DynamicService;
import com.rzyou.funtime.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("dynamic")
public class DynamicController {

    @Autowired
    DynamicService dynamicService;

    /**
     * 发布动态
     * @param request
     * @return
     */
    @PostMapping("addDynamic")
    public ResultMsg<Object> addDynamic(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            FuntimeDynamic dynamic = JSONObject.toJavaObject(paramJson,FuntimeDynamic.class);
            if (StringUtils.isBlank(dynamic.getResource1())){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            dynamic.setUserId(HttpHelper.getUserId());
            dynamicService.addDynamic(dynamic);
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
     * 删除动态
     * @param request
     * @return
     */
    @PostMapping("delDynamic")
    public ResultMsg<Object> delDynamic(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Long dynamicId = paramJson.getLong("dynamicId");
            if (dynamicId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            dynamicService.delDynamic(dynamicId,userId);
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
     * 发布评论
     * @param request
     * @return
     */
    @PostMapping("addComment")
    public ResultMsg<Object> addComment(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            FuntimeComment comment = JSONObject.toJavaObject(paramJson,FuntimeComment.class);
            if (StringUtils.isBlank(comment.getComment())){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            comment.setUserId(HttpHelper.getUserId());
            dynamicService.addComment(comment);
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
     * 删除评论
     * @param request
     * @return
     */
    @PostMapping("delComment")
    public ResultMsg<Object> delComment(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Long commentId = paramJson.getLong("commentId");
            Long dynamicId = paramJson.getLong("dynamicId");
            if (commentId == null||dynamicId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            dynamicService.delComment(commentId,userId,dynamicId);
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
     * 点赞
     * @param request
     * @return
     */
    @PostMapping("addLike")
    public ResultMsg<Object> addLike(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Long dynamicId = paramJson.getLong("dynamicId");
            if (dynamicId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            dynamicService.addLike(userId,dynamicId);
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
     * 取消点赞
     * @param request
     * @return
     */
    @PostMapping("delLike")
    public ResultMsg<Object> delLike(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Long dynamicId = paramJson.getLong("dynamicId");
            if (dynamicId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            dynamicService.delLike(userId,dynamicId);
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
     * 动态列表
     * @param request
     * @return
     */
    @PostMapping("getDynamicList")
    public ResultMsg<Object> getDynamicList(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Integer startPage = paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize");
            startPage = startPage == null?1:startPage;
            pageSize = pageSize == null?20:pageSize;
            Long lastId = paramJson.getLong("lastId");
            if (startPage>1&&lastId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            List<Map<String, Object>> dynamicList = dynamicService.getDynamicList(lastId, startPage, pageSize,userId);
            Map<String, Object> map = JsonUtil.getMap("dynamicList", dynamicList);
            if (dynamicList!=null&&!dynamicList.isEmpty()) {
                map.put("lastId", dynamicList.get(dynamicList.size() - 1).get("id"));
            }
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
     * 我的动态列表
     * @param request
     * @return
     */
    @PostMapping("getMyDynamicList")
    public ResultMsg<Object> getMyDynamicList(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Integer startPage = paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize");
            startPage = startPage == null?1:startPage;
            pageSize = pageSize == null?20:pageSize;
            Long lastId = paramJson.getLong("lastId");
            if (startPage>1&&lastId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            List<Map<String, Object>> dynamicList = dynamicService.getMyDynamicList(lastId, startPage, pageSize,userId);
            Map<String, Object> map = JsonUtil.getMap("dynamicList", dynamicList);
            if (dynamicList!=null&&!dynamicList.isEmpty()) {
                map.put("lastId", dynamicList.get(dynamicList.size() - 1).get("id"));
            }
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
     * 评论列表
     * @param request
     * @return
     */
    @PostMapping("getCommentList")
    public ResultMsg<Object> getCommentList(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer startPage = paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize");
            startPage = startPage == null?1:startPage;
            pageSize = pageSize == null?20:pageSize;
            Long lastId = paramJson.getLong("lastId");
            Long dynamicId = paramJson.getLong("dynamicId");
            if ((startPage>1&&lastId == null)||dynamicId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            List<Map<String, Object>> commentList = dynamicService.getCommentList(lastId,dynamicId, startPage, pageSize);
            Map<String, Object> map = JsonUtil.getMap("commentList", commentList);
            if (commentList!=null&&!commentList.isEmpty()) {
                map.put("lastId", commentList.get(commentList.size() - 1).get("id"));
            }
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


}
