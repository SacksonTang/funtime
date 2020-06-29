package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.service.GameService;
import com.rzyou.funtime.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("activity")
public class ActivityController {


    @Autowired
    GameService gameService;
    @Autowired
    UserService userService;

    /**
     * 获取活动转盘配置
     * @param request
     * @return
     */
    @PostMapping("getCircleActivityConf")
    public ResultMsg<Object> getCircleActivityConf(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {

            result.setData(gameService.getCircleActivityConf());
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
     * 夺宝
     * @param request
     * @return
     */
    @PostMapping("circleActivityDrawing")
    public ResultMsg<Object> circleActivityDrawing(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String showId = paramJson.getString("showId");
            String activityNo = paramJson.getString("activityNo");
            String channelNo = paramJson.getString("channelNo");
            if (StringUtils.isBlank(showId)||StringUtils.isBlank(activityNo)||StringUtils.isBlank(channelNo)){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            FuntimeUser user = userService.getUserInfoByShowId(showId);
            if (user == null){
                result.setCode(ErrorMsgEnum.DRAW_ACTIVITY_ID_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.DRAW_ACTIVITY_ID_ERROR.getDesc());
                return result;
            }
            return gameService.circleActivityDrawing(user.getId(),activityNo,channelNo);

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
