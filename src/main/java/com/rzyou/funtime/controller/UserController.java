package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.cos.CosStsUtil;
import com.rzyou.funtime.common.encryption.AESUtil;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.common.sms.linkme.LinkmeUtil;
import com.rzyou.funtime.entity.FuntimeAccusation;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserAccount;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.JsonUtil;
import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("user")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;
    @Autowired
    ParameterService parameterService;

    /**
     * 等级配置
     */
    @PostMapping("getLevelConf")
    public ResultMsg<Object> getLevelConf(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if (userId == null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            List<Map<String,Object>> levelConfs = accountService.getLevelConf(userId);
            Map<String, Object> map = JsonUtil.getMap("levelConfs", levelConfs);
            map.put("userAccount",userService.getUserAccountInfoById(userId));
            map.put("userPortraitAddress",userService.getUserBasicInfoById(userId).getPortraitAddress());
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
     * 座驾列表
     */
    @PostMapping("getCarList")
    public ResultMsg<Object> getCarList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if (userId == null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            List<Map<String,Object>> cars = accountService.getCarList(userId);
            result.setData(JsonUtil.getMap("cars",cars));
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
     * 购买坐骑
     */
    @PostMapping("buyCar")
    public ResultMsg<Object> buyCar(HttpServletRequest request){
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
            return accountService.buyCar(userId,id);
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
     * 设置坐骑
     */
    @PostMapping("setCar")
    public ResultMsg<Object> setCar(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer carId = paramJson.getInteger("carId");
            if (userId == null||carId == null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            accountService.setCar(userId,carId);
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
     * 用户座驾信息
     * @param request
     * @return
     */
    @PostMapping("getUserCarByUserId")
    public ResultMsg<Object> getUserCarByUserId(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if (userId == null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            List<Map<String,Object>> cars = accountService.getUserCarByUserId(userId);
            result.setData(JsonUtil.getMap("cars",cars));
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
     * 用户背包信息
     * @param request
     * @return
     */
    @PostMapping("getUserKnapsackByUserId")
    public ResultMsg<Object> getUserKnapsackByUserId(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if (userId == null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            List<Map<String,Object>> knapsacks = accountService.getUserKnapsackByUserId(userId);
            result.setData(JsonUtil.getMap("knapsacks",knapsacks));
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
     * 心跳
     */
    @PostMapping("heart")
    public ResultMsg<Object> heart(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            String ipAddr = HttpHelper.getClientIpAddr(request);
            if (userId == null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            userService.saveHeart(userId, ipAddr);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(ErrorMsgEnum.UNKNOWN_ERROR.getValue());
            result.setMsg(ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }
        return result;
    }



    /**
     * 根据类型获取标签
     * @param request
     * @return
     */
    @PostMapping("queryTagsByType")
    public ResultMsg<Object> queryTagsByType(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String tagType = paramJson.getString("tagType");
            Integer type = paramJson.getInteger("type");
            if (StringUtils.isBlank(tagType)) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            List<Map<String,Object>> tags = userService.queryTagsByType(tagType,type);
            result.setData(JsonUtil.getMap("tags",tags));
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
     * 获取表情
     * @param request
     * @return
     */
    @PostMapping("getExpression")
    public ResultMsg<Object> getExpression(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            List<Map<String,Object>> list = userService.getExpression();
            result.setData(JsonUtil.getMap("expressions",list));
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
     * banner列表
     * @param request
     * @return
     */
    @PostMapping("getBanners")
    public ResultMsg<Object> getBanners(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {

            List<Map<String,Object>> list = userService.getBanners();
            result.setData(JsonUtil.getMap("banners",list));
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
     * 获取用户基本信息
     * @param request
     * @return
     */
    @PostMapping("getUserBasicInfoById")
    public ResultMsg<Object> getUserBasicInfoById(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Long byUserId = paramJson.getLong("byUserId");
            if (userId==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            FuntimeUser user = userService.getUserBasicInfoById(userId);
            if (byUserId!=null) {
                user.setConcerned(userService.checkRecordExist(byUserId, userId));
            }
            user.setToken(null);
            result.setData(JsonUtil.getMap("user",user));
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
     * 获取用户账户信息
     * @param request
     * @return
     */
    @PostMapping("getUserAccountInfoById")
    public ResultMsg<Object> getUserAccountInfoById(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            //JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            FuntimeUserAccount userAccount = userService.getUserAccountInfoById(userId);
            userAccount.setBlackDiamondShow(String.valueOf(userAccount.getBlackDiamond().intValue()));
            userAccount.setBlueDiamondShow(String.valueOf(userAccount.getBlueDiamond().intValue()));
            BigDecimal sumGrabAmount = accountService.getSumGrabAmountById(userId, null);
            userAccount.setGrabAmountTotal(sumGrabAmount==null?0:sumGrabAmount.intValue());
            userAccount.setIsRedpacketShow(parameterService.getParameterValueByKey("is_redpacket_show"));
            Map<String, Object> map = JsonUtil.getMap("userAccount", userAccount);
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
     * 修改用户基本信息
     * @param request
     * @return
     */
    @PostMapping("updateUserBasicInfoById")
    public ResultMsg<Object> updateUserBasicInfoById(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            FuntimeUser user = JSONObject.toJavaObject(paramJson, FuntimeUser.class);
            if (user==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            if (user.getOnlineState()!=null&&user.getOnlineState()==0){
                user.setOnlineState(1);
            }

            userService.updateUserBasicInfoById(user);

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
     * 老手机认证
     * @param request
     * @return
     */
    @PostMapping("validPhone")
    public ResultMsg<Object> validPhone(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            String oldPhoneNumber = paramJson.getString("oldPhoneNumber");
            String code = paramJson.getString("code");
            if (userId==null||StringUtils.isBlank(oldPhoneNumber)||StringUtils.isBlank(code)) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            userService.validPhone(userId,code,oldPhoneNumber);

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
     * 修改手机号
     * @param request
     * @return
     */
    @PostMapping("updatePhoneNumber")
    public ResultMsg<Object> updatePhoneNumber(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            String oldPhoneNumber = paramJson.getString("oldPhoneNumber");
            String newPhoneNumber = paramJson.getString("newPhoneNumber");
            String code = paramJson.getString("code");
            if (userId==null||StringUtils.isBlank(oldPhoneNumber)||StringUtils.isBlank(newPhoneNumber)||StringUtils.isBlank(code)) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            userService.updatePhoneNumber(userId,newPhoneNumber,code,oldPhoneNumber);

            return result;
        } catch (BusinessException be) {
            log.error("updatePhoneNumber BusinessException==========>{}",be.getMsg());
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
     * 绑定手机号
     * @param request
     * @return
     */
    @PostMapping("bindPhoneNumber")
    public ResultMsg<Object> bindPhoneNumber(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer bindType = paramJson.getInteger("bindType");
            String token = paramJson.getString("token");
            Integer channel = paramJson.getInteger("channel");
            Integer platform = paramJson.getInteger("platform");
            Long userId = paramJson.getLong("userId");
            String phoneNumber = paramJson.getString("phoneNumber");
            String code = paramJson.getString("code");
            if (bindType==null||userId==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            //秒验
            if (bindType == 1){
                if (StringUtils.isBlank(token)||channel==null||platform==null){
                    result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                    result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                    return result;
                }
            }
            //短信
            if (bindType == 2){
                if (StringUtils.isBlank(phoneNumber)||StringUtils.isBlank(code)){
                    result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                    result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                    return result;
                }
            }

            if (bindType == 1) {
                phoneNumber = LinkmeUtil.getPhone(token, channel, platform, code);
                userService.bindPhoneNumber(userId,phoneNumber);
            }
            if (bindType == 2){
                userService.bindPhoneNumber(userId,phoneNumber,code);
            }
            return result;
        } catch (BusinessException be) {
            log.error("bindPhoneNumber BusinessException==========>{}",be.getMsg());
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
     * 绑定weixin
     * @param request
     * @return
     */
    @PostMapping("bindWeixin")
    public ResultMsg<Object> bindWeixin(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            String code = paramJson.getString("code");
            Integer type = paramJson.getInteger("type");//1-绑定2-换绑3-解绑
            if (userId==null||type==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            if(StringUtils.isBlank(code)&&(type == 1||type == 2)){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            String nickname = userService.bindWeixin(userId,code,type);
            result.setData(JsonUtil.getMap("wxNickname",nickname));
            return result;
        } catch (BusinessException be) {
            log.error("bindWeixin BusinessException==========>{}",be.getMsg());
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
     * 在线状态1-在线2-离线
     * @param request
     * @return
     */
    @PostMapping("updateOnlineState")
    public ResultMsg<Object> updateOnlineState(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Integer onlineState = paramJson.getInteger("onlineState");
            if (userId==null||onlineState==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            userService.saveImHeart(userId,onlineState,"Relogin","Client Login");

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
     * 退出登录
     */
    @PostMapping("logout")
    public ResultMsg<Object> logout(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            //JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            if (userId==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            userService.logout(userId);

            return result;
        } catch (BusinessException be) {
            log.error("logout BusinessException==========>{}",be.getMsg());
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
     * 在线用户查询
     * @param request
     * @return
     */
    @PostMapping("queryUserInfoByOnline")
    public ResultMsg<Object> queryUserInfoByOnline(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?0:paramJson.getInteger("pageSize");
            Integer sex = paramJson.getInteger("sex");
            Integer ageType = paramJson.getInteger("ageType");

            Long userId = HttpHelper.getUserId();

            result.setData(JsonUtil.getMap("pageInfo",userService.queryUserInfoByOnline(startPage,pageSize,sex,ageType,userId)));

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
     * 首页查询用户
     * @param request
     * @return
     */
    @PostMapping("queryUserInfoByIndex")
    public ResultMsg<Object> queryUserInfoByIndex(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?20:paramJson.getInteger("pageSize");
            String content = paramJson.getString("content");

            Long userId = HttpHelper.getUserId();

            result.setData(JsonUtil.getMap("pageInfo",userService.queryUserInfoByIndex(startPage,pageSize,content,userId)));

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
     * 试兑换
     */
    @PostMapping("diamondConvertTrial")
    public ResultMsg<Object> diamondConvertTrial(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            String from = paramJson.getString("from");
            String to = paramJson.getString("to");
            BigDecimal amount = paramJson.getBigDecimal("amount");
            List<String> convert = new ArrayList<>();
            convert.add("rmb");
            convert.add("blue");
            convert.add("black");
            if (StringUtils.isBlank(from)||StringUtils.isBlank(to)
                    ||amount==null||!convert.contains(from)||!convert.contains(to)||amount.intValue()<1) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            Integer convertAmount = accountService.diamondConvertTrial(userId,from ,to ,amount, ConvertType.BLACK_BLUE.getValue());
            result.setData(JsonUtil.getMap("convertAmount",convertAmount));
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
     * 返回兑换比例
     */
    @PostMapping("getRatio")
    public ResultMsg<Object> getRatio(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            String from = paramJson.getString("from");
            String to = paramJson.getString("to");
            List<String> convert = new ArrayList<>();
            convert.add("rmb");
            convert.add("blue");
            convert.add("black");
            if (StringUtils.isBlank(from)||StringUtils.isBlank(to)
                    ||!convert.contains(from)||!convert.contains(to)) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            BigDecimal ratio = accountService.getRatio(userId,from ,to);
            result.setData(JsonUtil.getMap("ratio",ratio));
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
     * 钻石兑换
     * @param request
     * @return
     */
    @PostMapping("diamondConvert")
    public ResultMsg<Object> diamondConvert(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            String from = paramJson.getString("from");
            String to = paramJson.getString("to");
            BigDecimal amount = paramJson.getBigDecimal("amount");
            List<String> convert = new ArrayList<>();
            convert.add("rmb");
            convert.add("blue");
            convert.add("black");
            if (StringUtils.isBlank(from)||StringUtils.isBlank(to)||amount.intValue()<1
                    ||amount==null||!convert.contains(from)||!convert.contains(to)) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            accountService.diamondConvert(userId,from ,to ,amount, ConvertType.BLACK_BLUE.getValue());

            return result;
        } catch (BusinessException be) {
            log.error("diamondConvert BusinessException==========>{}",be.getMsg());
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
     * 兑换列表
     * @param request
     * @return
     */
    @PostMapping("getUserConvertRecordForPage")
    public ResultMsg<Object> getUserConvertRecordForPage(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);

            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?10:paramJson.getInteger("pageSize");
            String queryDate = paramJson.getString("queryDate");
            Long userId = paramJson.getLong("userId");
            Integer convertType = paramJson.getInteger("convertType");
            if(StringUtils.isBlank(queryDate)||userId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            if (convertType == null){
                convertType = ConvertType.BLACK_BLUE.getValue();
            }
            result.setData(JsonUtil.getMap("pageInfo",accountService.getUserConvertRecordForPage(startPage,pageSize,userId,queryDate,convertType)));

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
     * 金币兑换配置
     * @param request
     * @return
     */
    @PostMapping("getGoldConvertConf")
    public ResultMsg<Object> getGoldConvertConf(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            Long userId = HttpHelper.getUserId();
            if(userId==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            result.setData(accountService.getGoldConvertConf(userId));

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
     * 金币兑换
     * @param request
     * @return
     */
    @PostMapping("goldConvert")
    public ResultMsg<Object> goldConvert(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            Integer id = paramJson.getInteger("id");
            if(userId==null||id == null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            accountService.goldConvert(userId,id);

            return result;
        } catch (BusinessException be) {
            log.error("goldConvert BusinessException==========>{}",be.getMsg());
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
     * 实名认证
     */
    @PostMapping("userValid")
    public ResultMsg<Object> userValid(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            String fullname = paramJson.getString("fullname");
            String identityCard = paramJson.getString("identityCard");
            String depositCard = paramJson.getString("depositCard");
            String code = paramJson.getString("code");

            if (StringUtils.isBlank(fullname)||StringUtils.isBlank(identityCard)
                    ||StringUtils.isBlank(depositCard)||StringUtils.isBlank(code)) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            userService.saveUserValid(userId,fullname,identityCard,depositCard,code);

            return result;
        } catch (BusinessException be) {
            log.error("saveUserValid BusinessException==========>{}",be.getMsg());
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
     * 实名认证信息修改
     */
    @PostMapping("updateUserValid")
    public ResultMsg<Object> updateUserValid(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            //String fullname = paramJson.getString("fullname");
            //String identityCard = paramJson.getString("identityCard");
            String depositCard = paramJson.getString("depositCard");
            String code = paramJson.getString("code");

            if (StringUtils.isBlank(depositCard)||StringUtils.isBlank(code)) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            userService.updateUserValid(userId,depositCard,code);

            return result;
        } catch (BusinessException be) {
            log.error("updateUserValid BusinessException==========>{}",be.getMsg());
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
     * 获取实名认证信息
     */
    @PostMapping("getUserValidInfo")
    public ResultMsg<Object> getUserValidInfo(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            //JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String,Object> data = userService.getUserValidInfo(userId);
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
     * 获取提现配置信息
     */
    @PostMapping("getWithdralInfo")
    public ResultMsg<Object> getWithdralInfo(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            //JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String,Object> data = userService.getWithdralInfo(userId);
            boolean isFirst = accountService.checkWithdrawalRecordIsFirst(userId);
            if(isFirst){
                data.put("rule",1);//1-首次规则2-100的倍数
                data.put("multiple",1);
            }else{
                data.put("rule",2);
                data.put("multiple",100);
            }
            data.put("isFirst",isFirst);

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
     * 设置页信息
     * @param request
     * @return
     */
    @PostMapping("getInstallInfo")
    public ResultMsg<Object> getInstallInfo(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            //JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String,Object> data = userService.getInstallInfo(userId);
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
     * 用户绑定信息
     * @param request
     * @return
     */
    @PostMapping("getUserBindInfo")
    public ResultMsg<Object> getUserBindInfo(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            //JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();
            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String,Object> data = userService.getUserBindInfo(userId);
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
     * 关注
     */
    @PostMapping("saveConcern")
    public ResultMsg<Object> saveConcern(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Long toUserId = paramJson.getLong("toUserId");


            if (userId==null||toUserId==null||userId.equals(toUserId)) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            userService.saveConcern(userId,toUserId);

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
     * 取消关注
     */
    @PostMapping("delConcern")
    public ResultMsg<Object> delConcern(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Long toUserId = paramJson.getLong("toUserId");


            if (userId==null||toUserId==null||userId.equals(toUserId)) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            userService.deleteConcern(userId,toUserId);

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
     * 关注列表
     * @param request
     * @return
     */
    @PostMapping("getConcernUserList")
    public ResultMsg<Object> getConcernUserList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Integer onlineState = paramJson.getInteger("onlineState");
            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?10:paramJson.getInteger("pageSize");
            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("concernUserList",userService.getConcernUserList(startPage,pageSize,userId,onlineState)));

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
     * 粉丝列表
     * @param request
     * @return
     */
    @PostMapping("getFansList")
    public ResultMsg<Object> getFansList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?10:paramJson.getInteger("pageSize");
            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("fansList",userService.getFansList(startPage,pageSize,userId)));

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
     * 邀请列表
     * @param request
     * @return
     */
    @PostMapping("getInvitationUserList")
    public ResultMsg<Object> getInvitationUserList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            Long roomId = paramJson.getLong("roomId");
            String content = paramJson.getString("content");
            Integer startPage = paramJson.getInteger("startPage")==null?1:paramJson.getInteger("startPage");
            Integer pageSize = paramJson.getInteger("pageSize")==null?10:paramJson.getInteger("pageSize");
            Integer type = paramJson.getInteger("type")==null?1:paramJson.getInteger("type");
            if (userId==null||roomId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("invitationUserList",userService.getInvitationUserList(startPage,pageSize,userId,roomId,type,content)));

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
     * 用户礼物列表
     * @param request
     * @return
     */
    @PostMapping("getGiftByUserId")
    public ResultMsg<Object> getGiftByUserId(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            //JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();

            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("giftlist",userService.getGiftByUserId(userId)));

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




    @PostMapping("getRankingList")
    public ResultMsg<Object> getRankingList(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String userId = paramJson.getString("userId");
            Integer type = paramJson.getInteger("type");//1-魅力榜2-贡献榜
            Integer dateType = paramJson.getInteger("dateType");//1-日2-周3-月

            if (type==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }
            Map<String,Object> resultMap = userService.getRankingList(dateType, type,userId);

            result.setData(resultMap);

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
     * 用户相册
     * @param request
     * @return
     */
    @PostMapping("getPhotoByUserId")
    public ResultMsg<Object> getPhotoByUserId(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            //JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = HttpHelper.getUserId();

            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("photolist",userService.getPhotoByUserId(userId)));

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
     * 查询聊天室用户信息
     * @param request
     * @return
     */
    @PostMapping("queryUserByChatUser")
    public ResultMsg<Object> queryUserByChatUser(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");//操作用户
            Long byUserId = paramJson.getLong("byUserId");//被查用户
            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("user",userService.queryUserByChatUser(userId,byUserId)));

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
     * 房间角色权限
     * @param request
     * @return
     */
    @PostMapping("getAuthority")
    public ResultMsg<Object> getAuthority(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Integer userRole = paramJson.getInteger("userRole");


            result.setData(JsonUtil.getMap("authority",userService.queryAuthorityByRole(userRole)));

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
     * 获取腾讯签名
     * @param request
     * @return
     */
    @PostMapping("getUserSig")
    public ResultMsg<Object> getUserSig(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            if (userId==null) {
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            result.setData(JsonUtil.getMap("userSig", UsersigUtil.getUsersig(userId.toString())));

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
     * 修改相册信息
     */

    @PostMapping("saveUserPhoto")
    public ResultMsg<Object> saveUserPhoto(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            Long userId = paramJson.getLong("userId");
            JSONArray array = paramJson.getJSONArray("photos");
            if (userId==null||array==null) {

                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            userService.updatePhotoByUserId(userId,array);

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
     * 举报
     */
    @PostMapping("makeAccusation")
    public ResultMsg<Object> makeAccusation(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            FuntimeAccusation accusation = JSONObject.toJavaObject(paramJson,FuntimeAccusation.class);
            if (accusation==null||accusation.getUserId()==null
                    ||accusation.getAccusationId()==null
                    ||accusation.getType()==null
                    ||accusation.getTypeTagId()==null){
                result.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
                result.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
                return result;
            }

            userService.makeAccusation(accusation);

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
     * 获取COS临时密钥
     * @param request
     * @return
     */
    @PostMapping("getCosKey")
    public ResultMsg<Object> getCosKey(HttpServletRequest request){
        ResultMsg<Object> result = new ResultMsg<>();
        try {
            JSONObject paramJson = HttpHelper.getParamterJson(request);
            String allowPrefix = paramJson.getString("allowPrefix");
            String str = CosStsUtil.getCredential(allowPrefix);

            result.setData(JSONObject.parseObject(str));

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
