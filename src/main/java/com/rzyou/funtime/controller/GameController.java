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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 2020/2/28
 * LLP-LX
 */
@RestController
@RequestMapping("game")
@Slf4j
public class GameController {

    @Autowired
    GameService gameService;
    @Autowired
    UserService userService;

    /**
     * 游戏列表
     * @param request
     * @return
     */
    @PostMapping("getGameList")
    public ResultMsg<Object> getGameList(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if (userId == null ){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String, Object> map = JsonUtil.getMap("gameList", gameService.getGameList(userId));
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
     * 奖池信息
     * @param request
     * @return
     */
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
            log.error("getYaoyaoPool BusinessException==========>{}",be.getMsg());
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
     * 摇摇乐配置
     * @param request
     * @return
     */
    @PostMapping("getYaoyaoShowConf")
    public ResultMsg<Object> getYaoyaoShowConf(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");

            if (userId == null ){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String, Object> map = JsonUtil.getMap("isGoldShow", gameService.getYaoyaoShowConf(1,userId));
            map.put("isBlueShow",gameService.getYaoyaoShowConf(2,userId));
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
     * 抽奖
     * @param request
     * @return
     */
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
            log.error("drawing BusinessException==========>{}",be.getMsg());
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
     * 获取捕鱼子弹数
     * @param request
     * @return
     */
    @PostMapping("getBulletOfFish")
    public ResultMsg<Object> getBulletOfFish(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if (userId == null ){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String, Object> map = JsonUtil.getMap("fish", gameService.getBulletOfFish(userId));
            result.setData(map);
            return result;

        } catch (BusinessException be) {
            log.error("getBulletOfFish BusinessException==========>{}",be.getMsg());
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
     * 保存得分
     * @param request
     * @return
     */
    @PostMapping("saveScoreOfFish")
    public ResultMsg<Object> saveScoreOfFish(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Integer score = paramJson.getInteger("score");
            Integer bullet = paramJson.getInteger("bullet");
            if (userId == null||score == null||bullet==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            if (score < 1||bullet < 1){
                return result;
            }
            gameService.saveScoreOfFish(userId,score,bullet);
            return result;

        } catch (BusinessException be) {
            log.error("saveScoreOfFish BusinessException==========>{}",be.getMsg());
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
     * 购买子弹
     * @param request
     * @return
     */
    @PostMapping("buyBullet")
    public ResultMsg<Object> buyBullet(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Integer bullet = paramJson.getInteger("bullet");
            Integer type = paramJson.getInteger("type");
            if (userId == null||bullet==null||bullet<100||bullet%100!=0||type==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            return gameService.buyBullet(userId,bullet,type);

        } catch (BusinessException be) {
            log.error("buyBullet BusinessException==========>{}",be.getMsg());
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
     * 获取捕鱼排行榜
     * @param request
     * @return
     */
    @PostMapping("getFishRanklist")
    public ResultMsg<Object> getFishRanklist(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Integer type = paramJson.getInteger("type"); //1-总榜2-月榜3-周榜
            if (userId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            type = type == null?1:type;
            result.setData(gameService.getFishRanklist(userId,type));
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
     * 获取砸蛋配置
     * @param request
     * @return
     */
    @PostMapping("getSmashEggConf")
    public ResultMsg<Object> getSmashEggConf(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if (userId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            result.setData(gameService.getSmashEggConf(userId));
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
     * 砸蛋
     * @param request
     * @return
     */
    @PostMapping("eggDrawing")
    public ResultMsg<Object> eggDrawing(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer counts = paramJson.getInteger("counts");
            Integer type = paramJson.getInteger("type");
            Long userId = HttpHelper.getUserId();
            if (userId == null||counts == null||type == null||counts<1){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            return gameService.eggDrawing(userId,counts,type);

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
     * 获取转盘配置
     * @param request
     * @return
     */
    @PostMapping("getCircleConf")
    public ResultMsg<Object> getCircleConf(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if (userId == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            result.setData(gameService.getCircleConf(userId));
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
     * 砸蛋
     * @param request
     * @return
     */
    @PostMapping("circleDrawing")
    public ResultMsg<Object> circleDrawing(HttpServletRequest request){

        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer counts = paramJson.getInteger("counts");
            Long userId = HttpHelper.getUserId();
            if (userId == null||counts == null||counts<1){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            return gameService.circleDrawing(userId,counts);

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
