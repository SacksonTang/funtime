package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.entity.FuntimeUserAccount;
import com.rzyou.funtime.service.GameService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 2020/2/28
 * LLP-LX
 */
@RestController
@RequestMapping("game")
public class GameController {

    @Autowired
    GameService gameService;
    @Autowired
    UserService userService;

    @PostMapping("getYaoyaoPool")
    public ResultMsg<Object> getYaoyaoPool(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Integer type = paramJson.getInteger("type");

            if (userId == null || type == null ){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String, Object> pools = JsonUtil.getMap("pools", gameService.getYaoyaoPool(type));
            FuntimeUserAccount info = userService.getUserAccountInfoById(userId);
            if (info==null){
                throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
            }
            if (type == 1){
                pools.put("userAccount",info.getGoldCoin());
            }
            if (type == 2){
                pools.put("userAccount",info.getBlueDiamond().intValue());
            }
            result.setData(pools);
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

    @PostMapping("drawing")
    public ResultMsg<Object> drawing(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Integer id = paramJson.getInteger("id");

            if (userId == null || id == null ){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            result.setData(gameService.drawing(id,userId));
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
