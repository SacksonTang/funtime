package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.service.HeadwearService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("headwear")
public class HeadwearController {

    @Autowired
    HeadwearService headwearService;

    /**
     * 头饰列表
     */
    @PostMapping("getHeadwearList")
    public ResultMsg<Object> getHeadwearList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if (userId == null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String,Object> map = headwearService.getHeadwearList(userId);
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
     * 购买头饰
     */
    @PostMapping("buyHeadwear")
    public ResultMsg<Object> buyHeadwear(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer id = paramJson.getInteger("id");
            if (userId == null||id == null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            return headwearService.buyHeadwear(userId,id);
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
     * 设置头饰
     */
    @PostMapping("setHeadwear")
    public ResultMsg<Object> setHeadwear(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer headwearId = paramJson.getInteger("headwearId");
            Integer type = paramJson.getInteger("type");

            if (userId == null||(headwearId == null&&type == 2)) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            if (type == 2) {
                headwearService.setHeadwear(userId, headwearId);
            }else{
                headwearService.setHeadwear(userId);
            }
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
     * 设置头饰
     */
    @PostMapping("cancelHeadwear")
    public ResultMsg<Object> cancelHeadwear(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer headwearId = paramJson.getInteger("headwearId");
            Integer type = paramJson.getInteger("type");

            if (userId == null||(headwearId == null&&type == 2)) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            if (type == 2) {
                headwearService.cancelHeadwear(userId, headwearId);
            }else{
                headwearService.cancelHeadwear(userId);
            }
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
